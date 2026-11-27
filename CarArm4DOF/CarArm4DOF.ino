/*
 * Bluetooth 4WD car + 4-DOF robotic arm (merged)
 *   Car part:  your original Code.ino (F / B / L / R / S)
 *   Arm part:  4-DOF Robotic Arm Control by Aryan Panwar (2024)
 *
 * COMMANDS - one capital letter each, like the wheel drive.
 * Send them from the gamepad app (HC-06) or type them in the Serial Monitor (9600).
 *   CAR:   F = forward   B = backward   L = turn   R = turn   S = stop
 *   ARM:   A / Z = base left / right          U / J = shoulder up / down
 *          I / K = elbow up / down            O / C = gripper open / close
 *          H = arm home (all joints to 90)
 *   Each arm letter moves that joint STEP degrees. Hold the app button
 *   (with "send continuously" on) and the joint keeps moving.
 *
 *   Exact angles still work as lowercase text + Enter:
 *     b90  s45  e120  g30  home  open  close  status  help
 *
 * PINS (Arduino Uno)
 *   L298N IN1..IN4 -> D7 D6 D5 D4     (same as the original car code)
 *   HC-06 TXD -> D2, RXD -> D3        (SoftwareSerial, D0/D1 stay free for uploading)
 *   Servos: base D9, shoulder D10, elbow D11, gripper D8   (gripper moved from D6)
 *   Power the servos from a separate 5-6 V supply, GND shared with the Arduino.
 *   Power bank keep-alive: A4 -> 1k -> NPN base; NPN collector -> 47 ohm (1/2 W) -> 5V;
 *                          NPN emitter -> GND   (see KEEPALIVE settings below)
 */

#include <Servo.h>
#include <SoftwareSerial.h>

// ---------- Bluetooth ----------
SoftwareSerial btSerial(2, 3); // RX, TX

// Holds a command that is still arriving (defined up here so the Arduino IDE's
// automatic function declarations can see it)
struct LineReader {
  char buf[16];
  byte len;
  unsigned long last;   // when the last character of an unfinished text command arrived
};

// ---------- Car (L298N) ----------
// Turning style
//   true  = gentle: one side drives forward, the other side stops. Needs much
//           less power, so the car can turn on the ground with small motors.
//   false = spin on the spot like the original Code.ino: one side forward,
//           the other side backward (needs a lot of power on the ground).
// Both turn the car in the same direction as the original L and R.
const bool GENTLE_TURNS = true;

// If the L button turns the car right (and R turns it left), set this to true.
const bool SWAP_TURNS = false;

const int IN1 = 7;
const int IN2 = 6;
const int IN3 = 5;
const int IN4 = 4;

// ---------- Arm ----------
Servo baseServo;
Servo shoulderServo;
Servo elbowServo;
Servo gripperServo;

const int BASE_PIN = 9;
const int SHOULDER_PIN = 10;
const int ELBOW_PIN = 11;
const int GRIPPER_PIN = 8;

// Target angle of each joint (where it is going). The servos glide there.
int basePos = 90;
int shoulderPos = 90;
int elbowPos = 90;
int gripperPos = 90;

// ---------- Arm moves ----------
// Base, shoulder and elbow move ARM_STEP degrees per button PRESS.
// The gripper goes fully open (O) or fully closed (C) in one press.
// Holding a button counts as one press (the app repeats the letter while held).
const int ARM_STEP = 45;
const unsigned long PRESS_GAP_MS = 300;   // letters closer together than this = same press

// ---------- SAFE LIMITS (protect the wooden arm) ----------
// No button or typed command can move a joint outside these angles.
// If a joint still buzzes or hits something at a limit, move that limit
// 10-15 degrees closer to 90. Home (90) must stay inside every range.
const int BASE_MIN = 0,       BASE_MAX = 180;
const int SHOULDER_MIN = 45,  SHOULDER_MAX = 135;
const int ELBOW_MIN = 0,      ELBOW_MAX = 180;   // elbow limit removed: full servo range
const int GRIP_CLOSED = 40;   // gripper fully closed
const int GRIP_OPEN = 130;    // gripper fully open

// ---------- Base servo type ----------
// true  = the base is a 360-degree CONTINUOUS servo (it keeps spinning instead of
//         stopping at an angle). The code then keeps track of the base angle
//         itself from how long it has turned, stops after each 45-degree step and
//         never lets it go below BASE_MIN or above BASE_MAX (0-180 = half a turn).
// false = a normal 180-degree servo that moves to an angle and holds it.
const bool BASE_IS_CONTINUOUS = true;
const int BASE_SPIN_SPEED = 8;         // turning speed: 90 +/- this (smaller = slower, 5-40)
// How fast the base turns in each direction (degrees per second). Cheap 360
// servos are often faster one way than the other, so measure both with "spin".
const float BASE_DEG_PER_SEC_LEFT = 120.0;    // speed when turning left  (A / arrow left)
const float BASE_DEG_PER_SEC_RIGHT = 120.0;   // speed when turning right (Z / arrow right)
const bool BASE_REVERSE = false;       // true if the base turns the wrong way
const unsigned long BASE_MAX_SPIN_MS = 1000;   // safety: one move never turns longer than this
// IMPORTANT for the continuous base: point the arm straight ahead (the middle)
// before switching on. That position counts as 90 degrees.

// ---------- Smooth movement ----------
// Every SWEEP_MS each servo moves SWEEP_DEG closer to its target, so a 45 degree
// move takes about a third of a second instead of a sudden jump.
const int SWEEP_DEG = 2;
const unsigned long SWEEP_MS = 15;

// ---------- Power bank keep-alive ----------
// Many power banks switch off when the load is small. Every few seconds the
// Arduino switches on a 47 ohm resistor for a moment so the power bank sees
// about 100 mA and stays on. Set KEEPALIVE_ENABLED to false if you don't use it.
const bool KEEPALIVE_ENABLED = true;
const int KEEPALIVE_PIN = A4;
const unsigned long KEEPALIVE_EVERY_MS = 3000;   // start a pulse this often
const unsigned long KEEPALIVE_PULSE_MS = 500;    // each pulse lasts this long
// Set to true to keep the load on all the time (about 100 mA extra). Works with
// any power bank, and is the easiest way to check the circuit: the 47 ohm
// resistor should get warm within a minute. Use a 1 W resistor for this mode.
const bool KEEPALIVE_ALWAYS_ON = false;
unsigned long keepAliveStart = 0;
bool keepAliveOn = false;

// ---------- Text going to both the Serial Monitor and the phone ----------
// Only used for rare messages (help, status, home). Sending over SoftwareSerial
// blocks receiving, so frequent replies would garble incoming commands.
void say(const __FlashStringHelper *text) { Serial.print(text); btSerial.print(text); }
void sayLine(const __FlashStringHelper *text) { Serial.println(text); btSerial.println(text); }
void sayNumber(int n) { Serial.println(n); btSerial.println(n); }

// ---------- Text going to the Serial Monitor only (frequent messages) ----------
void note(const __FlashStringHelper *text) { Serial.print(text); }
void noteLine(int n) { Serial.println(n); }

// =====================================================================
// CAR
// =====================================================================
void setMotors(int a, int b, int c, int d) {
  digitalWrite(IN1, a);
  digitalWrite(IN2, b);
  digitalWrite(IN3, c);
  digitalWrite(IN4, d);
}

void carCommand(char c) {
  char received = c;             // the letter as sent, for the Serial Monitor
  if (SWAP_TURNS) {
    if (c == 'L') c = 'R';
    else if (c == 'R') c = 'L';
  }
  switch (c) {
    case 'F': setMotors(HIGH, LOW, HIGH, LOW); break; // Move Forward
    case 'B': setMotors(LOW, HIGH, LOW, HIGH); break; // Move Backward
    case 'R':                                         // Turn (original 'R')
      if (GENTLE_TURNS) setMotors(LOW, LOW, HIGH, LOW);   // side B forward, side A stopped
      else              setMotors(LOW, HIGH, HIGH, LOW);  // original: A backward, B forward
      break;
    case 'L':                                         // Turn (original 'L')
      if (GENTLE_TURNS) setMotors(HIGH, LOW, LOW, LOW);   // side A forward, side B stopped
      else              setMotors(HIGH, LOW, LOW, HIGH);  // original: A forward, B backward
      break;
    case 'S': setMotors(LOW, LOW, LOW, LOW);   break; // Stop
  }
  // Confirm the command in the Serial Monitor, only when it changes
  // (the app repeats a held command many times a second)
  static char lastCar = 0;
  if (received != lastCar) {
    lastCar = received;
    Serial.print(F("Got "));
    Serial.println(received);
  }
}

// =====================================================================
// ARM
// =====================================================================
// Sets a joint's target, always kept inside that joint's safe limits
void moveServo(int &pos, int angle, int minA, int maxA, const __FlashStringHelper *name) {
  int safe = constrain(angle, minA, maxA);
  pos = safe;
  note(name);                 // Serial Monitor only, so Bluetooth stays free for commands
  note(F(" -> "));
  noteLine(safe);
  if (safe != angle) {
    note(F("  (limited to the safe range)"));
    noteLine(safe);
  }
}

void moveBase(int a)     { moveServo(basePos, a, BASE_MIN, BASE_MAX, F("Base")); }
void moveShoulder(int a) { moveServo(shoulderPos, a, SHOULDER_MIN, SHOULDER_MAX, F("Shoulder")); }
void moveElbow(int a)    { moveServo(elbowPos, a, ELBOW_MIN, ELBOW_MAX, F("Elbow")); }
void moveGripper(int a)  { moveServo(gripperPos, a, GRIP_CLOSED, GRIP_OPEN, F("Gripper")); }

void moveToHome() {
  sayLine(F("Moving to home position (all 90)"));
  basePos = shoulderPos = elbowPos = gripperPos = 90;   // servos glide there
}

// Glides every servo toward its target (called all the time from loop)
Servo *servos[4] = {&baseServo, &shoulderServo, &elbowServo, &gripperServo};
int *targets[4] = {&basePos, &shoulderPos, &elbowPos, &gripperPos};
int current[4] = {90, 90, 90, 90};
unsigned long lastSweep = 0;

// ---------- Continuous base: turn for a calculated time, then stop ----------
float baseEstimate = 90;          // where the code thinks the base is (degrees)
bool baseSpinning = false;
unsigned long baseSpinStart = 0, baseSpinMs = 0;
int baseSpinTarget = 90;

// Stopping: a continuous servo's "stop" value is often not exactly 90, so it
// would keep creeping. Switching its signal off (detach) always stops it.
void baseStop() {
  baseServo.write(90);
  baseServo.detach();
}

void baseRun(int dir) {
  if (!baseServo.attached()) baseServo.attach(BASE_PIN);
  baseServo.write(90 + dir * BASE_SPIN_SPEED);
}

void updateBase() {
  if (!BASE_IS_CONTINUOUS) return;
  unsigned long now = millis();
  if (baseSpinning) {
    if (now - baseSpinStart < baseSpinMs) return;       // still turning
    baseStop();
    baseSpinning = false;
    baseEstimate = baseSpinTarget;                       // arrived
    note(F("Base at about "));
    noteLine(baseSpinTarget);
  }
  int diff = basePos - (int)(baseEstimate + 0.5);
  if (diff > -2 && diff < 2) return;                     // already there
  int dir = (diff > 0) ? 1 : -1;
  if (BASE_REVERSE) dir = -dir;
  float speed = (diff > 0) ? BASE_DEG_PER_SEC_LEFT : BASE_DEG_PER_SEC_RIGHT;
  baseSpinMs = (unsigned long)(abs(diff) / speed * 1000.0);
  if (baseSpinMs > BASE_MAX_SPIN_MS) {                   // safety cap: move only part of the way
    baseSpinMs = BASE_MAX_SPIN_MS;
    baseSpinTarget = (int)(baseEstimate + (diff > 0 ? 1 : -1) * speed * BASE_MAX_SPIN_MS / 1000.0 + 0.5);
  } else {
    baseSpinTarget = basePos;
  }
  baseSpinStart = now;
  baseSpinning = true;
  baseRun(dir);
}

// "spin" command: turns the base LEFT for exactly 2 seconds, pauses, then
// RIGHT for exactly 2 seconds, so you can measure both speeds:
//   BASE_DEG_PER_SEC_LEFT  = degrees it turned left  / 2
//   BASE_DEG_PER_SEC_RIGHT = degrees it turned right / 2
void baseCalibrate() {
  if (!BASE_IS_CONTINUOUS) { sayLine(F("spin is only for a continuous base")); return; }
  sayLine(F("1) LEFT for 2 s - note how many degrees"));
  baseRun(BASE_REVERSE ? -1 : 1);
  delay(2000);
  baseStop();
  delay(3000);
  sayLine(F("2) RIGHT for 2 s - note how many degrees"));
  baseRun(BASE_REVERSE ? 1 : -1);
  delay(2000);
  baseStop();
  sayLine(F("Set BASE_DEG_PER_SEC_LEFT / _RIGHT = degrees / 2, upload again"));
  sayLine(F("Then point the arm straight ahead and press reset"));
}

void updateServos() {
  if (millis() - lastSweep < SWEEP_MS) return;
  lastSweep = millis();
  for (int i = (BASE_IS_CONTINUOUS ? 1 : 0); i < 4; i++) {   // continuous base is handled by updateBase()
    int diff = *targets[i] - current[i];
    if (diff == 0) continue;
    if (diff > SWEEP_DEG) diff = SWEEP_DEG;
    if (diff < -SWEEP_DEG) diff = -SWEEP_DEG;
    current[i] += diff;
    servos[i]->write(current[i]);
  }
}

void printStatus() {
  sayLine(F("--- Current Position ---"));
  say(F("Base: "));     sayNumber(basePos);
  say(F("Shoulder: ")); sayNumber(shoulderPos);
  say(F("Elbow: "));    sayNumber(elbowPos);
  say(F("Gripper: "));  sayNumber(gripperPos);
  sayLine(F("------------------------"));
}

void printInstructions() {
  sayLine(F("=== Commands ==="));
  sayLine(F("CAR: F forward, B backward, L / R turn, S stop"));
  sayLine(F("ARM: A/Z base, U/J shoulder, I/K elbow (45 per press)"));
  sayLine(F("     O gripper fully open, C fully closed, H home"));
  sayLine(F("     or b90 s45 e120 g60 home open close status help + Enter"));
  sayLine(F("Safe limits are set at the top of the code."));
  if (BASE_IS_CONTINUOUS) sayLine(F("Continuous base: type spin + Enter to measure its speed"));
}

bool isNumber(const char *s) {
  if (*s == 0) return false;
  for (; *s; s++) if (*s < '0' || *s > '9') return false;
  return true;
}

void armCommand(char *cmd) {
  for (char *p = cmd; *p; p++) if (*p >= 'A' && *p <= 'Z') *p += 32;   // lowercase

  // Whole words first (so "status" is not read as shoulder "s...")
  if (strcmp(cmd, "home") == 0)        moveToHome();
  else if (strcmp(cmd, "open") == 0)   moveGripper(GRIP_OPEN);
  else if (strcmp(cmd, "close") == 0)  moveGripper(GRIP_CLOSED);
  else if (strcmp(cmd, "status") == 0) printStatus();
  else if (strcmp(cmd, "spin") == 0)   baseCalibrate();
  else if (strcmp(cmd, "help") == 0)   printInstructions();
  // Then letter + angle
  else if (isNumber(cmd + 1)) {
    int angle = atoi(cmd + 1);
    switch (cmd[0]) {
      case 'b': moveBase(angle); break;
      case 's': moveShoulder(angle); break;
      case 'e': moveElbow(angle); break;
      case 'g': moveGripper(angle); break;
      default: sayLine(F("Unknown command. Type 'help' for instructions."));
    }
  }
  else sayLine(F("Unknown command. Type 'help' for instructions."));
}

// One arm action per button PRESS: the app repeats a held letter every
// 0.15 s, so repeats that arrive close together are ignored.
// 0 = base, 1 = shoulder, 2 = elbow, 3 = gripper
unsigned long lastLetter[4] = {0, 0, 0, 0};
bool newPress(int joint) {
  unsigned long now = millis();
  bool fresh = (now - lastLetter[joint] > PRESS_GAP_MS);
  lastLetter[joint] = now;
  return fresh;
}

// Returns true if the letter was a car or arm command
bool letterCommand(char c) {
  switch (c) {
    // --- car ---
    case 'F': case 'B': case 'L': case 'R': case 'S':
      carCommand(c); return true;
    // --- arm ---
    case 'A': if (newPress(0)) moveBase(basePos + ARM_STEP);          return true;  // base left
    case 'Z': if (newPress(0)) moveBase(basePos - ARM_STEP);          return true;  // base right
    case 'U': if (newPress(1)) moveShoulder(shoulderPos + ARM_STEP);  return true;  // shoulder up
    case 'J': if (newPress(1)) moveShoulder(shoulderPos - ARM_STEP);  return true;  // shoulder down
    case 'I': if (newPress(2)) moveElbow(elbowPos + ARM_STEP);        return true;  // elbow up
    case 'K': if (newPress(2)) moveElbow(elbowPos - ARM_STEP);        return true;  // elbow down
    case 'O': if (newPress(3)) moveGripper(GRIP_OPEN);                return true;  // gripper fully open
    case 'C': if (newPress(3)) moveGripper(GRIP_CLOSED);              return true;  // gripper fully closed
    case 'H': moveToHome();                             return true;  // arm home
  }
  return false;
}

// =====================================================================
// COMMAND READING (non-blocking, for both Serial Monitor and Bluetooth)
// A capital command letter ALWAYS acts straight away (and throws away any
// half-finished text, e.g. a garbled byte), so the controls can never get stuck.
// Lowercase text is collected until Enter and sent to the arm.
// =====================================================================
LineReader usbLine = {{0}, 0, 0};
LineReader btLine = {{0}, 0, 0};
const unsigned long TEXT_TIMEOUT_MS = 2000;   // unfinished text is dropped after 2 s

void feed(LineReader &r, char c) {
  if (c >= 'A' && c <= 'Z') {
    r.len = 0;                 // drop any unfinished text or junk
    letterCommand(c);          // unknown capitals are ignored
    return;
  }
  if (c == '\n' || c == '\r') {
    if (r.len > 0) {
      r.buf[r.len] = 0;
      armCommand(r.buf);
      r.len = 0;
    }
    return;
  }
  if (c <= ' ' || c > '~') return;            // ignore spaces and garbled bytes
  if (r.len >= sizeof(r.buf) - 1) r.len = 0;  // too long: must be junk, start again
  r.buf[r.len++] = c;
  r.last = millis();
}

// Power bank keep-alive pulse (non-blocking, so commands are never delayed)
void keepAlive() {
  if (!KEEPALIVE_ENABLED) return;
  if (KEEPALIVE_ALWAYS_ON) {
    digitalWrite(KEEPALIVE_PIN, HIGH);
    return;
  }
  unsigned long now = millis();
  if (!keepAliveOn && now - keepAliveStart >= KEEPALIVE_EVERY_MS) {
    digitalWrite(KEEPALIVE_PIN, HIGH);
    keepAliveOn = true;
    keepAliveStart = now;
  } else if (keepAliveOn && now - keepAliveStart >= KEEPALIVE_PULSE_MS) {
    digitalWrite(KEEPALIVE_PIN, LOW);
    keepAliveOn = false;
  }
}

// Throw away text that never got its Enter
void expire(LineReader &r) {
  if (r.len > 0 && millis() - r.last > TEXT_TIMEOUT_MS) r.len = 0;
}

// =====================================================================
void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);

  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  setMotors(LOW, LOW, LOW, LOW);

  baseServo.attach(BASE_PIN);
  shoulderServo.attach(SHOULDER_PIN);
  elbowServo.attach(ELBOW_PIN);
  gripperServo.attach(GRIPPER_PIN);
  for (int i = 0; i < 4; i++) servos[i]->write(current[i]);   // start at 90
  if (BASE_IS_CONTINUOUS) baseStop();                          // continuous base: signal off = stopped

  pinMode(KEEPALIVE_PIN, OUTPUT);
  digitalWrite(KEEPALIVE_PIN, LOW);

  sayLine(F("Car + 4-DOF Arm - Initialized"));
  moveToHome();
  printInstructions();
}

void loop() {
  while (btSerial.available() > 0) feed(btLine, btSerial.read());
  while (Serial.available() > 0)   feed(usbLine, Serial.read());
  expire(btLine);
  expire(usbLine);
  keepAlive();
  updateServos();
  updateBase();
}

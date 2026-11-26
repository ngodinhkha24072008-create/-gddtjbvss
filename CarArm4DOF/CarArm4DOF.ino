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

int basePos = 90;
int shoulderPos = 90;
int elbowPos = 90;
int gripperPos = 90;

const int STEP = 5;            // degrees per arm command (smaller = smoother)

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
const int MIN_ANGLE = 0;
const int MAX_ANGLE = 180;

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
  if (c != lastCar) {
    lastCar = c;
    Serial.print(F("Got "));
    Serial.println(c);
  }
}

// =====================================================================
// ARM
// =====================================================================
bool moveServo(Servo &servo, int &pos, int angle, const __FlashStringHelper *name) {
  if (angle < MIN_ANGLE || angle > MAX_ANGLE) {
    sayLine(F("Error: Angle out of range (0-180)"));
    return false;
  }
  pos = angle;
  servo.write(pos);
  note(name);                 // Serial Monitor only, so Bluetooth stays free for commands
  note(F(" moved to: "));
  noteLine(pos);
  return true;
}

void moveBase(int a)     { moveServo(baseServo, basePos, a, F("Base")); }
void moveShoulder(int a) { moveServo(shoulderServo, shoulderPos, a, F("Shoulder")); }
void moveElbow(int a)    { moveServo(elbowServo, elbowPos, a, F("Elbow")); }
void moveGripper(int a)  { moveServo(gripperServo, gripperPos, a, F("Gripper")); }

void moveToHome() {
  sayLine(F("Moving to home position..."));
  basePos = shoulderPos = elbowPos = gripperPos = 90;
  baseServo.write(basePos);         delay(300);
  shoulderServo.write(shoulderPos); delay(300);
  elbowServo.write(elbowPos);       delay(300);
  gripperServo.write(gripperPos);   delay(300);
  sayLine(F("Home position reached (all at 90)"));
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
  sayLine(F("ARM: A/Z base, U/J shoulder, I/K elbow, O/C gripper, H home"));
  sayLine(F("     or b90 s45 e120 g30 home open close status help + Enter"));
  sayLine(F("Angle range: 0-180 degrees"));
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
  else if (strcmp(cmd, "open") == 0)   moveGripper(180);
  else if (strcmp(cmd, "close") == 0)  moveGripper(30);
  else if (strcmp(cmd, "status") == 0) printStatus();
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

// One-letter arm commands (like the wheel letters)
void nudge(void (*move)(int), int pos, int delta) {
  move(constrain(pos + delta, MIN_ANGLE, MAX_ANGLE));
}

// Returns true if the letter was a car or arm command
bool letterCommand(char c) {
  switch (c) {
    // --- car ---
    case 'F': case 'B': case 'L': case 'R': case 'S':
      carCommand(c); return true;
    // --- arm ---
    case 'A': nudge(moveBase, basePos, +STEP);          return true;  // base left
    case 'Z': nudge(moveBase, basePos, -STEP);          return true;  // base right
    case 'U': nudge(moveShoulder, shoulderPos, +STEP);  return true;  // shoulder up
    case 'J': nudge(moveShoulder, shoulderPos, -STEP);  return true;  // shoulder down
    case 'I': nudge(moveElbow, elbowPos, +STEP);        return true;  // elbow up
    case 'K': nudge(moveElbow, elbowPos, -STEP);        return true;  // elbow down
    case 'O': nudge(moveGripper, gripperPos, +STEP);    return true;  // gripper open
    case 'C': nudge(moveGripper, gripperPos, -STEP);    return true;  // gripper close
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
}

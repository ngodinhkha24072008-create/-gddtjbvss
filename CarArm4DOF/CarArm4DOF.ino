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
 */

#include <Servo.h>
#include <SoftwareSerial.h>

// ---------- Bluetooth ----------
SoftwareSerial btSerial(2, 3); // RX, TX

// ---------- Car (L298N) ----------
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
const int MIN_ANGLE = 0;
const int MAX_ANGLE = 180;

// ---------- Text going to both the Serial Monitor and the phone ----------
void say(const __FlashStringHelper *text) { Serial.print(text); btSerial.print(text); }
void sayLine(const __FlashStringHelper *text) { Serial.println(text); btSerial.println(text); }
void sayNumber(int n) { Serial.println(n); btSerial.println(n); }

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
    case 'R': setMotors(LOW, HIGH, HIGH, LOW); break; // Turn (original 'R')
    case 'L': setMotors(HIGH, LOW, LOW, HIGH); break; // Turn (original 'L')
    case 'S': setMotors(LOW, LOW, LOW, LOW);   break; // Stop
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
  say(name);
  say(F(" moved to: "));
  sayNumber(pos);
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
// A capital command letter at the start of a line acts straight away.
// Anything else (lowercase text) is collected until Enter and sent to the arm.
// =====================================================================
struct LineReader {
  char buf[16];
  byte len;
};
LineReader usbLine = {{0}, 0};
LineReader btLine = {{0}, 0};

void feed(LineReader &r, char c) {
  if (r.len == 0 && c >= 'A' && c <= 'Z') {
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
  if (c == ' ') return;
  if (r.len < sizeof(r.buf) - 1) r.buf[r.len++] = c;
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

  sayLine(F("Car + 4-DOF Arm - Initialized"));
  moveToHome();
  printInstructions();
}

void loop() {
  while (btSerial.available() > 0) feed(btLine, btSerial.read());
  while (Serial.available() > 0)   feed(usbLine, Serial.read());
}

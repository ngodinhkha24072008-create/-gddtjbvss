/*
 * Bluetooth 4WD car + 4-DOF robotic arm (merged)
 *   Car part:  your original Code.ino (F / B / L / R / S)
 *   Arm part:  4-DOF Robotic Arm Control by Aryan Panwar (2024)
 *
 * COMMANDS - from the phone (HC-06) or typed in the Serial Monitor (9600)
 *   CAR (capital letters, act instantly):
 *     F = forward   B = backward   L = turn   R = turn   S = stop
 *   ARM (lowercase, end with Enter / newline):
 *     b<angle>  base       e.g. b90        s<angle>  shoulder   e.g. s45
 *     e<angle>  elbow      e.g. e120       g<angle>  gripper    e.g. g30
 *     home  open  close  status  help
 *
 * PINS (Arduino Uno)
 *   L298N IN1..IN4 -> D7 D6 D5 D4     (same as the original car code)
 *   HC-06 TXD -> D2, RXD -> D3        (SoftwareSerial, D0/D1 stay free for uploading)
 *   Servos: base D9, shoulder D10, elbow D11, gripper D8   (gripper moved from D6)
 *   Buttons: base A0, shoulder A1, elbow A2, gripper A3    (moved from D2-D5)
 *            gamepad style: each button from its pin to GND, no resistors needed
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

const int BASE_BUTTON = A0;
const int SHOULDER_BUTTON = A1;
const int ELBOW_BUTTON = A2;
const int GRIPPER_BUTTON = A3;

int basePos = 90;
int shoulderPos = 90;
int elbowPos = 90;
int gripperPos = 90;

const int STEP = 10;
const int MIN_ANGLE = 0;
const int MAX_ANGLE = 180;
const unsigned long BUTTON_REPEAT_MS = 200;
const int BUTTON_PRESSED = LOW;   // gamepad-style: pressed button connects the pin to GND
unsigned long lastButton[4] = {0, 0, 0, 0};

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
  sayLine(F("ARM: b<angle> base, s<angle> shoulder, e<angle> elbow, g<angle> gripper"));
  sayLine(F("     home, open, close, status, help   (lowercase, then Enter)"));
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

void checkButtons() {
  const int pins[4] = {BASE_BUTTON, SHOULDER_BUTTON, ELBOW_BUTTON, GRIPPER_BUTTON};
  for (int i = 0; i < 4; i++) {
    if (digitalRead(pins[i]) != BUTTON_PRESSED) continue;
    if (millis() - lastButton[i] < BUTTON_REPEAT_MS) continue;  // held: repeat every 200 ms
    lastButton[i] = millis();

    if (i == 0) moveBase(constrain(basePos + STEP, MIN_ANGLE, MAX_ANGLE));
    if (i == 1) moveShoulder(constrain(shoulderPos + STEP, MIN_ANGLE, MAX_ANGLE));
    if (i == 2) moveElbow(constrain(elbowPos + STEP, MIN_ANGLE, MAX_ANGLE));
    if (i == 3) moveGripper(gripperPos > 100 ? 30 : 180);       // toggle open / close
  }
}

// =====================================================================
// COMMAND READING (non-blocking, for both Serial Monitor and Bluetooth)
// Capital F/B/L/R/S at the start of a line drive the car straight away.
// Anything else is collected until Enter and then sent to the arm.
// =====================================================================
struct LineReader {
  char buf[16];
  byte len;
};
LineReader usbLine = {{0}, 0};
LineReader btLine = {{0}, 0};

void feed(LineReader &r, char c) {
  if (r.len == 0 && (c == 'F' || c == 'B' || c == 'L' || c == 'R' || c == 'S')) {
    carCommand(c);
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

  // Gamepad-style buttons: each button between its pin and GND, no resistors.
  // The Arduino's built-in pull-up keeps the pin HIGH; pressing pulls it LOW.
  pinMode(BASE_BUTTON, INPUT_PULLUP);
  pinMode(SHOULDER_BUTTON, INPUT_PULLUP);
  pinMode(ELBOW_BUTTON, INPUT_PULLUP);
  pinMode(GRIPPER_BUTTON, INPUT_PULLUP);

  sayLine(F("Car + 4-DOF Arm - Initialized"));
  moveToHome();
  printInstructions();
}

void loop() {
  while (btSerial.available() > 0) feed(btLine, btSerial.read());
  while (Serial.available() > 0)   feed(usbLine, Serial.read());
  checkButtons();
}

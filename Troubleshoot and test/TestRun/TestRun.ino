/*
 * TEST RUN - drives the whole robot through a fixed sequence, no phone needed.
 * Same pins and settings as CarArm4DOF.ino.
 *
 *   1. Car: forward, backward, turn left, turn right
 *   2. Arm: base, shoulder, elbow, gripper - each moves and comes back
 *   3. Arm home (all 90), car stopped. Press RESET to run it again.
 *
 * Open Tools > Serial Monitor at 9600 to see each step as it happens.
 * Put the car on the floor with space around it (or lift it for a first try).
 * The Arduino's L LED is on while the wheels should be turning.
 */

#include <Servo.h>

// ---------- Settings (same as CarArm4DOF.ino) ----------
const bool UPSIDE_DOWN = true;    // car runs upside down
const bool GENTLE_TURNS = true;   // turn with one side, the other side stopped
const bool SWAP_TURNS = false;    // true if L turns the car right

const unsigned long DRIVE_MS = 2000;   // how long each drive step lasts
const unsigned long TURN_MS = 1500;    // how long each turn lasts
const unsigned long PAUSE_MS = 1000;   // stop between steps

// ---------- Pins (same as CarArm4DOF.ino) ----------
const int IN1 = 7, IN2 = 6, IN3 = 5, IN4 = 4;
const int BASE_PIN = 9, SHOULDER_PIN = 10, ELBOW_PIN = 11, GRIPPER_PIN = 8;

Servo baseServo, shoulderServo, elbowServo, gripperServo;
int basePos = 90, shoulderPos = 90, elbowPos = 90, gripperPos = 90;

// ================= CAR =================
void setMotors(int a, int b, int c, int d) {
  if (UPSIDE_DOWN) {
    int t = a; a = b; b = t;
    t = c; c = d; d = t;
  }
  digitalWrite(IN1, a);
  digitalWrite(IN2, b);
  digitalWrite(IN3, c);
  digitalWrite(IN4, d);
}

void car(char c) {
  if (SWAP_TURNS) {
    if (c == 'L') c = 'R';
    else if (c == 'R') c = 'L';
  }
  switch (c) {
    case 'F': setMotors(HIGH, LOW, HIGH, LOW); break;
    case 'B': setMotors(LOW, HIGH, LOW, HIGH); break;
    case 'L':
      if (GENTLE_TURNS) setMotors(HIGH, LOW, LOW, LOW);
      else              setMotors(HIGH, LOW, LOW, HIGH);
      break;
    case 'R':
      if (GENTLE_TURNS) setMotors(LOW, LOW, HIGH, LOW);
      else              setMotors(LOW, HIGH, HIGH, LOW);
      break;
    default: setMotors(LOW, LOW, LOW, LOW); break;   // stop
  }
}

void driveStep(char c, unsigned long ms, const __FlashStringHelper *what) {
  Serial.print(F("  CAR  "));
  Serial.println(what);
  digitalWrite(LED_BUILTIN, HIGH);
  car(c);
  delay(ms);
  car('S');
  digitalWrite(LED_BUILTIN, LOW);
  delay(PAUSE_MS);
}

// ================= ARM =================
// Moves a servo slowly (2 degrees every 20 ms) so the arm doesn't jerk
void sweep(Servo &s, int &pos, int target) {
  target = constrain(target, 0, 180);
  while (pos != target) {
    int step = target - pos;
    if (step > 2) step = 2;          // at most 2 degrees per move
    if (step < -2) step = -2;
    pos += step;
    s.write(pos);
    delay(20);
  }
}

void armStep(Servo &s, int &pos, int a, int b, const __FlashStringHelper *what) {
  Serial.print(F("  ARM  "));
  Serial.println(what);
  sweep(s, pos, a);
  delay(300);
  sweep(s, pos, b);
  delay(300);
  sweep(s, pos, 90);
  delay(PAUSE_MS);
}

// ================= RUN =================
void setup() {
  Serial.begin(9600);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  car('S');

  baseServo.attach(BASE_PIN);
  shoulderServo.attach(SHOULDER_PIN);
  elbowServo.attach(ELBOW_PIN);
  gripperServo.attach(GRIPPER_PIN);
  baseServo.write(basePos);
  shoulderServo.write(shoulderPos);
  elbowServo.write(elbowPos);
  gripperServo.write(gripperPos);

  Serial.println(F("=== TEST RUN ==="));
  Serial.println(F("Starting in 3 seconds - clear the area!"));
  delay(3000);

  Serial.println(F("1) CAR"));
  driveStep('F', DRIVE_MS, F("forward"));
  driveStep('B', DRIVE_MS, F("backward"));
  driveStep('L', TURN_MS, F("turn left"));
  driveStep('R', TURN_MS, F("turn right"));

  Serial.println(F("2) ARM"));
  armStep(baseServo, basePos, 45, 135, F("base: left, right, centre"));
  armStep(shoulderServo, shoulderPos, 60, 120, F("shoulder: down, up, centre"));
  armStep(elbowServo, elbowPos, 60, 120, F("elbow: down, up, centre"));
  armStep(gripperServo, gripperPos, 150, 40, F("gripper: open, close, centre"));

  Serial.println(F("3) DONE - arm home, car stopped. Press RESET to run again."));
}

void loop() {
  // nothing - the test runs once in setup()
}

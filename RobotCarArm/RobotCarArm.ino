// Combined sketch: Bluetooth/Serial robot car + 3-servo record & playback robot arm
// Robot arm: original code by Ryan Chan, updated by MilesPeterson101.
// Robot car: serial command motor control (F, B, L, R, S).
//
// Both parts run at the same time. The arm code no longer uses long delay() calls,
// so the car still answers drive commands right away while the arm is moving or
// playing back saved positions.
//
// ---------------- PIN MAP (Arduino Uno / Nano) ----------------
//  Car motor driver : D7, D6, D5, D4        (same as the original car code)
//  Arm servos       : D10, D11, D9          (servo1, servo2 MOVED from D5, D6)
//  LEDs 1..5        : D2, D3, A3, A4, D8    (LED3, LED4 MOVED from D4, D7)
//  Buttons          : D12 (save), D13 (play/stop)
//  Potentiometers   : A0, A1, A2
//  Serial / Bluetooth module : D0 (RX), D1 (TX)
// ---------------------------------------------------------------
//
// ------------- "Arduino Bluetooth RC Car" APP BUTTON MAP -------------
//  App button            Sends     Does
//  Up arrow              F         Drive forward
//  Down arrow            B         Drive backward
//  Left arrow            L         Turn left
//  Right arrow           R         Turn right
//  Up + Left             G         Forward-left  (right motor only)
//  Up + Right            I         Forward-right (left motor only)
//  Down + Left           H         Back-left
//  Down + Right          J         Back-right
//  (release arrows)      S         Stop driving
//  Front lights toggle   W / w     ARM: save current position (each tap = next slot)
//  Back lights toggle    U / u     ARM: U = start playback, u = stop playback
//  Extra toggle          X / x     ARM: clear all saved positions (LEDs off)
//  Horn                  V / v     (not used)
//  Speed slider          0-9, q    ARM: hand/gripper (0 = closed ... q = fully open)
//  Stop-all / disconnect D         Stop driving AND stop arm playback
//  The physical buttons on D12 / D13 still work as before.
// ---------------------------------------------------------------------
//
// ------------- Rover Arm Gamepad (controller/index.html) -------------
//  Joystick     -> F B L R G I H J, release = S
//  Triangle     -> W  (save position)      Circle -> U (play)
//  Square       -> X  (clear positions)    Cross  -> u (stop playback)
//  Hand lever   -> 0-9, q  (forward = open, back = close)   Stop all -> D
//
//  The hand lever moves the gripper servo (servo3 on D9, knob A2 by default).
//  The last control you touch wins: move the lever and the hand follows the
//  lever; turn the A2 knob and the hand follows the knob again.
// ---------------------------------------------------------------------

#include <Servo.h>

// ---------- Car motor pins ----------
const int MOTOR_PIN_1 = 7;
const int MOTOR_PIN_2 = 6;
const int MOTOR_PIN_3 = 5;
const int MOTOR_PIN_4 = 4;

// ---------- Arm pins ----------
const int SERVO_PINS[3] = {10, 11, 9};
const int LED_PINS[5]   = {2, 3, A3, A4, 8};
const int POT_PINS[3]   = {A0, A1, A2};
const int BUTTON_SAVE   = 12;   // was button1
const int BUTTON_PLAY   = 13;   // was button2

// ---------- Timing ----------
const unsigned long DEBOUNCE_MS = 50;
const unsigned long PLAYBACK_STEP_MS = 1050;  // time spent at each saved position

// ---------- Hand / gripper (app lever) ----------
const int GRIPPER = 2;                 // which servo is the hand: 0 = servo1, 1 = servo2, 2 = servo3
const int GRIPPER_CLOSED_ANGLE = 10;   // lever all the way back  (adjust for your gripper)
const int GRIPPER_OPEN_ANGLE   = 170;  // lever all the way forward (adjust for your gripper)
const int KNOB_TAKEOVER_DEG    = 6;    // turn the knob this far to take the hand back from the app

bool appHasGripper = false;            // true while the app lever is controlling the hand
int appGripperAngle = 90;
int knobAngleAtLever = 0;              // knob position when the lever last moved

// ---------- Arm state ----------
Servo servos[3];
int potAngles[3];
int posSaves[5][3] = {{1,1,1},{1,1,1},{1,1,1},{1,1,1},{1,1,1}};
int savedCount = 0;

bool playing = false;
int playIndex = 0;
unsigned long lastStepTime = 0;

// ---------- Button debouncing ----------
struct Button {
  int pin;
  int stableState;
  int lastReading;
  unsigned long lastChange;
};

Button saveButton = {BUTTON_SAVE, LOW, LOW, 0};
Button playButton = {BUTTON_PLAY, LOW, LOW, 0};

// Returns true once when the button goes from released to pressed
bool wasPressed(Button &b) {
  int reading = digitalRead(b.pin);
  if (reading != b.lastReading) {
    b.lastReading = reading;
    b.lastChange = millis();
  }
  if (millis() - b.lastChange > DEBOUNCE_MS && reading != b.stableState) {
    b.stableState = reading;
    return reading == HIGH;
  }
  return false;
}

// ---------- Car ----------
void setMotors(int p1, int p2, int p3, int p4) {
  digitalWrite(MOTOR_PIN_1, p1);
  digitalWrite(MOTOR_PIN_2, p2);
  digitalWrite(MOTOR_PIN_3, p3);
  digitalWrite(MOTOR_PIN_4, p4);
}

// ---------- Arm ----------
void readPots() {
  for (int i = 0; i < 3; i++) {
    potAngles[i] = map(analogRead(POT_PINS[i]), 0, 1023, 0, 179);
  }
}

// The angle a servo should be at when it isn't playing back:
// the knob, or for the hand, the app lever if that was used last.
int liveAngle(int i) {
  if (i == GRIPPER && appHasGripper) return appGripperAngle;
  return potAngles[i];
}

// level: 0 = lever all the way back (closed) ... 10 = all the way forward (open)
void setGripperFromApp(int level) {
  appGripperAngle = map(level, 0, 10, GRIPPER_CLOSED_ANGLE, GRIPPER_OPEN_ANGLE);
  appHasGripper = true;
  knobAngleAtLever = potAngles[GRIPPER];
}

void savePosition() {
  if (savedCount >= 5) return;  // all 5 slots used, same as the original

  for (int i = 0; i < 3; i++) {
    posSaves[savedCount][i] = liveAngle(i);
  }
  digitalWrite(LED_PINS[savedCount], HIGH);
  Serial.print("Pos ");
  Serial.print(savedCount + 1);
  Serial.println(" Saved");
  savedCount++;
}

void playbackStep() {
  if (millis() - lastStepTime < PLAYBACK_STEP_MS) return;
  lastStepTime = millis();

  Serial.println(" potentimeter Angles: ");
  for (int i = 0; i < 3; i++) {
    servos[i].write(posSaves[playIndex][i]);
    Serial.println(posSaves[playIndex][i]);
  }
  playIndex = (playIndex + 1) % savedCount;  // loop through saved positions
}

void startPlayback() {
  if (savedCount == 0) return;     // nothing saved yet
  playing = true;
  playIndex = 0;
  lastStepTime = millis() - PLAYBACK_STEP_MS;  // move to the first position now
}

void stopPlayback() {
  playing = false;                 // servos follow the potentiometers again
}

void clearPositions() {
  stopPlayback();
  savedCount = 0;
  for (int i = 0; i < 5; i++) {
    digitalWrite(LED_PINS[i], LOW);
  }
  Serial.println("Positions cleared");
}

// ---------- Bluetooth app commands ----------
void handleAppCommands() {
  while (Serial.available() > 0) {
    char data = Serial.read();

    switch (data) {
      // --- Driving ---
      case 'F': setMotors(HIGH, LOW, HIGH, LOW); break; // Forward
      case 'B': setMotors(LOW, HIGH, LOW, HIGH); break; // Backward
      case 'L': setMotors(HIGH, LOW, LOW, HIGH); break; // Turn left
      case 'R': setMotors(LOW, HIGH, HIGH, LOW); break; // Turn right
      case 'G': setMotors(HIGH, LOW, LOW, LOW);  break; // Forward-left
      case 'I': setMotors(LOW, LOW, HIGH, LOW);  break; // Forward-right
      case 'H': setMotors(LOW, HIGH, LOW, LOW);  break; // Back-left
      case 'J': setMotors(LOW, LOW, LOW, HIGH);  break; // Back-right
      case 'S': setMotors(LOW, LOW, LOW, LOW);   break; // Stop

      // --- Robot arm ---
      case 'W':                       // Front lights on  -> save position
      case 'w':                       // Front lights off -> save position
        savePosition();
        break;
      case 'U': startPlayback(); break; // Back lights on  -> play
      case 'u': stopPlayback();  break; // Back lights off -> stop
      case 'X':                         // Extra on/off    -> clear saves
      case 'x':
        clearPositions();
        break;

      // --- Stop everything ---
      case 'D':
        setMotors(LOW, LOW, LOW, LOW);
        stopPlayback();
        break;

      // --- Hand lever (app speed slider) ---
      case '0': case '1': case '2': case '3': case '4':
      case '5': case '6': case '7': case '8': case '9':
        setGripperFromApp(data - '0');
        break;
      case 'q':
        setGripperFromApp(10);
        break;

      // Horn (V/v) and anything else are ignored
    }
  }
}

void handleArm() {
  readPots();

  if (wasPressed(saveButton)) {
    savePosition();
  }

  if (wasPressed(playButton)) {
    if (playing) stopPlayback();   // press once to start playback, again to stop
    else startPlayback();
  }

  if (playing) {
    playbackStep();
  } else {
    // Turning the hand knob takes the hand back from the app lever
    if (appHasGripper && abs(potAngles[GRIPPER] - knobAngleAtLever) > KNOB_TAKEOVER_DEG) {
      appHasGripper = false;
    }
    for (int i = 0; i < 3; i++) {
      servos[i].write(liveAngle(i));  // follow the knobs (and the hand lever)
    }
  }
}

// ---------- Main ----------
void setup() {
  Serial.begin(9600);

  pinMode(MOTOR_PIN_1, OUTPUT);
  pinMode(MOTOR_PIN_2, OUTPUT);
  pinMode(MOTOR_PIN_3, OUTPUT);
  pinMode(MOTOR_PIN_4, OUTPUT);
  setMotors(LOW, LOW, LOW, LOW);  // start stopped

  for (int i = 0; i < 3; i++) {
    servos[i].attach(SERVO_PINS[i]);
  }
  for (int i = 0; i < 5; i++) {
    pinMode(LED_PINS[i], OUTPUT);
  }
  pinMode(BUTTON_SAVE, INPUT);
  pinMode(BUTTON_PLAY, INPUT);

  readPots();  // so the first lever command has a real knob position to compare with
}

void loop() {
  handleAppCommands();
  handleArm();
}

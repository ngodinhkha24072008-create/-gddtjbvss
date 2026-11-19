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
// ---------- "Arduino Bluetooth Control" app (broxcode) SETUP ----------
//  Connect to the HC-05 in the app, open the gamepad / arrow-keys screen,
//  and in its settings set each control to send:
//    Up = F   Down = B   Left = L   Right = R   (diagonals, if offered: G I H J)
//    Triangle = W   Circle = U   Cross = u   Square = X   Start or Select = D
//  Buttons & Slider screen: set the slider range to 0 - 10
//    (0 = hand closed, 10 = hand fully open). Multi-digit numbers are fine.
//  The app keeps sending while you hold a direction. When you let go and it
//  stops sending, the car stops by itself after DRIVE_TIMEOUT_MS.
// ---------------------------------------------------------------------
//
// ------------- Rover Arm Gamepad (controller/index.html) -------------
//  Joystick     -> F B L R G I H J, release = S
//  Triangle     -> W  (save position)      Circle -> U (play)
//  Square       -> X  (clear positions)    Cross  -> u (stop playback)
//  Hand lever   -> 0-9, q  (slide right = open, left = close)   Stop all -> D
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
const unsigned long DRIVE_TIMEOUT_MS = 400;   // stop the car if no drive command arrives for this long
const unsigned long NUMBER_IDLE_MS   = 30;    // a slider number is complete after this much quiet

// ---------- Hand / gripper (app lever) ----------
const int GRIPPER = 2;                 // which servo is the hand: 0 = servo1, 1 = servo2, 2 = servo3
const int GRIPPER_CLOSED_ANGLE = 10;   // lever all the way left  (adjust for your gripper)
const int GRIPPER_OPEN_ANGLE   = 170;  // lever all the way right (adjust for your gripper)
const int KNOB_TAKEOVER_DEG    = 6;    // turn the knob this far to take the hand back from the app
const int LEVER_MIN = 0;               // slider value for "fully closed" (set the app slider to match)
const int LEVER_MAX = 10;              // slider value for "fully open"   ('q' also means fully open)

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
bool driving = false;
unsigned long lastDriveCommand = 0;

void setMotors(int p1, int p2, int p3, int p4) {
  digitalWrite(MOTOR_PIN_1, p1);
  digitalWrite(MOTOR_PIN_2, p2);
  digitalWrite(MOTOR_PIN_3, p3);
  digitalWrite(MOTOR_PIN_4, p4);
}

void drive(int p1, int p2, int p3, int p4) {
  setMotors(p1, p2, p3, p4);
  driving = true;
  lastDriveCommand = millis();
}

void stopCar() {
  setMotors(LOW, LOW, LOW, LOW);
  driving = false;
}

// Safety stop: the phone app repeats drive commands while a direction is held
void checkDriveTimeout() {
  if (driving && millis() - lastDriveCommand > DRIVE_TIMEOUT_MS) {
    stopCar();
  }
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

// level: LEVER_MIN = lever all the way left (closed) ... LEVER_MAX = all the way right (open)
void setGripperFromApp(int level) {
  level = constrain(level, LEVER_MIN, LEVER_MAX);
  appGripperAngle = map(level, LEVER_MIN, LEVER_MAX, GRIPPER_CLOSED_ANGLE, GRIPPER_OPEN_ANGLE);
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
// Slider values can be several digits ("10", "7\n"), so digits are collected
// into one number and used once a non-digit arrives or the line goes quiet.
int sliderNumber = -1;               // -1 = no number in progress
unsigned long lastDigitTime = 0;

void finishSliderNumber() {
  if (sliderNumber >= 0) {
    setGripperFromApp(sliderNumber);
    sliderNumber = -1;
  }
}

void handleAppCommands() {
  while (Serial.available() > 0) {
    char data = Serial.read();

    if (data >= '0' && data <= '9') {
      if (sliderNumber < 0) sliderNumber = 0;
      if (sliderNumber < 1000) sliderNumber = sliderNumber * 10 + (data - '0');
      lastDigitTime = millis();
      continue;
    }
    finishSliderNumber();             // any other character ends the number

    switch (data) {
      // --- Driving ---
      case 'F': drive(HIGH, LOW, HIGH, LOW); break; // Forward
      case 'B': drive(LOW, HIGH, LOW, HIGH); break; // Backward
      case 'L': drive(HIGH, LOW, LOW, HIGH); break; // Turn left
      case 'R': drive(LOW, HIGH, HIGH, LOW); break; // Turn right
      case 'G': drive(HIGH, LOW, LOW, LOW);  break; // Forward-left
      case 'I': drive(LOW, LOW, HIGH, LOW);  break; // Forward-right
      case 'H': drive(LOW, HIGH, LOW, LOW);  break; // Back-left
      case 'J': drive(LOW, LOW, LOW, HIGH);  break; // Back-right
      case 'S': stopCar();                   break; // Stop

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
        stopCar();
        stopPlayback();
        break;

      // --- Hand lever: numbers are handled above; 'q' = fully open ---
      case 'q':
        setGripperFromApp(LEVER_MAX);
        break;

      // Horn (V/v), newlines and anything else are ignored
    }
  }

  // A number with nothing after it (e.g. "7" then silence) is complete once the line goes quiet
  if (sliderNumber >= 0 && millis() - lastDigitTime > NUMBER_IDLE_MS) {
    finishSliderNumber();
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
  stopCar();  // start stopped

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
  checkDriveTimeout();
  handleArm();
}

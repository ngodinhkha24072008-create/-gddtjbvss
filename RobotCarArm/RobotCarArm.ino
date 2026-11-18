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

void handleCarCommands() {
  while (Serial.available() > 0) {
    char data = Serial.read();

    switch (data) {
      case 'F': // Move Forward
        setMotors(HIGH, LOW, HIGH, LOW);
        break;
      case 'B': // Move Backward
        setMotors(LOW, HIGH, LOW, HIGH);
        break;
      case 'R': // Turn (labelled "Turn Left" in the original code)
        setMotors(LOW, HIGH, HIGH, LOW);
        break;
      case 'L': // Turn (labelled "Turn Right" in the original code)
        setMotors(HIGH, LOW, LOW, HIGH);
        break;
      case 'S': // Stop
        setMotors(LOW, LOW, LOW, LOW);
        break;
    }
  }
}

// ---------- Arm ----------
void readPots() {
  for (int i = 0; i < 3; i++) {
    potAngles[i] = map(analogRead(POT_PINS[i]), 0, 1023, 0, 179);
  }
}

void savePosition() {
  if (savedCount >= 5) return;  // all 5 slots used, same as the original

  for (int i = 0; i < 3; i++) {
    posSaves[savedCount][i] = potAngles[i];
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

void handleArm() {
  readPots();

  if (wasPressed(saveButton)) {
    savePosition();
  }

  if (wasPressed(playButton) && savedCount > 0) {
    playing = !playing;            // press once to start playback, again to stop
    playIndex = 0;
    lastStepTime = millis() - PLAYBACK_STEP_MS;  // move to the first position now
  }

  if (playing) {
    playbackStep();
  } else {
    for (int i = 0; i < 3; i++) {
      servos[i].write(potAngles[i]);  // follow the potentiometers
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
}

void loop() {
  handleCarCommands();
  handleArm();
}

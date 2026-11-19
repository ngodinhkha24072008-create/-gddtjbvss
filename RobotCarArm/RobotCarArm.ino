// Robot car + robot arm - simplified, low-memory version
// Arm idea by Ryan Chan, updated by MilesPeterson101.
//
// PINS (Arduino Uno / Nano)
//   Motors : D7 D6 D5 D4          Servos : D10 D11 D9 (D9 = hand)
//   Knobs  : A0 A1 A2             LEDs   : D2 D3 A3 A4 D8
//   Buttons: D12 save, D13 play   Bluetooth HC-05: D0 RX, D1 TX, 9600 baud
//
// COMMANDS (one letter each, from the app or gamepad)
//   F B L R = forward, back, left, right    G I H J = diagonals    S = stop
//   W = save arm position   U = play   u = stop playback   X = clear   D = stop all
//   0 ... 10 (or q) = hand lever: 0 closed, 10 open
//   The car stops by itself AUTO_STOP_MS after the last drive command (set 0 to turn off).

#include <Servo.h>

// ----- Settings you may want to change -----
#define HAND        2     // which servo is the hand: 0, 1 or 2
#define HAND_CLOSED 10    // hand angle at lever 0
#define HAND_OPEN   170   // hand angle at lever 10
#define LEVER_MAX   10    // highest number the app slider sends
#define AUTO_STOP_MS 400   // stop the car this long after the last drive command (0 = off: drive until S)

// ----- Pins -----
const byte motorPin[4] = {7, 6, 5, 4};
const byte servoPin[3] = {10, 11, 9};
const byte knobPin[3]  = {A0, A1, A2};
const byte ledPin[5]   = {2, 3, A3, A4, 8};
#define SAVE_BTN 12
#define PLAY_BTN 13

// Drive letters and which motor pins go HIGH (bits = D7 D6 D5 D4)
const char driveKeys[] = "FBLRGIHJ";
const byte drivePattern[] = {0b1010, 0b0101, 0b1001, 0b0110, 0b1000, 0b0010, 0b0100, 0b0001};

Servo servo[3];
byte knob[3];             // knob angles right now
byte saved[5][3];         // 5 saved arm positions
byte savedCount = 0;
bool playing = false;
byte playStep = 0;
int  handAngle = -1;      // -1 = hand follows its knob, otherwise the app lever
byte knobAtLever = 0;
bool driving = false;
int  number = -1;         // slider number being received
bool lastSave = LOW, lastPlay = LOW;
unsigned long lastDrive = 0, lastStep = 0, lastDigit = 0, lastButtons = 0;

void setMotors(byte pattern) {
  for (byte i = 0; i < 4; i++) digitalWrite(motorPin[i], (pattern >> (3 - i)) & 1);
}

void stopCar() {
  setMotors(0);
  driving = false;
}

byte armAngle(byte i) {   // where a servo should be when not playing back
  if (i == HAND && handAngle >= 0) return handAngle;
  return knob[i];
}

void savePosition() {
  if (savedCount >= 5) return;
  for (byte i = 0; i < 3; i++) saved[savedCount][i] = armAngle(i);
  digitalWrite(ledPin[savedCount], HIGH);
  savedCount++;
  Serial.print(F("Saved "));
  Serial.println(savedCount);
}

void startPlayback() {
  if (savedCount == 0) return;
  playing = true;
  playStep = 0;
  lastStep = millis() - 1050;    // go to the first position now
}

void clearPositions() {
  playing = false;
  savedCount = 0;
  for (byte i = 0; i < 5; i++) digitalWrite(ledPin[i], LOW);
  Serial.println(F("Cleared"));
}

void setHand(int level) {
  level = constrain(level, 0, LEVER_MAX);
  handAngle = map(level, 0, LEVER_MAX, HAND_CLOSED, HAND_OPEN);
  knobAtLever = knob[HAND];
}

void finishNumber() {
  if (number >= 0) setHand(number);
  number = -1;
}

void readBluetooth() {
  while (Serial.available()) {
    char c = Serial.read();

    if (c >= '0' && c <= '9') {            // slider numbers can be "10", "7" ...
      if (number < 0) number = 0;
      number = min(number * 10 + (c - '0'), 999);
      lastDigit = millis();
      continue;
    }
    finishNumber();

    const char *p = (c != 0) ? strchr(driveKeys, c) : NULL;
    if (p) {                               // a drive letter
      setMotors(drivePattern[p - driveKeys]);
      driving = true;
      lastDrive = millis();
      continue;
    }

    switch (c) {
      case 'S': stopCar(); break;
      case 'W': case 'w': savePosition(); break;
      case 'U': startPlayback(); break;
      case 'u': playing = false; break;
      case 'X': case 'x': clearPositions(); break;
      case 'D': stopCar(); playing = false; break;
      case 'q': setHand(LEVER_MAX); break;
    }
  }
  if (number >= 0 && millis() - lastDigit > 30) finishNumber();
}

void readButtons() {
  if (millis() - lastButtons < 50) return;  // simple debounce
  lastButtons = millis();
  bool s = digitalRead(SAVE_BTN), p = digitalRead(PLAY_BTN);
  if (s && !lastSave) savePosition();
  if (p && !lastPlay) {
    if (playing) playing = false;
    else startPlayback();
  }
  lastSave = s;
  lastPlay = p;
}

void setup() {
  Serial.begin(9600);
  for (byte i = 0; i < 4; i++) pinMode(motorPin[i], OUTPUT);
  for (byte i = 0; i < 5; i++) pinMode(ledPin[i], OUTPUT);
  for (byte i = 0; i < 3; i++) servo[i].attach(servoPin[i]);
  pinMode(SAVE_BTN, INPUT);
  pinMode(PLAY_BTN, INPUT);
  stopCar();

  // Wiring check: LEDs 1-5 flash once each, in order, at power-on
  for (byte i = 0; i < 5; i++) {
    digitalWrite(ledPin[i], HIGH);
    delay(150);
    digitalWrite(ledPin[i], LOW);
  }
}

void loop() {
  for (byte i = 0; i < 3; i++) knob[i] = map(analogRead(knobPin[i]), 0, 1023, 0, 179);

  readBluetooth();
  readButtons();

  if (AUTO_STOP_MS > 0 && driving && millis() - lastDrive > AUTO_STOP_MS) stopCar();  // auto-stop

  // Turning the hand knob takes the hand back from the app lever
  if (handAngle >= 0 && abs(knob[HAND] - knobAtLever) > 6) handAngle = -1;

  if (playing) {
    if (millis() - lastStep >= 1050) {
      lastStep = millis();
      for (byte i = 0; i < 3; i++) servo[i].write(saved[playStep][i]);
      playStep = (playStep + 1) % savedCount;
    }
  } else {
    for (byte i = 0; i < 3; i++) servo[i].write(armAngle(i));
  }
}

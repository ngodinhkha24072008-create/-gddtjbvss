// Bluetooth 4WD car - built on your SoftwareSerial baseline
//
// The HC-06 talks on pins 2 and 3 (SoftwareSerial), so D0/D1 stay free:
// you can upload without unplugging the Bluetooth module, and the
// Serial Monitor shows every command the phone sends.
//
// WIRING
//   HC-06 TXD -> D2 (Arduino RX)      HC-06 RXD -> D3 (Arduino TX)
//   HC-06 VCC -> 5V                   HC-06 GND -> GND
//   L298N IN1 -> D7, IN2 -> D6  (left motors on OUT1 / OUT2)
//   L298N IN3 -> D5, IN4 -> D4  (right motors on OUT3 / OUT4)
//   L298N GND -> Arduino GND, ENA / ENB jumpers on
//
// COMMANDS (from the phone app, or typed in the Serial Monitor)
//   F = forward   B = backward   L = turn left   R = turn right   S = stop
//   1 = LED on    0 = LED off    (same as your baseline)

#include <SoftwareSerial.h>

SoftwareSerial btSerial(2, 3); // RX, TX
const int ledPin = 13;
char command;

// Motor pins (same as your original car code)
const int IN1 = 7;
const int IN2 = 6;
const int IN3 = 5;
const int IN4 = 4;

void setMotors(int a, int b, int c, int d) {
  digitalWrite(IN1, a);
  digitalWrite(IN2, b);
  digitalWrite(IN3, c);
  digitalWrite(IN4, d);
}

void handleCommand(char c) {
  switch (c) {
    case 'F': setMotors(HIGH, LOW, HIGH, LOW); break;  // Move Forward
    case 'B': setMotors(LOW, HIGH, LOW, HIGH); break;  // Move Backward
    case 'R': setMotors(LOW, HIGH, HIGH, LOW); break;  // Turn (same pins as original 'R')
    case 'L': setMotors(HIGH, LOW, LOW, HIGH); break;  // Turn (same pins as original 'L')
    case 'S': setMotors(LOW, LOW, LOW, LOW);   break;  // Stop
    case '1': digitalWrite(ledPin, HIGH);      break;  // LED on
    case '0': digitalWrite(ledPin, LOW);       break;  // LED off
    default: return;                                   // ignore newlines and anything else
  }
  // Report what was done, to the Serial Monitor and back to the phone
  Serial.print("Got ");
  Serial.println(c);
  btSerial.print("Got ");
  btSerial.println(c);
}

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);

  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  setMotors(LOW, LOW, LOW, LOW);   // start stopped

  Serial.begin(9600);
  btSerial.begin(9600);
  Serial.println("Bluetooth 4WD ready");
}

void loop() {
  // Commands from the phone (HC-06)
  if (btSerial.available() > 0) {
    command = btSerial.read();
    handleCommand(command);
  }

  // Commands typed in the Serial Monitor (for testing without the phone)
  if (Serial.available() > 0) {
    command = Serial.read();
    handleCommand(command);
  }
}

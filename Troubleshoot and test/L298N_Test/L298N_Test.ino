// L298N motor driver test - nothing else (no Bluetooth, no servos)
// Wheels off the ground! It repeats: forward, stop, backward, stop,
// left motor only, right motor only, stop.
//
// Wiring:  IN1 -> D7   IN2 -> D6   IN3 -> D5   IN4 -> D4
//          L298N GND -> Arduino GND AND battery minus
//          Battery plus (7-12 V) -> L298N "12V" terminal
//          ENA and ENB jumper caps ON
// Open Tools > Serial Monitor at 9600 to see each step.
// The Arduino's own LED (L, pin 13) is lit while the motors should be turning.

const byte IN1 = 7, IN2 = 6, IN3 = 5, IN4 = 4;

void motors(byte a, byte b, byte c, byte d, const __FlashStringHelper *name, bool moving) {
  digitalWrite(IN1, a);
  digitalWrite(IN2, b);
  digitalWrite(IN3, c);
  digitalWrite(IN4, d);
  digitalWrite(LED_BUILTIN, moving);
  Serial.println(name);
  delay(2000);
}

void setup() {
  Serial.begin(9600);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.println(F("L298N test starting"));
}

void loop() {
  motors(HIGH, LOW, HIGH, LOW, F("Forward  (both motors)"), true);
  motors(LOW, LOW, LOW, LOW,   F("Stop"), false);
  motors(LOW, HIGH, LOW, HIGH, F("Backward (both motors)"), true);
  motors(LOW, LOW, LOW, LOW,   F("Stop"), false);
  motors(HIGH, LOW, LOW, LOW,  F("Motor A only (OUT1/OUT2)"), true);
  motors(LOW, LOW, HIGH, LOW,  F("Motor B only (OUT3/OUT4)"), true);
  motors(LOW, LOW, LOW, LOW,   F("Stop"), false);
}

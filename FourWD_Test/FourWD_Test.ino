// 4WD car test (L298N + HC-06) - car only, no robot arm
//
// 1) At power-on it runs a self-test, 1 second each:
//    forward, backward, turn left, turn right, then stops.
// 2) Then it drives from Bluetooth: F B L R = drive, S = stop.
//    No auto-stop: the car keeps going until it gets S.
//
// WIRING
//   L298N IN1 -> D7   IN2 -> D6   IN3 -> D5   IN4 -> D4
//   Left motors (2)  -> OUT1 / OUT2      Right motors (2) -> OUT3 / OUT4
//   Battery + (7-12 V) -> L298N 12V      Battery - -> L298N GND
//   L298N GND -> Arduino GND (needed!)   ENA / ENB jumper caps ON
//   HC-06 TXD -> D0   RXD -> D1   VCC -> 5V   GND -> GND
//   Unplug HC-06 TXD/RXD while uploading.
//
// Open Tools > Serial Monitor at 9600 to see each step and every command received.
// If a side spins the wrong way, swap that side's two motor wires on the L298N.

const byte IN1 = 7, IN2 = 6, IN3 = 5, IN4 = 4;

void setMotors(byte a, byte b, byte c, byte d) {
  digitalWrite(IN1, a);
  digitalWrite(IN2, b);
  digitalWrite(IN3, c);
  digitalWrite(IN4, d);
}

void forward()   { setMotors(HIGH, LOW, HIGH, LOW); }
void backward()  { setMotors(LOW, HIGH, LOW, HIGH); }
void turnLeft()  { setMotors(HIGH, LOW, LOW, HIGH); }   // same pins as your original 'L'
void turnRight() { setMotors(LOW, HIGH, HIGH, LOW); }   // same pins as your original 'R'
void stopCar()   { setMotors(LOW, LOW, LOW, LOW); }

void step(void (*move)(), const __FlashStringHelper *name) {
  Serial.println(name);
  digitalWrite(LED_BUILTIN, HIGH);   // Arduino's L LED on while wheels should turn
  move();
  delay(1000);
  stopCar();
  digitalWrite(LED_BUILTIN, LOW);
  delay(500);
}

void setup() {
  Serial.begin(9600);
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  stopCar();

  Serial.println(F("4WD self-test (wheels off the ground!)"));
  delay(2000);
  step(forward,   F("Forward"));
  step(backward,  F("Backward"));
  step(turnLeft,  F("Turn left"));
  step(turnRight, F("Turn right"));
  Serial.println(F("Self-test done. Send F, B, L, R or S."));
}

void loop() {
  if (Serial.available() > 0) {
    char c = Serial.read();
    if (c == '\r' || c == '\n') return;

    Serial.print(F("Got "));
    Serial.println(c);

    switch (c) {
      case 'F': forward();   break;
      case 'B': backward();  break;
      case 'L': turnLeft();  break;
      case 'R': turnRight(); break;
      case 'S': stopCar();   break;
    }
  }
}

// Wheel control - 4WD car with L298N + HC-05/HC-06
//
// Commands from the phone app (one letter each):
//   F = forward      B = backward      L = spin left      R = spin right
//   G = forward-left I = forward-right H = back-left      J = back-right
//   S = stop
// Every command received is echoed back ("Got F") - see it in the app's
// terminal or in Tools > Serial Monitor at 9600.
//
// WIRING
//   L298N IN1 -> D7, IN2 -> D6   (left motors on OUT1 / OUT2)
//   L298N IN3 -> D5, IN4 -> D4   (right motors on OUT3 / OUT4)
//   ENA and ENB jumper caps ON
//   Battery + (7-12 V) -> L298N 12V,  Battery - -> L298N GND
//   L298N GND -> Arduino GND  (needed!)
//   HC-06 TXD -> D0, RXD -> D1, VCC -> 5V, GND -> GND
//   Unplug HC-06 TXD/RXD while uploading.

// ================= SETTINGS =================
// Pins (change these if your wires go to different Arduino pins)
const byte LEFT_A  = 7;   // IN1
const byte LEFT_B  = 6;   // IN2
const byte RIGHT_A = 5;   // IN3
const byte RIGHT_B = 4;   // IN4

// Fix directions here instead of rewiring:
const bool FLIP_LEFT  = false;  // true if the LEFT wheels spin backwards on F
const bool FLIP_RIGHT = false;  // true if the RIGHT wheels spin backwards on F
const bool SWAP_SIDES = false;  // true if L turns the car right (sides swapped)

// Auto-stop: 0 = keep driving until S (like the original code).
// Set e.g. 400 if your app repeats the command while a button is held.
const unsigned long AUTO_STOP_MS = 0;

// Side test at power-on: left side alone, then right side alone, then both.
// Set to false once both sides work.
const bool SIDE_TEST = true;
// ============================================

unsigned long lastCommand = 0;
bool moving = false;

// dir: +1 forward, -1 backward, 0 stop
void setSide(byte pinA, byte pinB, int dir, bool flip) {
  if (flip) dir = -dir;
  digitalWrite(pinA, dir > 0 ? HIGH : LOW);
  digitalWrite(pinB, dir < 0 ? HIGH : LOW);
}

void drive(int left, int right) {
  if (SWAP_SIDES) {
    int t = left;
    left = right;
    right = t;
  }
  setSide(LEFT_A, LEFT_B, left, FLIP_LEFT);
  setSide(RIGHT_A, RIGHT_B, right, FLIP_RIGHT);
  moving = (left != 0 || right != 0);
  lastCommand = millis();
}

// Runs one side (or both) for 1.5 s and says which outputs should be turning
void testStep(int left, int right, const __FlashStringHelper *what) {
  Serial.println(what);
  digitalWrite(LED_BUILTIN, HIGH);
  drive(left, right);
  delay(1500);
  drive(0, 0);
  digitalWrite(LED_BUILTIN, LOW);
  delay(700);
}

void setup() {
  Serial.begin(9600);
  pinMode(LEFT_A, OUTPUT);
  pinMode(LEFT_B, OUTPUT);
  pinMode(RIGHT_A, OUTPUT);
  pinMode(RIGHT_B, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  drive(0, 0);

  if (SIDE_TEST) {
    Serial.println(F("Side test - wheels off the ground!"));
    delay(2000);
    testStep(1, 0, F("LEFT side forward  (D7/D6 -> IN1/IN2 -> OUT1/OUT2)"));
    testStep(0, 1, F("RIGHT side forward (D5/D4 -> IN3/IN4 -> OUT3/OUT4)"));
    testStep(-1, 0, F("LEFT side backward"));
    testStep(0, -1, F("RIGHT side backward"));
    testStep(1, 1, F("BOTH sides forward"));
  }
  Serial.println(F("Wheel control ready"));
}

void loop() {
  if (Serial.available() > 0) {
    char c = Serial.read();

    switch (c) {
      //                  left right
      case 'F': drive( 1,  1); break;  // forward
      case 'B': drive(-1, -1); break;  // backward
      case 'L': drive(-1,  1); break;  // spin left
      case 'R': drive( 1, -1); break;  // spin right
      case 'G': drive( 0,  1); break;  // forward-left
      case 'I': drive( 1,  0); break;  // forward-right
      case 'H': drive( 0, -1); break;  // back-left
      case 'J': drive(-1,  0); break;  // back-right
      case 'S': drive( 0,  0); break;  // stop
      default: return;                  // ignore anything else (newlines etc.)
    }
    Serial.print(F("Got "));
    Serial.println(c);
  }

  if (AUTO_STOP_MS > 0 && moving && millis() - lastCommand > AUTO_STOP_MS) {
    drive(0, 0);
  }
}

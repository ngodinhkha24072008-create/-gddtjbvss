// L298N pin check: turns ONE input on at a time, 3 seconds each, then repeats.
//
//   Step 1: D7 -> IN1 on   => OUT1 on
//   Step 2: D6 -> IN2 on   => OUT2 on
//   Step 3: D5 -> IN3 on   => OUT3 on
//   Step 4: D4 -> IN4 on   => OUT4 on
//
// HOW TO CHECK (pick one):
//  a) Watch the small LEDs on the L298N next to the IN pins / outputs (if your board
//     has them): the matching one should light on each step.
//  b) Multimeter on DC volts, black probe on L298N GND, red probe on OUT1, OUT2,
//     OUT3, OUT4: the output for the current step reads about 9-10 V, the others ~0 V.
//  c) Connect one motor between the active OUT and its partner (OUT1+OUT2 or
//     OUT3+OUT4): it should spin on that output's step.
// Serial Monitor at 9600 shows which step is running. The Arduino's L LED blinks
// once per step number (1-4 blinks).

const byte pins[4]  = {7, 6, 5, 4};

void setup() {
  Serial.begin(9600);
  for (byte i = 0; i < 4; i++) pinMode(pins[i], OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.println(F("L298N pin check"));
}

void loop() {
  for (byte i = 0; i < 4; i++) {
    for (byte j = 0; j < 4; j++) digitalWrite(pins[j], LOW);
    digitalWrite(pins[i], HIGH);

    Serial.print(F("Step "));
    Serial.print(i + 1);
    Serial.print(F(": D"));
    Serial.print(pins[i]);
    Serial.print(F(" -> IN"));
    Serial.print(i + 1);
    Serial.print(F(" ON, OUT"));
    Serial.print(i + 1);
    Serial.println(F(" should be ON"));

    for (byte b = 0; b <= i; b++) {   // blink the step number on the L LED
      digitalWrite(LED_BUILTIN, HIGH); delay(150);
      digitalWrite(LED_BUILTIN, LOW);  delay(150);
    }
    delay(3000 - (i + 1) * 300);
  }
  for (byte j = 0; j < 4; j++) digitalWrite(pins[j], LOW);
  Serial.println(F("All off"));
  delay(1500);
}

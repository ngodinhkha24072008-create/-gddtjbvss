// Wheel test drive - based on the original Code.ino (same pins, same patterns)
// Runs all four wheels at the same time, by itself, no phone needed:
//   forward 3 s -> stop 1 s -> backward 3 s -> stop 1 s -> repeat
// Lift the wheels off the ground first.
// Open Tools > Serial Monitor at 9600 to see each step.

void setup() {
  Serial.begin(9600); // Start serial communication at 9600 baud rate

  // Set motor control pins as output
  pinMode(7, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(5, OUTPUT);
  pinMode(4, OUTPUT);

  Serial.println("Wheel test drive starting");
  delay(2000); // 2 s to get your hands clear
}

void loop() {
  // Move Forward - all four wheels (same as case 'F')
  Serial.println("Forward");
  digitalWrite(7, HIGH);
  digitalWrite(6, LOW);
  digitalWrite(5, HIGH);
  digitalWrite(4, LOW);
  delay(3000);

  // Stop (same as case 'S')
  Serial.println("Stop");
  digitalWrite(7, LOW);
  digitalWrite(6, LOW);
  digitalWrite(5, LOW);
  digitalWrite(4, LOW);
  delay(1000);

  // Move Backward - all four wheels (same as case 'B')
  Serial.println("Backward");
  digitalWrite(7, LOW);
  digitalWrite(6, HIGH);
  digitalWrite(5, LOW);
  digitalWrite(4, HIGH);
  delay(3000);

  // Stop (same as case 'S')
  Serial.println("Stop");
  digitalWrite(7, LOW);
  digitalWrite(6, LOW);
  digitalWrite(5, LOW);
  digitalWrite(4, LOW);
  delay(1000);
}

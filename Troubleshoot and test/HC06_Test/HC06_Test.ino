// HC-06 test - passes everything straight through, nothing else.
//
// WIRING:  HC-06 TXD -> D2   HC-06 RXD -> D3   VCC -> 5V   GND -> GND
//
// 1) Upload, open Tools > Serial Monitor at 9600 baud.
// 2) Connect your phone to the HC-06 (LED goes from fast blinking to steady).
// 3) Send something from the phone (any button or the app's terminal):
//      it appears in the Serial Monitor as  Phone sent: 'F'  (and its code).
//    => the HC-06 IS sending to the Arduino.
// 4) Type something in the Serial Monitor and press Enter:
//      it appears on the phone (in the app's terminal / reply line).
//    => the Arduino IS sending to the HC-06.
// 5) Before connecting the phone, set the Serial Monitor line ending to
//    "No line ending" and type  AT  then Enter. A working HC-06 answers  OK
//    (only while NO phone is connected).

#include <SoftwareSerial.h>

SoftwareSerial btSerial(2, 3); // RX, TX
unsigned long received = 0;

void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.println(F("HC-06 test ready. Send something from the phone."));
}

void loop() {
  // Phone -> HC-06 -> Arduino
  if (btSerial.available() > 0) {
    char c = btSerial.read();
    received++;
    digitalWrite(LED_BUILTIN, !digitalRead(LED_BUILTIN));  // L LED flips for every character
    Serial.print(F("Phone sent: '"));
    if (c == '\n') Serial.print(F("\\n"));
    else if (c == '\r') Serial.print(F("\\r"));
    else Serial.print(c);
    Serial.print(F("'  code "));
    Serial.print((int)c);
    Serial.print(F("   total "));
    Serial.println(received);
  }

  // Serial Monitor -> Arduino -> HC-06 -> phone
  if (Serial.available() > 0) {
    btSerial.write(Serial.read());
  }
}

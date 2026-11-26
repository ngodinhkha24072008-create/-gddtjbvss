// HC-06 baud finder + wiring check
//
// IMPORTANT: the phone must NOT be connected to the HC-06 (its LED blinking fast).
// WIRING:  HC-06 TXD -> D2   HC-06 RXD -> D3   VCC -> 5V   GND -> GND
//
// Open Tools > Serial Monitor at 9600. The Arduino sends "AT" to the HC-06 at
// every common speed and shows what comes back. A working HC-06 answers "OK".
// Press the Arduino's reset button to run it again.

#include <SoftwareSerial.h>

SoftwareSerial btSerial(2, 3); // RX, TX

const long speeds[] = {9600, 38400, 19200, 57600, 115200, 4800, 2400, 1200};

void setup() {
  Serial.begin(9600);
  Serial.println(F("HC-06 baud finder - phone must be DISCONNECTED"));
  Serial.println();

  bool found = false;
  for (byte i = 0; i < sizeof(speeds) / sizeof(speeds[0]); i++) {
    btSerial.begin(speeds[i]);
    delay(100);
    while (btSerial.available()) btSerial.read();   // clear old bytes

    btSerial.print("AT");                            // HC-06 needs no line ending
    unsigned long start = millis();
    String reply = "";
    while (millis() - start < 1500) {
      while (btSerial.available()) reply += (char)btSerial.read();
    }

    Serial.print(F("Trying "));
    Serial.print(speeds[i]);
    Serial.print(F(" baud ... "));
    if (reply.length() == 0) {
      Serial.println(F("no reply"));
    } else {
      Serial.print(F("reply: \""));
      Serial.print(reply);
      Serial.print(F("\""));
      if (reply.indexOf("OK") >= 0) {
        Serial.println(F("   <-- HC-06 FOUND"));
        found = true;
      } else {
        Serial.println(F("   (something came back, but not OK)"));
      }
    }
    btSerial.end();
  }

  Serial.println();
  if (found) {
    Serial.println(F("RESULT: wiring works. Use the speed marked FOUND."));
  } else {
    Serial.println(F("RESULT: no OK at any speed. Check:"));
    Serial.println(F(" - HC-06 TXD is in D2 and RXD is in D3 (try swapping them)"));
    Serial.println(F(" - HC-06 VCC to 5V and GND to GND, its LED lit"));
    Serial.println(F(" - the phone is disconnected (LED blinking fast)"));
  }
}

void loop() {}

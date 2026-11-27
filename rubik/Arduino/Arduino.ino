#include "Calibrate.h"
#include "MyServo.h"
#include "SequenceManager.h"
#include "API.h"
#include "BtLink.h"

void setup() {
    Serial.begin(9600);
    Serial.setTimeout(3000);
    calibrateSetup();   // Initialize EEPROM where calibrations are stored
#if !CALIBRATE
    APISetup();        // Setup API if not in calibration mode
#endif
}

void loop() {
    // During calibration, seqManager is never busy  
    if (millis() - seqManager.idleTimeMs < 3000 || seqManager.isBusy() || CALIBRATE)
        attachAllServos();
    else
        detachAllServos();
        
#if CALIBRATE
    calibrateLoop();
#else
    APILoop();  // Listen for API commands
    int res = seqManager.tick();    // If ongoing sequence, keep going
    if (res < 0)
    {
        Link.print("SEQ ERR ");
        Link.println(res);
    }
#endif
}

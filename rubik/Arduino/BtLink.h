#pragma once
#include <Arduino.h>
#include <SoftwareSerial.h>
#include "Config.h"

// The HC-06 link (SoftwareSerial on BT_RX_PIN / BT_TX_PIN).
extern SoftwareSerial btSerial;

// Everything printed to Link goes to BOTH the HC-06 (the phone app)
// and the USB Serial Monitor, so you can watch replies on the computer.
class DualPrint : public Print
{
public:
    using Print::write; // keep write(const char*) etc.
    size_t write(uint8_t c) override;
    size_t write(const uint8_t* buffer, size_t size) override;
};

extern DualPrint Link;

// Start the Bluetooth serial port
void linkBegin();

// The port that has an incoming command waiting (Bluetooth first, then USB),
// or nullptr if neither has data.
Stream* linkAvailable();

#include "BtLink.h"

SoftwareSerial btSerial(BT_RX_PIN, BT_TX_PIN); // RX, TX
DualPrint Link;

size_t DualPrint::write(uint8_t c)
{
    Serial.write(c);
    btSerial.write(c);
    return 1;
}

size_t DualPrint::write(const uint8_t* buffer, size_t size)
{
    Serial.write(buffer, size);
    btSerial.write(buffer, size);
    return size;
}

void linkBegin()
{
    btSerial.begin(BT_BAUD);
    btSerial.setTimeout(2000); // same as the USB port, safer for long lines
}

Stream* linkAvailable()
{
    if (btSerial.available())
        return &btSerial;
    if (Serial.available())
        return &Serial;
    return nullptr;
}

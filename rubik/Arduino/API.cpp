#include <Arduino.h>
#include <string.h>
#include "MyServo.h"
#include "SequenceManager.h"
#include "Config.h"
#include "API.h"
#include "BtLink.h"
#include "Types.h"

#define MAX_TOKENS  5

static char rxBuf[SEQUENCE_BUFFER_SIZE];    // This is extremely wasteful for the SEQ command, but oh well fuck it
static char* tokens[MAX_TOKENS];

void APISetup()
{
    linkBegin();             // HC-06 on D11/D12
    Serial.setTimeout(2000); // safer for long lines
    Link.println("Listening for API commands, type 'HELP' for list of commands.");
}

static int tokenize(char* line, char** tokens, int maxTokens)
{
    int count = 0;

    while (*line && count < maxTokens)
    {
        // Skip leading spaces
        while (*line == ' ')
            ++line;

        if (*line == '\0')
            break;

        tokens[count++] = line;

        // Advance to next space
        while (*line && *line != ' ')
            ++line;

        // Null-terminate token
        if (*line)
        {
            *line = '\0';
            ++line;
        }
    }

    return count;
}

void APILoop()
{
    // Commands can come from the phone (HC-06) or the USB Serial Monitor
    Stream* in = linkAvailable();
    if (!in)
        return;

    int len = in->readBytesUntil('\n', rxBuf, SEQUENCE_BUFFER_SIZE - 1);
    rxBuf[len] = '\0';

    // Trim trailing CR (Windows serial)
    if (len > 0 && rxBuf[len - 1] == '\r')
        rxBuf[len - 1] = '\0';

    if (rxBuf[0] == '\0')
        return;

    int tokenCount = tokenize(rxBuf, tokens, MAX_TOKENS);
    if (tokenCount == 0)
        return;

    const char* cmd = tokens[0];

    // --- HELP ---
    if (strcmp(cmd, "HELP") == 0)
    {
        Link.println("Commands:");
        Link.println("HELP");
        Link.println("PING");
        Link.println("STATUS [servo]");
        Link.println("SEQ <string>|C");
        Link.println("MOVE <delay_ms> <orientation> <moves>");
        return;
    }

    // --- SEQ ---
    if (strcmp(cmd, "SEQ") == 0)
    {
        if (tokenCount != 2)
        {
            Link.println("ERR args");
            return;
        }

        int res = seqManager.startSequence(tokens[1]);
        if (res == 0) Link.println("OK");
        else if (res == -1) Link.println("ERR busy");
        else if (res == -2) Link.println("ERR format");
        else Link.println("ERR");

        return;
    }

    // --- MOVE ---
    if (strcmp(cmd, "MOVE") == 0)
    {
        if (tokenCount != 4)
        {
            Link.println("ERR args");
            return;
        }

        int delay = atoi(tokens[1]);
        if (delay < 0)
        {
            Link.println("ERR delay");
            return;
        }

        int inputOrientation = atoi(tokens[2]);
        if (inputOrientation == 0)
            seqManager.orientation = ORIENT_NORMAL;
        else if (inputOrientation == 1)
            seqManager.orientation = ORIENT_INVERT;
        else
        {
            Link.println("ERR orientation");
            return;
        }

        int res = seqManager.startMoves(tokens[3], delay);
        if (res == 0) Link.println("OK");
        else if (res == -1) Link.println("ERR busy");
        else if (res == -2) Link.println("ERR format");
        else Link.println("ERR");

        return;
    }

    // --- STATUS ---
    if (strcmp(cmd, "STATUS") == 0)
    {
        if (tokenCount == 1)
        {
            Link.println(seqManager.isBusy() ? "BUSY" : "IDLE");
            return;
        }

        if (tokenCount != 2)
        {
            Link.println("ERR args");
            return;
        }

        ServoType servo;
        if (!parseServoType(tokens[1][0], servo))
        {
            Link.println("ERR servo_type");
            return;
        }

        switch (servos[servo].getState())
        {
            case STATE_R: Link.println('R'); break;
            case STATE_L: Link.println('L'); break;
            case STATE_C: Link.println('C'); break;
            case STATE_r: Link.println('r'); break;
            case STATE_l: Link.println('l'); break;
            default: Link.println("ERR state"); break;
        }
        return;
    }

    // --- PING ---
    if (strcmp(cmd, "PING") == 0)
    {
        Link.println("PONG");
        return;
    }

    // --- UNKNOWN ---
    Link.print("ERR cmd: ");
    Link.println(cmd);
}

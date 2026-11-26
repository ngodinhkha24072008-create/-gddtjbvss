package com.example.cubesolver.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Console palette, shared with the Rover Arm Gamepad controller:
 * deep navy shell, amber accent, mono command codes.
 */
object Console {
    val Bg = Color(0xFF0D1220)        // page background
    val Shell = Color(0xFF161D2E)     // panels / handheld body
    val ShellTop = Color(0xFF1A2236)  // top of the panel gradient
    val Well = Color(0xFF0A0E19)      // recessed areas (camera, inputs, chips)
    val Line = Color(0xFF29344B)      // hairlines and borders
    val Fg = Color(0xFFE6EBF5)        // main text
    val Muted = Color(0xFF8A96AE)     // secondary text, labels
    val Accent = Color(0xFFFFB547)    // amber: primary actions, live readouts
    val OnAccent = Color(0xFF1B1305)  // text on amber
    val Ok = Color(0xFF46D39A)        // connected / idle / done
    val Danger = Color(0xFFFF5F57)    // abort / stop / errors

    // Face-button colours (used to tell the four grabbers apart)
    val Tri = Color(0xFF43D6B5)
    val Circ = Color(0xFFFF7A6B)
    val Cross = Color(0xFF7EA6FF)
    val Sq = Color(0xFFE08CFF)

    // Key caps (radial gradient top -> bottom)
    val KeyTop = Color(0xFF26304A)
    val KeyBottom = Color(0xFF151C2C)
}

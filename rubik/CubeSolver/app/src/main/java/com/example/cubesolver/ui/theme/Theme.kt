package com.example.cubesolver.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Console scheme from the Rover Arm Gamepad: navy surfaces, amber primary.
// Always dark, no dynamic colour, so the app looks the same on every phone.
private val ConsoleColorScheme = darkColorScheme(
    primary = Console.Accent,                 // Buttons, slider, selected things
    onPrimary = Console.OnAccent,
    primaryContainer = Console.Accent,
    onPrimaryContainer = Console.OnAccent,
    secondary = Console.Tri,
    onSecondary = Console.OnAccent,
    secondaryContainer = Console.Line,        // Nav bar selected pill
    onSecondaryContainer = Console.Accent,
    tertiary = Console.Cross,
    background = Console.Bg,
    onBackground = Console.Fg,
    surface = Console.Bg,                     // Scaffold / screen
    onSurface = Console.Fg,
    surfaceVariant = Console.Well,
    onSurfaceVariant = Console.Muted,
    surfaceTint = Console.Accent,
    surfaceContainerLowest = Console.Well,
    surfaceContainerLow = Console.Shell,
    surfaceContainer = Console.Shell,         // Navigation bar
    surfaceContainerHigh = Console.Shell,     // Dialogs
    surfaceContainerHighest = Console.Shell,  // Cards
    outline = Console.Line,
    outlineVariant = Console.Line,
    error = Console.Danger,
    onError = Console.OnAccent
)

@Composable
fun CubeSolverTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ConsoleColorScheme,
        typography = Typography,
        content = content
    )
}

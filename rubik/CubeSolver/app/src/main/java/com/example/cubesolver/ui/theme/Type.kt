package com.example.cubesolver.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.cubesolver.R

// Same three faces as the car controller: Chakra Petch for titles,
// Inter for reading, JetBrains Mono for anything the robot receives.
val DisplayFont = FontFamily(Font(R.font.chakra_petch_bold, FontWeight.Bold))
val BodyFont = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)
val MonoFont = FontFamily(
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

object ConsoleText {
    val title = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 19.sp, letterSpacing = 0.5.sp)
    val panelTitle = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.4.sp)
    val body = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val bodyStrong = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
    val small = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 17.sp)
    val label = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, letterSpacing = 1.5.sp)
    val mono = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp)
    val monoBold = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp)
    val key = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 20.sp)
    val readout = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 38.sp)
    val button = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.6.sp)
}

val Typography = Typography(
    bodyLarge = ConsoleText.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = ConsoleText.body,
    bodySmall = ConsoleText.small,
    titleLarge = ConsoleText.title,
    titleMedium = ConsoleText.panelTitle,
    labelLarge = ConsoleText.bodyStrong,
    labelMedium = ConsoleText.label,
    labelSmall = ConsoleText.label
)

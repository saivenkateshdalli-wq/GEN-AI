package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AcademicDarkColorScheme = darkColorScheme(
    primary = AcademicBlueLight,
    secondary = ScholarGold,
    tertiary = AcademicPurple,
    background = OxfordNavy,
    surface = TutorSlate,
    surfaceVariant = TutorCardBg,
    onPrimary = Color.White,
    onSecondary = OxfordNavy,
    onBackground = ParchmentLight,
    onSurface = ParchmentLight
)

private val AcademicLightColorScheme = lightColorScheme(
    primary = AcademicBlue,
    secondary = ParchmentAmber,
    tertiary = AcademicPurple,
    background = ParchmentLight,
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OxfordNavy,
    onSurface = OxfordNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve distinctive University Tutor theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AcademicDarkColorScheme
        else -> AcademicDarkColorScheme // Prefer high-contrast Oxford Academic Dark theme for VENKY
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

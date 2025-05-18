package com.yourdomain.yourappname.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Replace with your own font assets if you have them
val AppFontFamily = FontFamily.Default // Or specify custom fonts

val Typography =
        Typography(
                bodyLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                letterSpacing = 0.5.sp
                        ),
                titleLarge =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 22.sp,
                                lineHeight = 28.sp,
                                letterSpacing = 0.sp
                        ),
                labelSmall =
                        TextStyle(
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.5.sp
                        )
                // Define other text styles as needed from Material 3 spec
                )

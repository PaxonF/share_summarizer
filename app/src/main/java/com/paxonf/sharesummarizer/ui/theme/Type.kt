package com.paxonf.sharesummarizer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.paxonf.sharesummarizer.R

@OptIn(ExperimentalTextApi::class)
val RobotoFlex =
    FontFamily(
        Font(
            resId = R.font.roboto_flex,
            weight = FontWeight.Normal,
            variationSettings =
            FontVariation.Settings(
                FontVariation.weight(400)
            )
        ),
        Font(
            resId = R.font.roboto_flex,
            weight = FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700))
        ),
        Font(
            resId = R.font.roboto_flex,
            weight = FontWeight.ExtraBold,
            variationSettings =
            FontVariation.Settings(
                FontVariation.weight(800),
                FontVariation.width(125f)
            )
        )
    )

val Typography =
    Typography(
        bodyLarge =
        TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge =
        TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall =
        TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
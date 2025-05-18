package com.paxonf.sharesummarizer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paxonf.sharesummarizer.data.AppPreferences
import com.paxonf.sharesummarizer.viewmodel.SummaryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(uiState: SummaryUiState, onDismiss: () -> Unit, onRetry: (String) -> Unit) {
        // Get the text size from preferences
        val context = LocalContext.current
        val textSize = AppPreferences(context).textSize

        ModalBottomSheet(
                onDismissRequest = onDismiss,
                modifier = Modifier.fillMaxHeight(0.9f),
                scrimColor = Color.Transparent,
                dragHandle = { /* Empty composable to hide the drag handle */},
                containerColor = MaterialTheme.colorScheme.surface
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Text(
                                text = "Summary",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                        )

                        when {
                                uiState.isLoading -> {
                                        CircularProgressIndicator(
                                                modifier = Modifier.padding(32.dp)
                                        )
                                        Text(
                                                text = "Generating summary...",
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                }
                                uiState.error != null -> {
                                        // Simple text icon instead of using material icons
                                        Text(
                                                text = "⚠️", // Unicode warning symbol
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Text(
                                                text = uiState.error,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                modifier =
                                                        Modifier.padding(
                                                                horizontal = 16.dp,
                                                                vertical = 8.dp
                                                        )
                                        )
                                        Button(
                                                onClick = { onRetry(uiState.originalText) },
                                                modifier = Modifier.padding(top = 16.dp)
                                        ) { Text("Retry") }
                                }
                                uiState.summary.isNotEmpty() -> {
                                        // For testing, append a test string with bold and italic
                                        // formatting
                                        val summary = uiState.summary.trim().trimIndent()

                                        // A simple markdown-style formatter without any external
                                        // dependencies
                                        val formattedText =
                                                remember(summary) {
                                                        formatBasicMarkdown(
                                                                summary,
                                                                headingLarge = 22.sp,
                                                                headingMedium = 18.sp,
                                                                headingSmall = 16.sp
                                                        )
                                                }

                                        Text(
                                                text = formattedText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = 8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                                Button(onClick = onDismiss) { Text("Close") }

                                                // Add buttons for sharing, copying, etc.
                                        }
                                }
                                else -> {
                                        Text(
                                                text = "No summary available",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center
                                        )
                                }
                        }
                }
        }
}

// Basic Markdown formatter that handles common markdown syntax
fun formatBasicMarkdown(
        markdown: String,
        headingLarge: TextUnit = 22.sp,
        headingMedium: TextUnit = 18.sp,
        headingSmall: TextUnit = 16.sp
) = buildAnnotatedString {
        val lines = markdown.split("\n")

        for (line in lines) {
                val trimmedLine = line.trim()

                when {
                        // Headers - only handle basic headers for simplicity
                        trimmedLine.startsWith("# ") -> {
                                withStyle(
                                        SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = headingLarge
                                        )
                                ) { append(trimmedLine.substring(2)) }
                        }
                        trimmedLine.startsWith("## ") -> {
                                withStyle(
                                        SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = headingMedium
                                        )
                                ) { append(trimmedLine.substring(3)) }
                        }
                        trimmedLine.startsWith("### ") -> {
                                withStyle(
                                        SpanStyle(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = headingSmall
                                        )
                                ) { append(trimmedLine.substring(4)) }
                        }

                        // List items
                        trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ") -> {
                                append("• ")
                                append(parseInlineFormatting(trimmedLine.substring(2)))
                        }

                        // Regular text, parse for inline formatting
                        else -> {
                                append(parseInlineFormatting(trimmedLine))
                        }
                }

                append("\n")
        }
}

// Helper function to parse inline formatting (bold and italic)
fun parseInlineFormatting(text: String) = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
                // Bold text (double asterisks)
                if (i < text.length - 3 && text.substring(i, i + 2) == "**") {
                        val endBold = text.indexOf("**", i + 2)
                        if (endBold != -1) {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(text.substring(i + 2, endBold))
                                }
                                i = endBold + 2
                                continue
                        }
                }

                // Italic text (single asterisk)
                if (i < text.length - 1 && text[i] == '*' && text[i + 1] != '*') {
                        val endItalic = text.indexOf('*', i + 1)
                        if (endItalic != -1 &&
                                        (endItalic + 1 >= text.length || text[endItalic + 1] != '*')
                        ) {
                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                        append(text.substring(i + 1, endItalic))
                                }
                                i = endItalic + 1
                                continue
                        }
                }

                // Regular character
                append(text[i])
                i++
        }
}

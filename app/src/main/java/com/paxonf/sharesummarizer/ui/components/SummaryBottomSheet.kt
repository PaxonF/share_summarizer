package com.paxonf.sharesummarizer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.paxonf.sharesummarizer.viewmodel.SummaryUiState
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryBottomSheet(
        uiState: SummaryUiState,
        onDismiss: () -> Unit,
        onRetry: (String) -> Unit,
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
        val scrollState = rememberScrollState()

        // Get current theme colors for WebView
        val textColor = MaterialTheme.colorScheme.onSurface

        ModalBottomSheet(
                onDismissRequest = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                scrimColor = Color.Transparent,
                containerColor = containerColor
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .heightIn(
                                                max = 700.dp
                                        ) // Maximum height but allows natural sizing
                                        .navigationBarsPadding() // Handle navigation bar properly
                                        .padding(
                                                start = 16.dp,
                                                end = 16.dp,
                                                bottom = 16.dp,
                                                top = 8.dp
                                        )
                                        .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                                        val summary = uiState.summary.trim()

                                        // Parse and display markdown content using native Compose
                                        MarkdownText(
                                                markdown = summary,
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = 8.dp),
                                                color = textColor
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
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

@Composable
private fun MarkdownText(
        markdown: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface
) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Simple markdown parsing for the most common elements
                val lines = markdown.lines()
                var i = 0

                while (i < lines.size) {
                        val line = lines[i].trim()

                        when {
                                line.startsWith("# ") -> {
                                        Text(
                                                text = line.substring(2),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = color,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                }
                                line.startsWith("## ") -> {
                                        Text(
                                                text = line.substring(3),
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = color,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                }
                                line.startsWith("### ") -> {
                                        Text(
                                                text = line.substring(4),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = color,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                }
                                line.startsWith("**") && line.endsWith("**") -> {
                                        Text(
                                                text = line.substring(2, line.length - 2),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = color
                                        )
                                }
                                line.startsWith("- ") || line.startsWith("* ") -> {
                                        Row(
                                                modifier = Modifier.padding(start = 16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                Text(
                                                        text = "•",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = color
                                                )
                                                Text(
                                                        text = line.substring(2),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = color,
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }
                                }
                                line.startsWith("*") &&
                                        line.endsWith("*") &&
                                        !line.startsWith("**") -> {
                                        Text(
                                                text = line.substring(1, line.length - 1),
                                                style =
                                                        MaterialTheme.typography.bodyMedium.copy(
                                                                fontStyle =
                                                                        androidx.compose.ui.text
                                                                                .font.FontStyle
                                                                                .Italic
                                                        ),
                                                color = color.copy(alpha = 0.8f)
                                        )
                                }
                                line.isNotEmpty() -> {
                                        // Handle bold text within regular paragraphs
                                        if (line.contains("**")) {
                                                FormattedText(text = line, color = color)
                                        } else {
                                                Text(
                                                        text = line,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = color
                                                )
                                        }
                                }
                        }
                        i++
                }
        }
}

@Composable
private fun FormattedText(text: String, color: Color) {
        val parts = text.split("**")
        val formattedText = buildAnnotatedString {
                parts.forEachIndexed { index, part ->
                        if (index % 2 == 0) {
                                // Regular text
                                append(part)
                        } else {
                                // Bold text
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(part)
                                }
                        }
                }
        }

        Text(text = formattedText, style = MaterialTheme.typography.bodyMedium, color = color)
}

// Helper function to generate styled HTML from markdown
internal fun generateStyledHtmlFromMarkdown(
        markdownText: String,
        textColor: Color,
): String {
        // Convert Color to hex string for CSS
        val textColorHex = String.format("#%06X", (0xFFFFFF and textColor.toArgb()))

        // Parse the markdown
        val flavour = GFMFlavourDescriptor()
        val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdownText)
        val generatedHtml = HtmlGenerator(markdownText, parsedTree, flavour).generateHtml()

        // Create the complete HTML with styling
        return """
    <!DOCTYPE html>
    <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style type="text/css">
                html, body {
                    margin: 0;
                    padding: 0;
                    overflow: hidden;
                    height: auto;
                    width: 100%;
                }
                body {
                    font-family: sans-serif;
                    font-size: 16px;
                    line-height: 1.6;
                    color: ${textColorHex};
                    background-color: transparent;
                    -webkit-user-select: text;
                    user-select: text;
                    overflow: hidden;
                    height: auto;
                }
                h1 {
                    font-size: 20px;
                    font-weight: bold;
                    color: ${textColorHex};
                }
                h2 {
                    font-size: 18px;
                    font-weight: bold;
                    color: ${textColorHex};
                }
                h3 {
                    font-size: 16px;
                    font-weight: bold;
                    color: ${textColorHex};
                }
                h1, h2, h3 {
                    margin-top: 16px;
                    margin-bottom: 12px;
                    line-height: 1.3;
                    -webkit-user-select: text;
                    user-select: text;
                }
                ul, ol {
                    margin-top: 8px;
                    margin-bottom: 8px;
                    padding-left: 24px;
                    color: ${textColorHex};
                }
                li {
                    margin-bottom: 8px;
                    padding-left: 4px;
                    -webkit-user-select: text;
                    user-select: text;
                    color: ${textColorHex};
                }
                p {
                    margin-bottom: 12px;
                    -webkit-user-select: text;
                    user-select: text;
                    color: ${textColorHex};
                }
                a {
                    color: ${textColorHex};
                    text-decoration: underline;
                }
                /* Ensure all text is selectable and no scrolling */
                * {
                    -webkit-user-select: text;
                    user-select: text;
                    overflow: visible;
                }
            </style>
        </head>
        <body>
            $generatedHtml
        </body>
    </html>
    """
}

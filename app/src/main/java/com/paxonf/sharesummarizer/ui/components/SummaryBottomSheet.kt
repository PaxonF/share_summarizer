package com.paxonf.sharesummarizer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    textSizeMultiplier: Float = 1.0f
) {
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val loadingStateHeight = 200.dp // Fixed height for loading state content

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        scrimColor = Color.Transparent,
        containerColor = containerColor,
        sheetState = sheetState
    ) {
        Column(
            modifier =
            Modifier.fillMaxWidth()
                .fillMaxHeight(0.9f)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .animateContentSize() // Animate height changes of this Column
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Define a stable key for the animation based on the state type
            val animationKey =
                when {
                    uiState.error != null -> "Error"
                    uiState.isLoading && uiState.summary.isEmpty() -> "Loading"
                    uiState.summary.isNotEmpty() -> "Summary"
                    else -> "Empty"
                }

            AnimatedContent(
                targetState = animationKey,
                transitionSpec = {
                    // Slide and fade for major state changes
                    (slideInVertically { fullHeight -> fullHeight } + fadeIn())
                        .togetherWith(slideOutVertically { fullHeight -> -fullHeight } + fadeOut())
                        .using(SizeTransform(clip = false))
                },
                label = "SummaryContentAnimation"
            ) { key ->
                when (key) {
                    "Summary" -> {
                        val summary = uiState.summary.trim()
                        MarkdownText(
                            markdown = summary,
                            modifier = Modifier.fillMaxWidth(),
                            color = contentColor,
                            textSizeMultiplier = textSizeMultiplier
                        )
                    }
                    "Loading" -> {
                        Box(
                            modifier =
                            Modifier.fillMaxWidth()
                                .height(loadingStateHeight), // Fixed height
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "Generating summary...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor
                                )
                            }
                        }
                    }
                    "Error" -> {
                        Box(
                            modifier =
                            Modifier.fillMaxWidth()
                                .padding(vertical = 16.dp), // Natural height
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "⚠️",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = uiState.error ?: "An unknown error occurred",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                Button(
                                    onClick = { onRetry(uiState.originalText) },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    "Empty" -> {
                        Box(
                            modifier =
                            Modifier.fillMaxWidth()
                                .height(loadingStateHeight / 2), // Smaller fixed height
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No summary available",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textSizeMultiplier: Float
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Simple markdown parsing for the most common elements
        val lines = markdown.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()

            when {
                line.startsWith("# ") && line.length > 2 -> {
                    Text(
                        text = line.substring(2),
                        style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontSize =
                            MaterialTheme.typography.headlineMedium.fontSize *
                                textSizeMultiplier,
                            lineHeight =
                            MaterialTheme.typography.headlineMedium.lineHeight *
                                textSizeMultiplier
                        ),
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("## ") && line.length > 3 -> {
                    Text(
                        text = line.substring(3),
                        style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontSize =
                            MaterialTheme.typography.headlineSmall.fontSize *
                                textSizeMultiplier,
                            lineHeight =
                            MaterialTheme.typography.headlineSmall.lineHeight *
                                textSizeMultiplier
                        ),
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                line.startsWith("### ") && line.length > 4 -> {
                    Text(
                        text = line.substring(4),
                        style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontSize =
                            MaterialTheme.typography.titleLarge.fontSize *
                                textSizeMultiplier,
                            lineHeight =
                            MaterialTheme.typography.titleLarge.lineHeight *
                                textSizeMultiplier
                        ),
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                line.startsWith("**") && line.endsWith("**") && line.length > 4 -> {
                    Text(
                        text = line.substring(2, line.length - 2),
                        style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize =
                            MaterialTheme.typography.bodyLarge.fontSize *
                                textSizeMultiplier,
                            lineHeight =
                            MaterialTheme.typography.bodyLarge.lineHeight *
                                textSizeMultiplier
                        ),
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                (line.startsWith("- ") || line.startsWith("* ")) && line.length > 2 -> {
                    Row(
                        modifier = Modifier.padding(start = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize =
                                MaterialTheme.typography.bodyMedium.fontSize *
                                    textSizeMultiplier,
                                lineHeight =
                                MaterialTheme.typography.bodyMedium.lineHeight *
                                    textSizeMultiplier
                            ),
                            color = color
                        )
                        Text(
                            text = line.substring(2),
                            style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize =
                                MaterialTheme.typography.bodyMedium.fontSize *
                                    textSizeMultiplier,
                                lineHeight =
                                MaterialTheme.typography.bodyMedium.lineHeight *
                                    textSizeMultiplier
                            ),
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                line.startsWith("*") && line.endsWith("*") && !line.startsWith("**") && line.length > 2 -> {
                    Text(
                        text = line.substring(1, line.length - 1),
                        style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize =
                            MaterialTheme.typography.bodyMedium.fontSize *
                                textSizeMultiplier,
                            lineHeight =
                            MaterialTheme.typography.bodyMedium.lineHeight *
                                textSizeMultiplier
                        ),
                        color = color.copy(alpha = 0.8f)
                    )
                }
                line.isNotEmpty() -> {
                    // Handle bold text within regular paragraphs
                    if (line.contains("**")) {
                        FormattedText(
                            text = line,
                            color = color,
                            textSizeMultiplier = textSizeMultiplier
                        )
                    } else {
                        Text(
                            text = line,
                            style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize =
                                MaterialTheme.typography.bodyMedium.fontSize *
                                    textSizeMultiplier,
                                lineHeight =
                                MaterialTheme.typography.bodyMedium.lineHeight *
                                    textSizeMultiplier
                            ),
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
private fun FormattedText(text: String, color: Color, textSizeMultiplier: Float) {
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

        Text(
                text = formattedText,
                style =
                        MaterialTheme.typography.bodyMedium.copy(
                                fontSize =
                                        MaterialTheme.typography.bodyMedium.fontSize *
                                                textSizeMultiplier,
                                lineHeight =
                                        MaterialTheme.typography.bodyMedium.lineHeight *
                                                textSizeMultiplier
                        ),
                color = color
        )
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

package com.paxonf.sharesummarizer.ui.components

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
                modifier = Modifier.fillMaxHeight(0.9f),
                scrimColor = Color.Transparent,
                containerColor = containerColor,
                windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .fillMaxHeight()
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

                                        // Create the complete HTML with markdown parsing and
                                        // styling
                                        // Pass current theme colors
                                        val styledHtml =
                                                generateStyledHtmlFromMarkdown(
                                                        summary,
                                                        textColor = textColor
                                                )

                                        // Use WebView instead of TextView which handles HTML
                                        // content better
                                        AndroidView(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(horizontal = 8.dp)
                                                                .heightIn(min = 200.dp),
                                                factory = { context ->
                                                        WebView(context).apply {
                                                                // Disable scrolling in the WebView
                                                                // since we have a parent scroller
                                                                isVerticalScrollBarEnabled = false
                                                                isHorizontalScrollBarEnabled = false

                                                                // Make WebView background
                                                                // transparent to show the parent
                                                                // background
                                                                setBackgroundColor(
                                                                        android.graphics.Color
                                                                                .TRANSPARENT
                                                                )

                                                                // Configure WebView settings
                                                                settings.apply {
                                                                        javaScriptEnabled = false
                                                                        loadWithOverviewMode = true
                                                                        useWideViewPort = true
                                                                        defaultFontSize = 16

                                                                        // Enable text selection and
                                                                        // copying
                                                                        setSupportMultipleWindows(
                                                                                false
                                                                        )
                                                                }

                                                                // Enable long-press selection and
                                                                // context menus
                                                                isLongClickable = true

                                                                // Load the HTML content directly
                                                                loadDataWithBaseURL(
                                                                        null,
                                                                        styledHtml,
                                                                        "text/html",
                                                                        "UTF-8",
                                                                        null
                                                                )
                                                        }
                                                }
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
                body {
                    font-family: sans-serif;
                    font-size: 16px;
                    line-height: 1.6;
                    color: ${textColorHex};
                    margin: 8px 0;
                    padding: 0;
                    background-color: transparent;
                    -webkit-user-select: text;
                    user-select: text;
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
                /* Ensure all text is selectable */
                * {
                    -webkit-user-select: text;
                    user-select: text;
                }
            </style>
        </head>
        <body>
            $generatedHtml
        </body>
    </html>
    """
}

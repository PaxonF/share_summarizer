package com.paxonf.sharesummarizer.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paxonf.sharesummarizer.viewmodel.SummaryUiState
import dev.jeziellago.compose.markdowntext.MarkdownText

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
                        // Regex to remove blank lines between list items to make lists "tight"
                        val tightSummary = summary.replace("(\\n[ \\t]*\\n)(?=[ \\t]*[\\*\\-])".toRegex(), "\n")
                        MarkdownText(
                            markdown = tightSummary,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = contentColor,
                                fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value * textSizeMultiplier).sp
                            )
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
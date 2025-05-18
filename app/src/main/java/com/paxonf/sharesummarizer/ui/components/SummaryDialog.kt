package com.paxonf.sharesummarizer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paxonf.sharesummarizer.ui.theme.ShareSummarizerTheme
import com.paxonf.sharesummarizer.viewmodel.SummaryUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryDialog(
        uiState: SummaryUiState,
        onDismiss: () -> Unit,
        onRetry: (String) -> Unit // Pass the original text for retry
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth() // You can adjust height with .heightIn(max = ...dp)
    ) {
        Column(
                modifier =
                        Modifier.padding(16.dp)
                                .navigationBarsPadding() // Handles insets for navigation bar
                                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 32.dp))
                Text("Generating summary...", style = MaterialTheme.typography.titleMedium)
            } else if (uiState.error != null) {
                Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onRetry(uiState.originalText) }) { Text("Retry") }
            } else if (uiState.summary.isNotEmpty()) {
                Text(text = "Summary", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.summary, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                        "No summary available or an unexpected state.",
                        style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
            ) { Text("Close") }
            Spacer(modifier = Modifier.height(8.dp)) // Space for nav bar if any
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SummaryDialogPreviewLoading() {
    ShareSummarizerTheme {
        SummaryDialog(uiState = SummaryUiState(isLoading = true), onDismiss = {}, onRetry = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SummaryDialogPreviewSuccess() {
    ShareSummarizerTheme {
        SummaryDialog(
                uiState =
                        SummaryUiState(
                                summary =
                                        "This is a fantastic summary of the provided content. It is concise and to the point."
                        ),
                onDismiss = {},
                onRetry = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SummaryDialogPreviewError() {
    ShareSummarizerTheme {
        SummaryDialog(
                uiState =
                        SummaryUiState(
                                error =
                                        "Failed to connect to the summarization service. Please check your API key and internet connection.",
                                originalText = "Some original text"
                        ),
                onDismiss = {},
                onRetry = {}
        )
    }
}

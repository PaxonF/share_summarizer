package com.paxonf.sharesummarizer.utils

// OkHttp imports
import android.content.Context
import android.webkit.URLUtil
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

class TextSummarizer(private val context: Context) {

    private val client =
            OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS) // Infinite timeout for streaming
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

    fun summarize(
            text: String,
            summaryLength: Int,
            apiKey: String,
            modelId: String = "gemini-2.5-flash-lite",
            summaryPrompt: String
    ): Flow<String> =
            flow {
                        if (apiKey.isBlank()) {
                            emit(fallbackSummarize(text, summaryLength))
                            return@flow
                        }

                        // Check if the text is a URL and fetch its content if needed
                        val contentToSummarize =
                                if (isUrl(text)) {
                                    try {
                                        extractArticleContent(text)
                                    } catch (e: Exception) {
                                        emit("Error extracting content from URL: ${e.message}")
                                        return@flow
                                    }
                                } else {
                                    text
                                }

                        // Convert the 1-5 scale to a percentage (0.2 to 1.0)
                        val summaryLengthPercentage = convertLengthToPercentage(summaryLength)
                        val summaryLengthString = convertLengthToString(summaryLength)
                        val configInfo =
                                "The user has configured that their summary should be '$summaryLengthString', or, in other words, '$summaryLengthPercentage' long compared to the original text."

                        makeAPIRequest(
                                        contentToSummarize,
                                        apiKey,
                                        modelId,
                                        configInfo,
                                        summaryPrompt
                                )
                                .collect { emit(it) }
                    }
                    .catch { e ->
                        e.printStackTrace() // Log the exception for debugging
                        emit(fallbackSummarize(text, summaryLength))
                    }
                    .flowOn(Dispatchers.IO)

    private fun isUrl(text: String): Boolean {
        // Using Android's URLUtil to validate URL
        return URLUtil.isHttpUrl(text) || URLUtil.isHttpsUrl(text)
    }

    /** Extract article content using Readability4J for better content extraction */
    private suspend fun extractArticleContent(url: String): String =
            withContext(Dispatchers.IO) {
                try {
                    // First try with Readability4J
                    val doc =
                            Jsoup.connect(url)
                                    .userAgent(
                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                                    )
                                    .timeout(30000)
                                    .get()

                    val readability = Readability4J(url, doc.outerHtml())
                    val article = readability.parse()

                    if (!article.content.isNullOrBlank()) {
                        // Extract text only from the HTML content
                        return@withContext Jsoup.parse(article.content ?: "").text()
                    }

                    // If extraction fails, fall back to simple content fetching
                    return@withContext fetchUrlContent(url)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // If Readability4J fails, fall back to simple content fetching
                    return@withContext fetchUrlContent(url)
                }
            }

    private suspend fun fetchUrlContent(url: String): String =
            withContext(Dispatchers.IO) {
                val request = Request.Builder().url(url).build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        return@withContext response.body?.string() ?: "No content found at URL"
                    }
                } catch (e: Exception) {
                    throw IOException("Failed to fetch content from URL: ${e.message}")
                }
            }

    private fun convertLengthToString(length: Int): String {
        return when (length) {
            1 -> "very short"
            2 -> "short"
            3 -> "medium"
            4 -> "long"
            5 -> "very long"
            else -> "medium"
        }
    }

    // Convert from 1-5 scale to a percentage between 0.2 and 1.0
    private fun convertLengthToPercentage(length: Int): Float {
        return when (length) {
            1 -> 0.05f // Very short (5%)
            2 -> 0.1f // Short (10%)
            3 -> 0.15f // Medium (15%)
            4 -> 0.2f // Long (20%)
            5 -> 0.25f // Very long (25%)
            else -> 0.15f // Default to medium if invalid value
        }
    }

    private fun makeAPIRequest(
            promptText: String,
            apiKey: String,
            modelId: String = "gemini-2.5-flash-lite",
            configInfo: String,
            summaryPrompt: String
    ): Flow<String> =
            flow {
                val urlString =
                        "https://generativelanguage.googleapis.com/v1beta/models/$modelId:streamGenerateContent?key=$apiKey"

                // Construct the JSON request body with system_instruction separated from content
                val requestBodyJson = JSONObject()

                // Add system instruction
                val systemInstruction = JSONObject()
                val systemPart = JSONObject().put("text", summaryPrompt + " " + configInfo)
                systemInstruction.put("parts", org.json.JSONArray().put(systemPart))
                requestBodyJson.put("system_instruction", systemInstruction)

                // Add content to summarize
                val content = JSONObject()
                val contentPart = JSONObject().put("text", promptText)
                content.put("parts", org.json.JSONArray().put(contentPart))
                requestBodyJson.put("contents", org.json.JSONArray().put(content))

                // Add safety settings and generation config
                val generationConfig = JSONObject()
                generationConfig.put("temperature", 0.2)
                generationConfig.put("topP", 0.8)
                generationConfig.put("topK", 40)
                generationConfig.put("maxOutputTokens", 8192)
                requestBodyJson.put("generationConfig", generationConfig)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

                val request =
                        Request.Builder()
                                .url(urlString)
                                .post(requestBody)
                                .header("Content-Type", "application/json")
                                .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Unknown error"
                        try {
                            val errorJson = JSONObject(errorBody).optJSONObject("error")
                            val message =
                                    errorJson?.optString("message", "Unknown API error")
                                            ?: "Unknown API error"
                            emit("API Error: $message")
                        } catch (e: JSONException) {
                            emit("Error from API (status ${response.code}): $errorBody")
                        }
                        return@use
                    }

                    val source = response.body?.source()
                    while (source != null && !source.exhausted()) {
                        val line = source.readUtf8Line()
                        if (line != null && line.trim().startsWith("\"text\":")) {
                            try {
                                val textContent =
                                        line.trim()
                                                .substringAfter("\"text\":")
                                                .trim()
                                                .removeSurrounding("\"")
                                                .removeSuffix(",")
                                                .replace("\\n", "\n")
                                                .replace("\\\"", "\"")
                                emit(textContent)
                            } catch (e: Exception) {
                                // Ignore malformed lines
                            }
                        }
                    }
                }
            }

    /** Simple fallback summarization method (can be kept as a backup) */
    private fun fallbackSummarize(text: String, summaryLength: Int): String {
        val percentage = convertLengthToPercentage(summaryLength)
        val sentences = text.split(Regex("[.!?]")).filter { it.isNotBlank() }.map { it.trim() }
        val sentencesToInclude = (sentences.size * percentage).toInt().coerceAtLeast(1)
        return sentences.take(sentencesToInclude).joinToString(". ") + "."
    }
}

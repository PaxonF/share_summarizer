package com.paxonf.sharesummarizer.utils

// OkHttp imports
import android.content.Context
import android.webkit.URLUtil
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Readability4J
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.jsoup.Jsoup

class TextSummarizer(private val context: Context) {

    private val client =
            OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

    suspend fun summarize(
            text: String,
            summaryLength: Int,
            apiKey: String,
            modelId: String = "gemini-2.0-flash"
    ): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext fallbackSummarize(text, summaryLength)
            }

            try {
                // Check if the text is a URL and fetch its content if needed
                val contentToSummarize =
                        if (isUrl(text)) {
                            try {
                                extractArticleContent(text)
                            } catch (e: Exception) {
                                return@withContext "Error extracting content from URL: ${e.message}"
                            }
                        } else {
                            text
                        }

                // Convert the 1-5 scale to a percentage (0.2 to 1.0)
                val summaryLengthPercentage = convertLengthToPercentage(summaryLength)
                val summaryLengthString = convertLengthToString(summaryLength)
                val configInfo =
                        "The user has configured that their summary should be '$summaryLengthString', or, in other words, '$summaryLengthPercentage' long compared to the original text."

                makeAPIRequest(contentToSummarize, apiKey, modelId, configInfo)
            } catch (e: Exception) {
                e.printStackTrace() // Log the exception for debugging
                return@withContext fallbackSummarize(text, summaryLength)
            }
        }
    }

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
            modelId: String = "gemini-2.0-flash",
            configInfo: String
    ): String {
        val urlString =
                "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"

        // Extract the system instruction part (everything before the content to summarize)
        val systemInstructionText =
                "Summarize the following text. In your response, include a brief title of the article, or whatever you're summarizing, including the author, date, and source, if it is available. You should use raw markdown formatting to make the summary more readable. Use headers, italics, bold, or other markdown formatting to make the summarization clear, and only if appropriate. Do not include any other text in your response. Use bullet points or lists sparingly. Main headers and key ideas should not be bullet points or lists."

        // Construct the JSON request body with system_instruction separated from content
        val requestBodyJson = JSONObject()

        // Add system instruction
        val systemInstruction = JSONObject()
        val systemPart = JSONObject().put("text", systemInstructionText + " " + configInfo)
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
        generationConfig.put("maxOutputTokens", 1024)
        requestBodyJson.put("generationConfig", generationConfig)

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request =
                    Request.Builder()
                            .url(urlString)
                            .post(requestBody)
                            .header("Content-Type", "application/json")
                            .build()

            client.newCall(request).execute().use { response ->
                val statusCode = response.code
                val responseBody = response.body?.string() ?: ""

                if (statusCode == 200) {
                    val responseJson = JSONObject(responseBody)
                    try {
                        // Navigate through the JSON structure to get the text
                        // Expected path: candidates[0].content.parts[0].text
                        val text =
                                responseJson
                                        .getJSONArray("candidates")
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text")
                        return text
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return "Error parsing API response: ${e.message}"
                    }
                } else {
                    try {
                        val errorJson = JSONObject(responseBody).optJSONObject("error")
                        if (errorJson != null) {
                            val message = errorJson.optString("message", "Unknown error")
                            return "API Error: $message"
                        }
                    } catch (pe: Exception) {
                        // Ignored
                    }
                    return "Error from API (status $statusCode)"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Network error: ${e.message}"
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

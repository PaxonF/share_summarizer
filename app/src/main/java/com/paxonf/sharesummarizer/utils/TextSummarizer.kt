package com.paxonf.sharesummarizer.utils

// OkHttp imports
import android.content.Context
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TextSummarizer(private val context: Context) {

    suspend fun summarize(text: String, summaryLength: Int, apiKey: String): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext fallbackSummarize(text, summaryLength)
            }

            try {
                // Convert the 1-5 scale to a percentage (0.2 to 1.0)
                val summaryLengthPercentage = convertLengthToPercentage(summaryLength)
                val summaryLengthString = convertLengthToString(summaryLength)
                val prompt =
                        "Summarize the following text, article, or link concisely: \n\n$text.\n\n In your response, include a brief title for what you're summarizing. You should use markdown formatting to make the summary more readable. If appropriate, summarize it into a few bullet points, with headers, italics, bold, or other markdown formatting to make the summarization clear. Do not include any other text in your response.\n\nIf the link is unaccessible, please let the user know the link is not accessible.\n\nThe user has configured that their summary should be '$summaryLengthString' of the original text, or, in other words, '$summaryLengthPercentage' of the original text."
                makeAPIRequest(prompt, apiKey)
            } catch (e: Exception) {
                e.printStackTrace() // Log the exception for debugging
                return@withContext fallbackSummarize(text, summaryLength)
            }
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

    private fun makeAPIRequest(promptText: String, apiKey: String): String {
        val modelName = "gemini-1.5-flash" // Using a public Google Gemini model
        val urlString =
                "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        // Construct the JSON request body
        val requestBodyJson = JSONObject()
        val content = JSONObject()
        val part = JSONObject().put("text", promptText)
        content.put("parts", org.json.JSONArray().put(part))
        requestBodyJson.put("contents", org.json.JSONArray().put(content))

        // Add safety settings and generation config
        val generationConfig = JSONObject()
        generationConfig.put("temperature", 0.2)
        generationConfig.put("topP", 0.8)
        generationConfig.put("topK", 40)
        generationConfig.put("maxOutputTokens", 1024)
        requestBodyJson.put("generationConfig", generationConfig)

        try {
            // Create OkHttpClient with increased timeouts
            val client =
                    OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build()

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

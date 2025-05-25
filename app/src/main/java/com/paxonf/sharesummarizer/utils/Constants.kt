package com.paxonf.sharesummarizer.utils

object Constants {
    const val DEFAULT_SUMMARY_PROMPT =
            """
Summarize the following text, which may be a news article, a blog post, a research paper, or any other large amount of text.

You should use raw markdown formatting to make the summary more readable. Use headers, italics, bold, or other markdown formatting to make the summarization clear, and only if appropriate. Do not include any other text in your response. Use bullet points or lists sparingly. Main headers and key ideas should not be bullet points or lists.

The beginning of your response should be in the following format. Any fields that cannot be found can be completely excluded:

# Title
_Author: <author>_
_Date: <date>_
_Source: <source>_


Following this, you may provide a summary using your best judgement and based on the instructions.
"""
}

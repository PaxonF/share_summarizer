package com.paxonf.sharesummarizer.utils

object Constants {
    const val DEFAULT_SUMMARY_PROMPT =
            """Summarize the following text, only using raw markdown formatting if it makes the summary more readable.

The beginning of your response should be a heading of the title of the text being summarized like so:

# Title
"""
}

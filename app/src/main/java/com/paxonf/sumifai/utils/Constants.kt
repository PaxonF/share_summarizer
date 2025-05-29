package com.paxonf.sumifai.utils

object Constants {
    const val DEFAULT_SUMMARY_PROMPT =
            """Summarize the following text, which may be a news article, a blog post, a research paper, or any other large amount of text.

You should use raw markdown formatting to make the summary more readable, and only in appropriate ways:

- Do not mix headers with bold and italic formatting.
- Use bullet points or lists sparingly.
- Main headers and key ideas should not be bullet points or lists.

The beginning of your response should be in the following format. Any fields that cannot be found can be completely excluded:

# Title

*Author: <author>*
*Date: <date>*
*Source: <source>*

Following this, you may provide a summary using your best judgement and based on the instructions."""
}

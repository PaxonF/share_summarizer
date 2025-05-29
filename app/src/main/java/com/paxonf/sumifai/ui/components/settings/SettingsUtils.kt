package com.paxonf.sumifai.ui.components.settings

fun getLengthLabel(length: Int): String {
    return when (length) {
        1 -> "Very Short"
        2 -> "Short"
        3 -> "Medium"
        4 -> "Long"
        5 -> "Very Long"
        else -> "Medium"
    }
}

fun getTextSizeLabel(multiplier: Float): String {
    val percentage = (multiplier * 100).toInt()
    return "$percentage%"
}

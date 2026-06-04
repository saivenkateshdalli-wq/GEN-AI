package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

import androidx.compose.ui.unit.isSpecified

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val annotatedString = parseMarkdown(text)
    Text(
        text = annotatedString,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        lineHeight = if (fontSize.isSpecified) fontSize * 1.4f else TextUnit.Unspecified
    )
}

fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            val codeStart = text.indexOf("`", cursor)
            val italicStart = text.indexOf("*", cursor)

            // Find the closest formatting tag
            val nextTagIndex = listOf(boldStart, codeStart, italicStart)
                .filter { it != -1 }
                .minOrNull() ?: -1

            if (nextTagIndex == -1) {
                // No more tags, append the rest
                append(text.substring(cursor))
                break
            }

            // Append plain text up to the next tag
            append(text.substring(cursor, nextTagIndex))
            cursor = nextTagIndex

            when {
                cursor == boldStart -> {
                    val boldEnd = text.indexOf("**", boldStart + 2)
                    if (boldEnd != -1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(boldStart + 2, boldEnd))
                        }
                        cursor = boldEnd + 2
                    } else {
                        append("**")
                        cursor = boldStart + 2
                    }
                }
                cursor == codeStart -> {
                    val codeEnd = text.indexOf("`", codeStart + 1)
                    if (codeEnd != -1) {
                        withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)) {
                            append(text.substring(codeStart + 1, codeEnd))
                        }
                        cursor = codeEnd + 1
                    } else {
                        append("`")
                        cursor = codeStart + 1
                    }
                }
                cursor == italicStart -> {
                    val italicEnd = text.indexOf("*", italicStart + 1)
                    if (italicEnd != -1) {
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(italicStart + 1, italicEnd))
                        }
                        cursor = italicEnd + 1
                    } else {
                        append("*")
                        cursor = italicStart + 1
                    }
                }
                else -> {
                    append(text[cursor])
                    cursor++
                }
            }
        }
    }
}

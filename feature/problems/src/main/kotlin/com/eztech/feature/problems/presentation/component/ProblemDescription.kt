package com.eztech.feature.problems.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun ProblemDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    val blocks = rememberProblemDescriptionBlocks(text)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        blocks.forEach { block ->
            when (block) {
                is ProblemDescriptionBlock.Code -> CodeBlock(block.code)
                is ProblemDescriptionBlock.Heading -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                is ProblemDescriptionBlock.ListItem -> Text(
                    text = "- ${block.text}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is ProblemDescriptionBlock.Paragraph -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.small,
            )
            .padding(EzTechDimens.SpaceMedium),
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
    )
}

private fun rememberProblemDescriptionBlocks(text: String): List<ProblemDescriptionBlock> {
    val normalized = text.replace("\r\n", "\n").trim()
    if (normalized.isBlank()) return emptyList()

    val blocks = mutableListOf<ProblemDescriptionBlock>()
    val paragraph = StringBuilder()
    val code = StringBuilder()
    var inCodeBlock = false

    fun flushParagraph() {
        val value = paragraph.toString().trim()
        if (value.isNotBlank()) {
            blocks += ProblemDescriptionBlock.Paragraph(value)
            paragraph.clear()
        }
    }

    normalized.lines().forEach { rawLine ->
        val line = rawLine.trimEnd()
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                blocks += ProblemDescriptionBlock.Code(code.toString().trimEnd())
                code.clear()
                inCodeBlock = false
            } else {
                flushParagraph()
                inCodeBlock = true
            }
            return@forEach
        }

        if (inCodeBlock) {
            code.appendLine(line)
            return@forEach
        }

        when {
            trimmed.isBlank() -> flushParagraph()
            trimmed.startsWith("#") -> {
                flushParagraph()
                blocks += ProblemDescriptionBlock.Heading(
                    trimmed.trimStart('#').trim(),
                )
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                flushParagraph()
                blocks += ProblemDescriptionBlock.ListItem(trimmed.drop(2).trim())
            }
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(trimmed)
            }
        }
    }

    if (inCodeBlock && code.isNotBlank()) {
        blocks += ProblemDescriptionBlock.Code(code.toString().trimEnd())
    }
    flushParagraph()

    return blocks
}

private sealed interface ProblemDescriptionBlock {
    data class Heading(val text: String) : ProblemDescriptionBlock
    data class Paragraph(val text: String) : ProblemDescriptionBlock
    data class ListItem(val text: String) : ProblemDescriptionBlock
    data class Code(val code: String) : ProblemDescriptionBlock
}

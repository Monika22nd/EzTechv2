package com.eztech.feature.problems.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.eztech.core.domain.model.TestCase
import com.eztech.core.domain.model.TestCaseResult
import com.eztech.core.domain.model.TestCaseStatus
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun VisibleTestCaseCard(
    testCase: TestCase,
    index: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Text("Example ${index + 1}", fontWeight = FontWeight.SemiBold)
            CodeValue(label = "Input", value = testCase.input)
            CodeValue(label = "Expected", value = testCase.expectedOutput)
        }
    }
}

@Composable
fun TestResultCard(
    result: TestCaseResult,
    modifier: Modifier = Modifier,
) {
    val passed = result.status == TestCaseStatus.PASSED
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (passed) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
            ) {
                Icon(
                    imageVector = if (passed) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                    contentDescription = null,
                    tint = if (passed) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Text(
                    text = "Test ${result.index + 1}: ${result.status.displayName()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (result.isHidden) {
                    Icon(Icons.Rounded.Lock, contentDescription = "Hidden test")
                }
                Text(
                    text = "${result.executionTimeMs} ms",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            result.input?.let { CodeValue("Input", it) }
            result.expectedOutput?.let { CodeValue("Expected", it) }
            result.actualOutput?.let { CodeValue("Actual", it.ifBlank { "<empty>" }) }
            result.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CodeValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun TestCaseStatus.displayName(): String = when (this) {
    TestCaseStatus.PASSED -> "Passed"
    TestCaseStatus.WRONG_ANSWER -> "Wrong answer"
    TestCaseStatus.RUNTIME_ERROR -> "Runtime error"
    TestCaseStatus.TIME_LIMIT_EXCEEDED -> "Time limit exceeded"
}

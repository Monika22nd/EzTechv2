package com.eztech.feature.learn.presentation.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.eztech.core.ui.component.EzTechTopBar

@Composable
internal fun LearnTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    EzTechTopBar(
        title = title,
        onBackClick = onBackClick,
        actions = actions,
    )
}

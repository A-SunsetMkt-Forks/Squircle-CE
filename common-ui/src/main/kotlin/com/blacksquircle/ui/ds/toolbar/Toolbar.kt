/*
 * Copyright 2025 Squircle CE contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blacksquircle.ui.ds.toolbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.blacksquircle.ui.ds.PreviewBackground
import com.blacksquircle.ui.ds.R
import com.blacksquircle.ui.ds.SquircleTheme
import com.blacksquircle.ui.ds.button.IconButton
import com.blacksquircle.ui.ds.button.IconButtonSize
import com.blacksquircle.ui.ds.toolbar.internal.ToolbarActions
import com.blacksquircle.ui.ds.toolbar.internal.ToolbarContent
import com.blacksquircle.ui.ds.toolbar.internal.ToolbarIcon

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    alignment: Alignment.Horizontal = Alignment.Start,
    @DrawableRes navigationIcon: Int? = null,
    navigationIconDescription: String? = null,
    navigationActions: @Composable (RowScope.() -> Unit)? = null,
    onNavigationClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp)
            .background(SquircleTheme.colors.colorBackgroundSecondary)
            .statusBarsPadding()
    ) {
        ToolbarLayout(
            title = title,
            subtitle = subtitle,
            alignment = alignment,
            navigationIcon = navigationIcon,
            navigationIconDescription = navigationIconDescription,
            navigationActions = navigationActions,
            onNavigationClicked = onNavigationClicked,
        )
    }
}

@Composable
private fun ToolbarLayout(
    title: String?,
    subtitle: String?,
    alignment: Alignment.Horizontal,
    @DrawableRes navigationIcon: Int?,
    navigationIconDescription: String?,
    navigationActions: @Composable (RowScope.() -> Unit)?,
    onNavigationClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            ToolbarIcon(
                iconRes = navigationIcon,
                contentDescription = navigationIconDescription,
                onNavigationClicked = onNavigationClicked,
                modifier = Modifier
                    .size(56.dp)
                    .layoutId(ToolbarSlot.ToolbarIcon),
            )
            ToolbarContent(
                title = title,
                subtitle = subtitle,
                alignment = alignment,
                modifier = Modifier
                    .fillMaxSize()
                    .layoutId(ToolbarSlot.ToolbarContent),
            )
            ToolbarActions(
                content = navigationActions,
                modifier = Modifier
                    .height(56.dp)
                    .layoutId(ToolbarSlot.ToolbarActions),
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) { measurables, constraints ->
        // Useful variables
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        // Detect measurables
        val iconMeasurable = measurables.firstOrNull { it.layoutId == ToolbarSlot.ToolbarIcon }
        val contentMeasurable = measurables.first { it.layoutId == ToolbarSlot.ToolbarContent }
        val actionsMeasurable = measurables.firstOrNull { it.layoutId == ToolbarSlot.ToolbarActions }

        // Measure icon with wrapContentSize
        val iconConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val iconPlaceable = iconMeasurable?.measure(iconConstraints)

        // Icon and actions width
        val iconMeasuredWidth = iconPlaceable?.measuredWidth ?: 0
        val iconWidth = if (iconMeasuredWidth > 0) {
            /** It's not null and not empty composable */
            iconMeasuredWidth
        } else {
            16.dp.roundToPx()
        }

        // Measure actions
        val actionsConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxWidth = layoutWidth - iconWidth,
        )
        val actionsPlaceable = actionsMeasurable?.measure(actionsConstraints)

        // Actions width
        val actionsMeasuredWidth = actionsPlaceable?.measuredWidth ?: 0
        val actionsWidth = if (actionsMeasuredWidth > 0) {
            /** It's not null and not empty composable */
            actionsMeasuredWidth
        } else {
            16.dp.roundToPx()
        }

        // Content padding and size
        val contentPadding = when (alignment) {
            Alignment.CenterHorizontally -> {
                maxOf(iconWidth, actionsWidth)
            }
            else -> {
                iconWidth
            }
        }
        val contentMaxWidth = when (alignment) {
            Alignment.CenterHorizontally -> {
                maxOf(0, layoutWidth - (contentPadding * 2))
            }
            else -> {
                maxOf(0, layoutWidth - iconWidth - actionsWidth)
            }
        }

        // Measure content with padding
        val contentConstraints = constraints.copy(minWidth = 0, maxWidth = contentMaxWidth)
        val contentPlaceable = contentMeasurable.measure(contentConstraints)

        // Layout children
        layout(layoutWidth, layoutHeight) {
            // Place icon
            iconPlaceable?.placeRelative(x = 0, y = 0)

            // Place actions
            actionsPlaceable?.placeRelative(x = layoutWidth - actionsWidth, y = 0)

            // Place content
            contentPlaceable.placeRelative(contentPadding, 0)
        }
    }
}

@PreviewLightDark
@Composable
private fun ToolbarPreview() {
    PreviewBackground {
        Toolbar(
            title = "Lorem Ipsum",
            subtitle = "Lorem Ipsum",
            alignment = Alignment.Start,
            navigationIcon = R.drawable.ic_back,
            navigationActions = {
                IconButton(
                    iconResId = R.drawable.ic_edit,
                    iconSize = IconButtonSize.L,
                    onClick = {},
                )
            }
        )
    }
}

private enum class ToolbarSlot {
    ToolbarIcon,
    ToolbarContent,
    ToolbarActions,
}
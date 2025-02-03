/*
 * Copyright 2023 Squircle CE contributors.
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

package com.blacksquircle.ui.ds.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.blacksquircle.ui.ds.SquircleTheme

@Composable
@NonRestartableComposable
fun IconButton(
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = SquircleTheme.colors.colorTextAndIconPrimary,
    iconSize: IconButtonSize = IconButtonSize.M,
    contentDescription: String? = null,
    enabled: Boolean = true,
    anchor: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val boxSize = when (iconSize) {
        IconButtonSize.S -> 42.dp
        IconButtonSize.M -> 48.dp
        IconButtonSize.L -> 56.dp
    }
    val rippleSize = when (iconSize) {
        IconButtonSize.S -> 18.dp
        IconButtonSize.M -> 24.dp
        IconButtonSize.L -> 24.dp
    }
    Box(
        modifier = modifier
            .size(boxSize)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = rippleSize,
                ),
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            tint = if (enabled) iconColor else SquircleTheme.colors.colorTextAndIconDisabled,
        )
        anchor?.invoke()
    }
}

enum class IconButtonSize {
    S,
    M,
    L,
}
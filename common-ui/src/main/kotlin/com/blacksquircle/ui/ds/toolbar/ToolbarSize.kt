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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blacksquircle.ui.ds.button.IconButtonSize
import com.blacksquircle.ui.ds.button.IconButtonSizeDefaults

@Immutable
data class ToolbarSize(
    val height: Dp,
    val shadowSize: Dp,
    val emptyIconPadding: Dp,
    val emptyActionsPadding: Dp,
    val iconButtonSize: IconButtonSize,
    val iconButtonPadding: PaddingValues,
    val contentPadding: PaddingValues,
    val textSpacer: Dp,
)

object ToolbarSizeDefaults {

    val S: ToolbarSize
        get() = ToolbarSize(
            height = 48.dp,
            shadowSize = 4.dp,
            emptyIconPadding = 16.dp,
            emptyActionsPadding = 16.dp,
            iconButtonSize = IconButtonSizeDefaults.M,
            iconButtonPadding = PaddingValues(start = 4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            textSpacer = 4.dp,
        )

    val M: ToolbarSize
        get() = ToolbarSize(
            height = 56.dp,
            shadowSize = 4.dp,
            emptyIconPadding = 16.dp,
            emptyActionsPadding = 16.dp,
            iconButtonSize = IconButtonSizeDefaults.L,
            iconButtonPadding = PaddingValues(0.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            textSpacer = 4.dp,
        )
}
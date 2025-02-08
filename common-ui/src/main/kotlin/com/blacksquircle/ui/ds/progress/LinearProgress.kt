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

package com.blacksquircle.ui.ds.progress

import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.blacksquircle.ui.ds.PreviewBackground
import com.blacksquircle.ui.ds.SquircleTheme

@Composable
@NonRestartableComposable
fun LinearProgress(
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    indeterminate: Boolean = false,
) {
    if (indeterminate) {
        LinearProgressIndicator(
            color = SquircleTheme.colors.colorPrimary,
            modifier = modifier,
        )
    } else {
        LinearProgressIndicator(
            progress = progress,
            color = SquircleTheme.colors.colorPrimary,
            modifier = modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun LinearProgressPreview() {
    PreviewBackground {
        LinearProgress(progress = 0.7f)
    }
}
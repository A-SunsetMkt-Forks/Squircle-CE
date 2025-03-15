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

package com.blacksquircle.ui.feature.servers.ui.dialog.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.blacksquircle.ui.ds.SquircleTheme
import com.blacksquircle.ui.ds.button.IconButton
import com.blacksquircle.ui.ds.button.IconButtonSizeDefaults
import com.blacksquircle.ui.ds.textfield.TextField
import com.blacksquircle.ui.feature.servers.R
import com.blacksquircle.ui.ds.R as UiR

@Composable
@NonRestartableComposable
internal fun ServerKeyFile(
    keyId: String,
    onChooseFileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        inputText = keyId,
        labelText = stringResource(R.string.hint_key_file),
        readOnly = true,
        endContent = {
            IconButton(
                iconResId = UiR.drawable.ic_folder_open,
                iconColor = SquircleTheme.colors.colorTextAndIconSecondary,
                iconButtonSize = IconButtonSizeDefaults.S,
                onClick = onChooseFileClicked,
            )
        },
        modifier = modifier,
    )
}
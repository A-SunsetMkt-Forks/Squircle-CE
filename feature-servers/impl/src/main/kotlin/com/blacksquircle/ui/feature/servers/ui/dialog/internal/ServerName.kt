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

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.blacksquircle.ui.ds.textfield.TextField
import com.blacksquircle.ui.feature.servers.R

@Composable
@NonRestartableComposable
internal fun ServerName(
    name: String,
    onNameChanged: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    TextField(
        inputText = name,
        labelText = stringResource(R.string.hint_server_name),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
        ),
        error = isError,
        onInputChanged = onNameChanged,
        modifier = modifier,
    )
}
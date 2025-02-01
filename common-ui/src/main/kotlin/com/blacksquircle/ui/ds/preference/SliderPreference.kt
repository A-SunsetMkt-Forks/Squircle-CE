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

package com.blacksquircle.ui.ds.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blacksquircle.ui.ds.SquircleTheme

@Composable
fun SliderPreference(
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    minValue: Float = 0f,
    maxValue: Float = 1f,
    currentValue: Float = 0f,
    stepCount: Int = 1,
    onValueChanged: (Float) -> Unit = {},
) {
    Preference(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        bottomContent = {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                var displayValueHolder by rememberSaveable { mutableFloatStateOf(currentValue) }
                Box(
                    modifier = Modifier
                        .requiredHeight(32.dp)
                        .weight(1f)
                ) {
                    Slider(
                        value = displayValueHolder,
                        valueRange = minValue..maxValue,
                        steps = stepCount,
                        onValueChange = { displayValueHolder = it },
                        onValueChangeFinished = {
                            onValueChanged(displayValueHolder)
                        },
                        enabled = enabled,
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = displayValueHolder.toInt().toString(),
                    style = SquircleTheme.typography.text16Regular,
                    color = if (enabled) {
                        SquircleTheme.colors.colorTextAndIconPrimary
                    } else {
                        SquircleTheme.colors.colorTextAndIconDisabled
                    },
                )
                Spacer(modifier = Modifier.size(12.dp))
            }
        }
    )
}

@Preview
@Composable
private fun SliderPreferenceEnabledPreview() {
    SquircleTheme {
        SliderPreference(
            title = "Preference Title",
            subtitle = "Preference Subtitle",
            enabled = true,
            minValue = 1f,
            maxValue = 8f,
            currentValue = 4f,
            stepCount = 2,
            onValueChanged = {},
        )
    }
}

@Preview
@Composable
private fun SliderPreferenceDisabledPreview() {
    SquircleTheme {
        SliderPreference(
            title = "Preference Title",
            subtitle = "Preference Subtitle",
            enabled = false,
            minValue = 1f,
            maxValue = 8f,
            currentValue = 4f,
            stepCount = 2,
            onValueChanged = {},
        )
    }
}
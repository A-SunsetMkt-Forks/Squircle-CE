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

package com.blacksquircle.ui.feature.shortcuts.api.extensions

import android.view.KeyEvent
import com.blacksquircle.ui.feature.shortcuts.api.model.Keybinding
import com.blacksquircle.ui.feature.shortcuts.api.model.Shortcut

fun Int.keyCodeToChar(): Char {
    val charCode = when (this) {
        KeyEvent.KEYCODE_DPAD_LEFT -> 8592 // ←
        KeyEvent.KEYCODE_DPAD_RIGHT -> 8594 // →
        KeyEvent.KEYCODE_DEL -> 9003 // ⌫
        else -> KeyEvent(KeyEvent.ACTION_DOWN, this).unicodeChar
    }
    return charCode.toChar()
}

fun List<Keybinding>.forAction(
    ctrl: Boolean,
    shift: Boolean,
    alt: Boolean,
    keyCode: Int,
): Shortcut? {
    val char = keyCode.keyCodeToChar()
    val keybinding = find {
        it.key == char.uppercaseChar() &&
            it.isCtrl == ctrl &&
            it.isShift == shift &&
            it.isAlt == alt
    }
    return keybinding?.shortcut
}
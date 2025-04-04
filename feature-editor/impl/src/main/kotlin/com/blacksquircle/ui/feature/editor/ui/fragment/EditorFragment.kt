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

package com.blacksquircle.ui.feature.editor.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.navigation.fragment.findNavController
import com.blacksquircle.ui.ds.SquircleTheme

internal class EditorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        SquircleTheme {
            EditorScreen(navController = findNavController())
        }
    }

    companion object {

        const val KEY_CLOSE_MODIFIED = "KEY_CLOSE_TAB"
        const val KEY_SELECT_LANGUAGE = "KEY_SELECT_LANGUAGE"
        const val KEY_GOTO_LINE = "KEY_GOTO_LINE"
        const val KEY_INSERT_COLOR = "KEY_INSERT_COLOR"

        const val ARG_FILE_UUID = "ARG_FILE_UUID"
        const val ARG_LANGUAGE = "ARG_LANGUAGE"
        const val ARG_LINE_NUMBER = "ARG_LINE_NUMBER"
        const val ARG_COLOR = "ARG_COLOR"
    }
}
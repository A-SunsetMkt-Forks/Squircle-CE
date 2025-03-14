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

package com.blacksquircle.ui.feature.editor.ui.fragment.internal

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.blacksquircle.ui.ds.PreviewBackground
import com.blacksquircle.ui.ds.R
import com.blacksquircle.ui.ds.button.IconButton
import com.blacksquircle.ui.ds.extensions.findActivity
import com.blacksquircle.ui.ds.toolbar.Toolbar
import com.blacksquircle.ui.ds.toolbar.ToolbarSizeDefaults
import com.blacksquircle.ui.feature.editor.ui.fragment.menu.EditMenu
import com.blacksquircle.ui.feature.editor.ui.fragment.menu.FileMenu
import com.blacksquircle.ui.feature.editor.ui.fragment.menu.OtherMenu
import com.blacksquircle.ui.feature.editor.ui.fragment.menu.ToolsMenu
import com.blacksquircle.ui.feature.editor.ui.fragment.model.MenuType

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun EditorToolbar(
    modifier: Modifier = Modifier,
    onDrawerClicked: () -> Unit = {},
    onNewFileClicked: () -> Unit = {},
    onOpenFileClicked: () -> Unit = {},
    onSaveFileClicked: () -> Unit = {},
    onSaveFileAsClicked: () -> Unit = {},
    onCloseFileClicked: () -> Unit = {},
    onCutClicked: () -> Unit = {},
    onCopyClicked: () -> Unit = {},
    onPasteClicked: () -> Unit = {},
    onSelectAllClicked: () -> Unit = {},
    onSelectLineClicked: () -> Unit = {},
    onDeleteLineClicked: () -> Unit = {},
    onDuplicateLineClicked: () -> Unit = {},
    onForceSyntaxClicked: () -> Unit = {},
    onInsertColorClicked: () -> Unit = {},
    onFindClicked: () -> Unit = {},
    onUndoClicked: () -> Unit = {},
    onRedoClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
) {
    val activity = LocalContext.current.findActivity()
    val windowSizeClass = if (activity != null) {
        calculateWindowSizeClass(activity)
    } else {
        null
    }
    val isMediumWidth = if (windowSizeClass != null) {
        windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    } else {
        false
    }
    var menuType by rememberSaveable {
        mutableStateOf<MenuType?>(null)
    }

    Toolbar(
        navigationIcon = R.drawable.ic_menu,
        onNavigationClicked = onDrawerClicked,
        navigationActions = {
            if (isMediumWidth) {
                IconButton(
                    iconResId = R.drawable.ic_save,
                    onClick = onSaveFileClicked,
                )
            }
            IconButton(
                iconResId = R.drawable.ic_folder,
                onClick = { menuType = MenuType.FILE },
                anchor = {
                    FileMenu(
                        expanded = menuType == MenuType.FILE,
                        onDismiss = { menuType = null },
                        onNewFileClicked = { menuType = null; onNewFileClicked() },
                        onOpenFileClicked = { menuType = null; onOpenFileClicked() },
                        onSaveFileClicked = { menuType = null; onSaveFileClicked() },
                        onSaveFileAsClicked = { menuType = null; onSaveFileAsClicked() },
                        onCloseFileClicked = { menuType = null; onCloseFileClicked() },
                    )
                }
            )
            IconButton(
                iconResId = R.drawable.ic_pencil,
                onClick = { menuType = MenuType.EDIT },
                anchor = {
                    EditMenu(
                        expanded = menuType == MenuType.EDIT,
                        onDismiss = { menuType = null },
                        onCutClicked = { menuType = null; onCutClicked() },
                        onCopyClicked = { menuType = null; onCopyClicked() },
                        onPasteClicked = { menuType = null; onPasteClicked() },
                        onSelectAllClicked = { menuType = null; onSelectAllClicked() },
                        onSelectLineClicked = { menuType = null; onSelectLineClicked() },
                        onDeleteLineClicked = { menuType = null; onDeleteLineClicked() },
                        onDuplicateLineClicked = { menuType = null; onDuplicateLineClicked() },
                    )
                }
            )
            if (isMediumWidth) {
                IconButton(
                    iconResId = R.drawable.ic_wrench,
                    onClick = { menuType = MenuType.TOOLS },
                    anchor = {
                        ToolsMenu(
                            expanded = menuType == MenuType.TOOLS,
                            onDismiss = { menuType = null },
                            onForceSyntaxClicked = { menuType = null; onForceSyntaxClicked() },
                            onInsertColorClicked = { menuType = null; onInsertColorClicked() },
                        )
                    }
                )
            }
            if (isMediumWidth) {
                IconButton(
                    iconResId = R.drawable.ic_file_find,
                    onClick = onFindClicked,
                )
            }
            IconButton(
                iconResId = R.drawable.ic_undo,
                onClick = onUndoClicked,
            )
            IconButton(
                iconResId = R.drawable.ic_redo,
                onClick = onRedoClicked,
            )
            IconButton(
                iconResId = R.drawable.ic_dots_vertical,
                onClick = { menuType = MenuType.OTHER },
                anchor = {
                    OtherMenu(
                        isMediumWidth = isMediumWidth,
                        expanded = menuType == MenuType.OTHER,
                        onDismiss = { menuType = null },
                        onFindClicked = { menuType = null; onFindClicked() },
                        onToolsClicked = { menuType = MenuType.TOOLS },
                        onSettingsClicked = { menuType = null; onSettingsClicked() }
                    )
                    if (!isMediumWidth) {
                        ToolsMenu(
                            expanded = menuType == MenuType.TOOLS,
                            onDismiss = { menuType = null },
                            onForceSyntaxClicked = { menuType = null; onForceSyntaxClicked() },
                            onInsertColorClicked = { menuType = null; onInsertColorClicked() },
                        )
                    }
                }
            )
        },
        toolbarSize = ToolbarSizeDefaults.S,
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun EditorToolbarPreview() {
    PreviewBackground {
        EditorToolbar()
    }
}
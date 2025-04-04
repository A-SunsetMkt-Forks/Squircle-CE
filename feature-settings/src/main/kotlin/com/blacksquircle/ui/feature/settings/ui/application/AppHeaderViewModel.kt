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

package com.blacksquircle.ui.feature.settings.ui.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blacksquircle.ui.core.mvi.ViewEvent
import com.blacksquircle.ui.core.navigation.Screen
import com.blacksquircle.ui.core.storage.keyvalue.SettingsManager
import com.blacksquircle.ui.core.theme.Theme
import com.blacksquircle.ui.core.theme.ThemeManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

internal class AppHeaderViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val themeManager: ThemeManager,
) : ViewModel() {

    private val _viewState = MutableStateFlow(updateViewState())
    val viewState: StateFlow<AppHeaderViewState> = _viewState.asStateFlow()

    private val _viewEvent = Channel<ViewEvent>(Channel.BUFFERED)
    val viewEvent: Flow<ViewEvent> = _viewEvent.receiveAsFlow()

    fun onBackClicked() {
        viewModelScope.launch {
            _viewEvent.send(ViewEvent.PopBackStack())
        }
    }

    fun onThemeChanged(theme: Theme) {
        viewModelScope.launch {
            settingsManager.appTheme = theme.value
            themeManager.apply(theme)
            _viewState.value = updateViewState()
        }
    }

    fun onColorSchemeClicked() {
        viewModelScope.launch {
            val screen = Screen.Themes
            _viewEvent.send(ViewEvent.Navigation(screen))
        }
    }

    fun onFullscreenChanged(value: Boolean) {
        viewModelScope.launch {
            settingsManager.fullScreenMode = value
            _viewState.value = updateViewState()
        }
    }

    fun onConfirmExitChanged(value: Boolean) {
        viewModelScope.launch {
            settingsManager.confirmExit = value
            _viewState.value = updateViewState()
        }
    }

    private fun updateViewState(): AppHeaderViewState {
        return AppHeaderViewState(
            appTheme = Theme.of(settingsManager.appTheme),
            fullscreenMode = settingsManager.fullScreenMode,
            confirmExit = settingsManager.confirmExit,
        )
    }

    class Factory : ViewModelProvider.Factory {

        @Inject
        lateinit var viewModelProvider: Provider<AppHeaderViewModel>

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModelProvider.get() as T
        }
    }
}
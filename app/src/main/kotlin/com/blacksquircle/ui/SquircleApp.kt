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

package com.blacksquircle.ui

import android.app.Application
import android.content.Context
import com.blacksquircle.ui.core.logger.AndroidTree
import com.blacksquircle.ui.core.storage.keyvalue.SettingsManager
import com.blacksquircle.ui.core.theme.Theme
import com.blacksquircle.ui.core.theme.ThemeManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SquircleApp : Application() {

    override fun attachBaseContext(base: Context) {
        val settingsManager = SettingsManager(base)
        val themeManager = ThemeManager(base)
        val theme = Theme.of(settingsManager.theme)
        themeManager.apply(theme)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(AndroidTree())
    }
}
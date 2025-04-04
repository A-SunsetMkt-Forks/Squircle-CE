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

package com.blacksquircle.ui.application.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentContainerView
import com.blacksquircle.ui.R
import com.blacksquircle.ui.application.viewmodel.MainViewModel
import com.blacksquircle.ui.core.extensions.applySystemWindowInsets
import com.blacksquircle.ui.core.extensions.fullscreenMode
import com.blacksquircle.ui.core.extensions.viewModels
import com.blacksquircle.ui.internal.di.AppComponent
import com.blacksquircle.ui.utils.InAppUpdate
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import javax.inject.Provider

internal class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var inAppUpdate: InAppUpdate

    @Inject
    lateinit var viewModelProvider: Provider<MainViewModel>

    private val viewModel by viewModels<MainViewModel> { viewModelProvider.get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        AppComponent.buildOrGet(this).inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost = findViewById<FragmentContainerView>(R.id.nav_host)

        enableEdgeToEdge()
        window.fullscreenMode(viewModel.fullScreenMode)

        navHost.applySystemWindowInsets(false) { left, _, right, _ ->
            navHost.updatePadding(left = left, right = right)
        }

        inAppUpdate.checkForUpdates(this) {
            Snackbar.make(navHost, R.string.message_in_app_update_ready, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_restart) { inAppUpdate.completeUpdate() }
                .show()
        }

        if (savedInstanceState == null) {
            viewModel.handleIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleIntent(intent)
    }
}
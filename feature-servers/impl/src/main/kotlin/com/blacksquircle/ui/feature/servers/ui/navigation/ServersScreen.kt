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

package com.blacksquircle.ui.feature.servers.ui.navigation

import com.blacksquircle.ui.core.navigation.Screen

internal sealed class ServersScreen(route: Any) : Screen(route) {

    data class EditServer(val serverId: String) : ServersScreen(
        route = "blacksquircle://settings/cloud/edit/$serverId",
    )
}
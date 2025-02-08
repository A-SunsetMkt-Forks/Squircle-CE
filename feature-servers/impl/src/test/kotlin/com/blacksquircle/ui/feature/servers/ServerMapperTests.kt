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

package com.blacksquircle.ui.feature.servers

import com.blacksquircle.ui.core.storage.database.entity.server.ServerEntity
import com.blacksquircle.ui.feature.servers.data.mapper.ServerMapper
import com.blacksquircle.ui.filesystem.base.model.AuthMethod
import com.blacksquircle.ui.filesystem.base.model.FileServer
import com.blacksquircle.ui.filesystem.base.model.ServerConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerMapperTests {

    @Test
    fun `convert ServerEntity to ServerConfig`() {
        val serverEntity = ServerEntity(
            uuid = "1234567890",
            scheme = FileServer.FTP.value,
            name = "test",
            address = "192.168.21.97",
            port = 21,
            initialDir = "",
            authMethod = AuthMethod.PASSWORD.ordinal,
            username = "username",
            password = "password",
            privateKey = null,
            passphrase = "test",
        )
        val serverConfig = ServerConfig(
            uuid = "1234567890",
            scheme = FileServer.FTP,
            name = "test",
            address = "192.168.21.97",
            port = 21,
            initialDir = "",
            authMethod = AuthMethod.PASSWORD,
            username = "username",
            password = "password",
            privateKey = null,
            passphrase = "test",
        )

        assertEquals(serverConfig, ServerMapper.toModel(serverEntity))
    }

    @Test
    fun `convert ServerConfig to ServerEntity`() {
        val serverConfig = ServerConfig(
            uuid = "1234567890",
            scheme = FileServer.FTP,
            name = "test",
            address = "192.168.21.97",
            port = 21,
            initialDir = "",
            authMethod = AuthMethod.PASSWORD,
            username = "username",
            password = "password",
            privateKey = null,
            passphrase = "test",
        )
        val serverEntity = ServerEntity(
            uuid = "1234567890",
            scheme = FileServer.FTP.value,
            name = "test",
            address = "192.168.21.97",
            port = 21,
            initialDir = "",
            authMethod = AuthMethod.PASSWORD.ordinal,
            username = "username",
            password = "password",
            privateKey = null,
            passphrase = "test",
        )

        assertEquals(serverEntity, ServerMapper.toEntity(serverConfig))
    }
}
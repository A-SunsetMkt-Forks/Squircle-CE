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

package com.blacksquircle.ui.feature.explorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blacksquircle.ui.core.extensions.indexOf
import com.blacksquircle.ui.core.mvi.ViewEvent
import com.blacksquircle.ui.core.navigation.Screen
import com.blacksquircle.ui.core.provider.resources.StringProvider
import com.blacksquircle.ui.core.storage.keyvalue.SettingsManager
import com.blacksquircle.ui.feature.editor.api.interactor.EditorInteractor
import com.blacksquircle.ui.feature.explorer.R
import com.blacksquircle.ui.feature.explorer.data.manager.TaskManager
import com.blacksquircle.ui.feature.explorer.data.utils.fileComparator
import com.blacksquircle.ui.feature.explorer.domain.model.ErrorAction
import com.blacksquircle.ui.feature.explorer.domain.model.FilesystemModel
import com.blacksquircle.ui.feature.explorer.domain.model.SortMode
import com.blacksquircle.ui.feature.explorer.domain.model.TaskStatus
import com.blacksquircle.ui.feature.explorer.domain.model.TaskType
import com.blacksquircle.ui.feature.explorer.domain.model.ViewMode
import com.blacksquircle.ui.feature.explorer.domain.repository.ExplorerRepository
import com.blacksquircle.ui.feature.explorer.ui.fragment.ExplorerViewEvent
import com.blacksquircle.ui.feature.explorer.ui.fragment.ExplorerViewState
import com.blacksquircle.ui.feature.explorer.ui.fragment.model.BreadcrumbState
import com.blacksquircle.ui.feature.explorer.ui.fragment.model.ErrorState
import com.blacksquircle.ui.feature.explorer.ui.navigation.ExplorerScreen
import com.blacksquircle.ui.feature.servers.api.interactor.ServersInteractor
import com.blacksquircle.ui.filesystem.base.exception.AuthRequiredException
import com.blacksquircle.ui.filesystem.base.exception.AuthenticationException
import com.blacksquircle.ui.filesystem.base.exception.EncryptedArchiveException
import com.blacksquircle.ui.filesystem.base.exception.FileAlreadyExistsException
import com.blacksquircle.ui.filesystem.base.exception.FileNotFoundException
import com.blacksquircle.ui.filesystem.base.exception.InvalidArchiveException
import com.blacksquircle.ui.filesystem.base.exception.PermissionException
import com.blacksquircle.ui.filesystem.base.exception.SplitArchiveException
import com.blacksquircle.ui.filesystem.base.exception.UnsupportedArchiveException
import com.blacksquircle.ui.filesystem.base.model.AuthMethod
import com.blacksquircle.ui.filesystem.base.model.FileModel
import com.blacksquircle.ui.filesystem.base.model.FileType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import com.blacksquircle.ui.ds.R as UiR

internal class ExplorerViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    private val settingsManager: SettingsManager,
    private val taskManager: TaskManager,
    private val editorInteractor: EditorInteractor,
    private val explorerRepository: ExplorerRepository,
    private val serversInteractor: ServersInteractor,
) : ViewModel() {

    private val _viewState = MutableStateFlow(ExplorerViewState())
    val viewState: StateFlow<ExplorerViewState> = _viewState.asStateFlow()

    private val _viewEvent = Channel<ViewEvent>(Channel.BUFFERED)
    val viewEvent: Flow<ViewEvent> = _viewEvent.receiveAsFlow()

    private var selectedFilesystem: String
        get() = settingsManager.filesystem
        set(value) {
            settingsManager.filesystem = value
        }
    private var showHidden: Boolean
        get() = settingsManager.showHidden
        set(value) {
            settingsManager.showHidden = value
        }
    private var sortMode: SortMode
        get() = SortMode.of(settingsManager.sortMode)
        set(value) {
            settingsManager.sortMode = value.value
        }
    private val viewMode: ViewMode
        get() = ViewMode.of(settingsManager.viewMode)

    private var taskType: TaskType = TaskType.CREATE
    private var taskBuffer: List<FileModel> = emptyList()
    private var selectedFiles: List<FileModel> = emptyList()
    private var filesystems: List<FilesystemModel> = emptyList()
    private var breadcrumbs: List<BreadcrumbState> = emptyList()
    private var selectedBreadcrumb: Int = -1
    private var searchQuery: String = ""
    private var currentJob: Job? = null

    init {
        loadFilesystems()
    }

    fun onBackClicked() {
        if (selectedFiles.isNotEmpty()) {
            selectedFiles = emptyList()
            _viewState.update {
                it.copy(selectedFiles = selectedFiles)
            }
        } else {
            viewModelScope.launch {
                _viewEvent.send(ViewEvent.PopBackStack())
            }
        }
    }

    fun onFilesystemSelected(filesystem: String) {
        viewModelScope.launch {
            try {
                if (filesystem == selectedFilesystem) {
                    return@launch
                }
                val filesystemModel = filesystems.first { it.uuid == filesystem }

                selectedFilesystem = filesystem
                breadcrumbs = explorerRepository.loadBreadcrumbs(filesystemModel).mapBreadcrumbs()
                selectedBreadcrumb = breadcrumbs.size - 1
                resetBuffer()

                _viewState.update {
                    it.copy(
                        selectedFilesystem = selectedFilesystem,
                        breadcrumbs = breadcrumbs,
                        selectedBreadcrumb = selectedBreadcrumb,
                    )
                }
                onRefreshClicked()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, e.message)
                _viewEvent.send(ViewEvent.Toast(e.message.orEmpty()))
            }
        }
    }

    fun onFilesystemAdded() {
        viewModelScope.launch {
            try {
                filesystems = explorerRepository.loadFilesystems()

                _viewState.update {
                    it.copy(filesystems = filesystems)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, e.message)
                _viewEvent.send(ViewEvent.Toast(e.message.orEmpty()))
            }
        }
    }

    fun onAddServerClicked() {
        viewModelScope.launch {
            _viewEvent.send(ViewEvent.Navigation(Screen.Server))
        }
    }

    fun onQueryChanged(query: String) {
        searchQuery = query
        _viewState.update {
            it.copy(
                breadcrumbs = breadcrumbs.mapSelected { state ->
                    state.copy(fileList = state.fileList.applyFilter())
                },
                searchQuery = searchQuery,
            )
        }
    }

    fun onClearQueryClicked() {
        searchQuery = ""
        _viewState.update {
            it.copy(
                breadcrumbs = breadcrumbs.mapSelected { state ->
                    state.copy(fileList = state.fileList.applyFilter())
                },
                searchQuery = searchQuery,
            )
        }
    }

    fun onShowHiddenClicked() {
        showHidden = !showHidden
        _viewState.update {
            it.copy(
                breadcrumbs = breadcrumbs.mapSelected { state ->
                    state.copy(fileList = state.fileList.applyFilter())
                },
                showHidden = showHidden,
            )
        }
    }

    fun onSortModeSelected(value: SortMode) {
        sortMode = value
        _viewState.update {
            it.copy(
                breadcrumbs = breadcrumbs.mapSelected { state ->
                    state.copy(fileList = state.fileList.applyFilter())
                },
                sortMode = sortMode,
            )
        }
    }

    fun onHomeClicked() {
        if (breadcrumbs.isNotEmpty()) {
            val breadcrumb = breadcrumbs.first()
            loadFiles(breadcrumb.fileModel, fromUser = true)
        }
    }

    fun onBreadcrumbClicked(breadcrumb: BreadcrumbState) {
        selectedFiles = emptyList()
        _viewState.update {
            it.copy(selectedFiles = selectedFiles)
        }
        loadFiles(breadcrumb.fileModel, fromUser = true)
    }

    fun onFileClicked(fileModel: FileModel) {
        if (selectedFiles.isNotEmpty()) {
            onFileSelected(fileModel)
        } else if (fileModel.directory) {
            loadFiles(fileModel, fromUser = true)
        } else {
            viewModelScope.launch {
                when (fileModel.type) {
                    FileType.ARCHIVE -> extractFiles(fileModel)
                    FileType.DEFAULT,
                    FileType.TEXT -> {
                        editorInteractor.openFile(fileModel)
                        _viewEvent.send(ViewEvent.PopBackStack())
                    }

                    else -> onOpenWithClicked(fileModel)
                }
            }
        }
    }

    fun onFileSelected(fileModel: FileModel) {
        val index = selectedFiles.indexOf { it.fileUri == fileModel.fileUri }
        if (index == -1) {
            selectedFiles += fileModel
        } else {
            selectedFiles -= fileModel
        }
        _viewState.update {
            it.copy(selectedFiles = selectedFiles)
        }
    }

    fun onRefreshClicked() {
        if (breadcrumbs.isNotEmpty()) {
            val breadcrumb = breadcrumbs[selectedBreadcrumb]
            loadFiles(breadcrumb.fileModel, fromUser = false)
        }
    }

    fun onCreateClicked() {
        viewModelScope.launch {
            taskType = TaskType.CREATE
            taskBuffer = emptyList()
            selectedFiles = emptyList()
            _viewState.update {
                it.copy(
                    taskType = taskType,
                    selectedFiles = selectedFiles,
                )
            }

            val screen = ExplorerScreen.CreateDialogScreen
            _viewEvent.send(ViewEvent.Navigation(screen))
        }
    }

    fun onRenameClicked() {
        viewModelScope.launch {
            taskType = TaskType.RENAME
            taskBuffer = listOf(selectedFiles.first())
            selectedFiles = emptyList()
            _viewState.update {
                it.copy(
                    taskType = taskType,
                    selectedFiles = selectedFiles,
                )
            }

            val screen = ExplorerScreen.RenameDialogScreen(taskBuffer.first().name)
            _viewEvent.send(ViewEvent.Navigation(screen))
        }
    }

    fun onDeleteClicked() {
        viewModelScope.launch {
            taskType = TaskType.DELETE
            taskBuffer = selectedFiles.toList()
            selectedFiles = emptyList()
            _viewState.update {
                it.copy(
                    taskType = taskType,
                    selectedFiles = selectedFiles,
                )
            }

            val screen = ExplorerScreen.DeleteDialogScreen(taskBuffer.first().name, taskBuffer.size)
            _viewEvent.send(ViewEvent.Navigation(screen))
        }
    }

    fun onCopyClicked() {
        viewModelScope.launch {
            taskType = TaskType.COPY
            taskBuffer = selectedFiles.toList()
            selectedFiles = emptyList()
            _viewState.update {
                it.copy(
                    taskType = taskType,
                    selectedFiles = selectedFiles,
                )
            }
        }
    }

    fun onPasteClicked() {
        when (taskType) {
            TaskType.CUT -> cutFiles()
            TaskType.COPY -> copyFiles()
            else -> Unit
        }
    }

    fun onCutClicked() {
        taskType = TaskType.CUT
        taskBuffer = selectedFiles.toList()
        selectedFiles = emptyList()
        _viewState.update {
            it.copy(
                taskType = taskType,
                selectedFiles = selectedFiles,
            )
        }
    }

    fun onSelectAllClicked() {
        val parent = breadcrumbs[selectedBreadcrumb]
        selectedFiles = parent.fileList
        _viewState.update {
            it.copy(selectedFiles = selectedFiles)
        }
    }

    fun onOpenWithClicked(fileModel: FileModel? = null) {
        viewModelScope.launch {
            val source = fileModel ?: selectedFiles.first()
            _viewEvent.send(ExplorerViewEvent.OpenFileWith(source))
            resetBuffer()
        }
    }

    fun onPropertiesClicked() {
        viewModelScope.launch {
            val fileModel = selectedFiles.first()
            val screen = ExplorerScreen.PropertiesDialogScreen(fileModel)
            _viewEvent.send(ViewEvent.Navigation(screen))
            resetBuffer()
        }
    }

    fun onCopyPathClicked() {
        viewModelScope.launch {
            val fileModel = selectedFiles.first()
            _viewEvent.send(ExplorerViewEvent.CopyPath(fileModel))
            resetBuffer()
        }
    }

    fun onCompressClicked() {
        viewModelScope.launch {
            taskType = TaskType.COMPRESS
            taskBuffer = selectedFiles.toList()
            selectedFiles = emptyList()
            _viewState.update {
                it.copy(
                    taskType = taskType,
                    selectedFiles = selectedFiles,
                )
            }

            val screen = ExplorerScreen.CompressDialogScreen
            _viewEvent.send(ViewEvent.Navigation(screen))
        }
    }

    // region ERROR_ACTION

    fun onErrorActionClicked(errorAction: ErrorAction) {
        viewModelScope.launch {
            when (errorAction) {
                ErrorAction.REQUEST_PERMISSIONS -> {
                    _viewEvent.send(ExplorerViewEvent.RequestPermission)
                }

                ErrorAction.ENTER_PASSWORD -> {
                    val screen = ExplorerScreen.AuthDialogScreen(AuthMethod.PASSWORD)
                    _viewEvent.send(ViewEvent.Navigation(screen))
                }

                ErrorAction.ENTER_PASSPHRASE -> {
                    val screen = ExplorerScreen.AuthDialogScreen(AuthMethod.KEY)
                    _viewEvent.send(ViewEvent.Navigation(screen))
                }

                ErrorAction.UNDEFINED -> Unit
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            val screen = ExplorerScreen.StorageDeniedScreen
            _viewEvent.send(ViewEvent.Navigation(screen))
        }
    }

    fun onPermissionGranted() {
        onRefreshClicked()
    }

    fun onCredentialsEntered(credentials: String) {
        viewModelScope.launch {
            try {
                serversInteractor.authenticate(selectedFilesystem, credentials)
                val breadcrumb = breadcrumbs[selectedBreadcrumb]
                loadFiles(breadcrumb.fileModel, fromUser = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, e.message)
                _viewEvent.send(ViewEvent.Toast(e.message.orEmpty()))
            }
        }
    }

    // endregion

    fun createFile(fileName: String, isFolder: Boolean) {
        viewModelScope.launch {
            val parent = breadcrumbs[selectedBreadcrumb].fileModel
            val taskId = explorerRepository.createFile(parent, fileName, isFolder)
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    fun renameFile(fileName: String) {
        viewModelScope.launch {
            val fileModel = taskBuffer.first()
            val taskId = explorerRepository.renameFile(fileModel, fileName)
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    fun deleteFile() {
        viewModelScope.launch {
            val taskId = explorerRepository.deleteFiles(taskBuffer.toList())
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    fun compressFiles(fileName: String) {
        viewModelScope.launch {
            val parent = breadcrumbs[selectedBreadcrumb].fileModel
            val taskId = explorerRepository.compressFiles(taskBuffer.toList(), parent, fileName)
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    private fun cutFiles() {
        viewModelScope.launch {
            val parent = breadcrumbs[selectedBreadcrumb].fileModel
            val taskId = explorerRepository.cutFiles(taskBuffer.toList(), parent)
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    private fun copyFiles() {
        viewModelScope.launch {
            val parent = breadcrumbs[selectedBreadcrumb].fileModel
            val taskId = explorerRepository.copyFiles(taskBuffer.toList(), parent)
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    private fun extractFiles(fileModel: FileModel) {
        viewModelScope.launch {
            val parent = breadcrumbs[selectedBreadcrumb].fileModel
            val taskId = explorerRepository.extractFiles(fileModel, parent)
            val screen = ExplorerScreen.TaskDialogScreen(taskId)
            _viewEvent.send(ViewEvent.Navigation(screen))

            resetBuffer()

            taskManager.monitor(taskId).collect { task ->
                when (val status = task.status) {
                    is TaskStatus.Error -> onTaskFailed(status.exception)
                    is TaskStatus.Done -> onTaskFinished()
                    else -> Unit
                }
            }
        }
    }

    private fun loadFiles(parent: FileModel, fromUser: Boolean) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                /** Check if [parent] is already added to breadcrumbs */
                val existingIndex = breadcrumbs.indexOf { it.fileModel.fileUri == parent.fileUri }
                if (existingIndex == selectedBreadcrumb && selectedBreadcrumb != -1 && fromUser) {
                    return@launch
                }

                if (existingIndex != -1) {
                    /** Refresh directory, don't open a new tab */
                    selectedBreadcrumb = existingIndex
                } else {
                    /** Remove all tabs after the selected one, insert empty tree at the end */
                    val fromIndex = 0
                    val toIndex = if (selectedBreadcrumb > -1) selectedBreadcrumb + 1 else 0
                    val newState = BreadcrumbState(
                        fileModel = parent,
                        fileList = emptyList(),
                        errorState = null,
                    )
                    breadcrumbs = breadcrumbs.subList(fromIndex, toIndex) + newState
                    selectedBreadcrumb = breadcrumbs.size - 1
                }

                _viewState.update {
                    it.copy(
                        breadcrumbs = breadcrumbs,
                        selectedBreadcrumb = selectedBreadcrumb,
                        isLoading = true,
                    )
                }

                val fileList = explorerRepository.listFiles(parent)
                ensureActive()

                breadcrumbs = breadcrumbs.mapSelected {
                    it.copy(
                        fileList = fileList,
                        errorState = null,
                    )
                }

                _viewState.update {
                    it.copy(
                        breadcrumbs = breadcrumbs.mapSelected { state ->
                            state.copy(fileList = state.fileList.applyFilter())
                        },
                        selectedBreadcrumb = selectedBreadcrumb,
                        isLoading = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, e.message)

                /** Clear list and show error */
                breadcrumbs = breadcrumbs.mapSelected {
                    it.copy(
                        fileList = emptyList(),
                        errorState = errorState(e),
                    )
                }

                _viewState.update {
                    it.copy(
                        breadcrumbs = breadcrumbs,
                        selectedBreadcrumb = selectedBreadcrumb,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun loadFilesystems() {
        viewModelScope.launch {
            try {
                filesystems = explorerRepository.loadFilesystems()

                val filesystemModel = filesystems.find { it.uuid == selectedFilesystem }
                    ?: filesystems.first()

                breadcrumbs = explorerRepository.loadBreadcrumbs(filesystemModel).mapBreadcrumbs()
                selectedBreadcrumb = breadcrumbs.size - 1

                _viewState.update {
                    it.copy(
                        filesystems = filesystems,
                        selectedFilesystem = selectedFilesystem,
                        breadcrumbs = breadcrumbs,
                        selectedBreadcrumb = selectedBreadcrumb,
                        searchQuery = searchQuery,
                        showHidden = showHidden,
                        sortMode = sortMode,
                        viewMode = viewMode,
                    )
                }
                onRefreshClicked()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, e.message)
                _viewEvent.send(ViewEvent.Toast(e.message.orEmpty()))
            }
        }
    }

    private fun resetBuffer() {
        taskType = TaskType.CREATE
        taskBuffer = emptyList()
        selectedFiles = emptyList()
        _viewState.update {
            it.copy(
                taskType = taskType,
                selectedFiles = selectedFiles,
            )
        }
    }

    private fun errorState(e: Exception): ErrorState {
        return when (e) {
            is PermissionException -> ErrorState(
                icon = UiR.drawable.ic_file_error,
                title = stringProvider.getString(R.string.message_access_denied),
                subtitle = stringProvider.getString(R.string.message_access_required),
                action = ErrorAction.REQUEST_PERMISSIONS,
            )

            is AuthRequiredException -> ErrorState(
                icon = UiR.drawable.ic_file_error,
                title = stringProvider.getString(R.string.message_auth_required),
                subtitle = when (e.authMethod) {
                    AuthMethod.PASSWORD -> stringProvider.getString(R.string.message_enter_password)
                    AuthMethod.KEY -> stringProvider.getString(R.string.message_enter_passphrase)
                },
                action = when (e.authMethod) {
                    AuthMethod.PASSWORD -> ErrorAction.ENTER_PASSWORD
                    AuthMethod.KEY -> ErrorAction.ENTER_PASSPHRASE
                }
            )

            is AuthenticationException -> ErrorState(
                icon = UiR.drawable.ic_file_error,
                title = stringProvider.getString(UiR.string.common_error_occurred),
                subtitle = when (e.authMethod) {
                    AuthMethod.PASSWORD -> stringProvider.getString(R.string.message_enter_password)
                    AuthMethod.KEY -> stringProvider.getString(R.string.message_enter_passphrase)
                },
                action = when (e.authMethod) {
                    AuthMethod.PASSWORD -> ErrorAction.ENTER_PASSWORD
                    AuthMethod.KEY -> ErrorAction.ENTER_PASSPHRASE
                }
            )

            else -> ErrorState(
                icon = UiR.drawable.ic_file_error,
                title = stringProvider.getString(UiR.string.common_error_occurred),
                subtitle = e.message.orEmpty(),
                action = ErrorAction.UNDEFINED,
            )
        }
    }

    private suspend fun onTaskFinished() {
        val message = stringProvider.getString(R.string.message_done)
        _viewEvent.send(ViewEvent.Toast(message))
        onRefreshClicked()
    }

    private suspend fun onTaskFailed(e: Exception) {
        when (e) {
            is FileNotFoundException -> {
                val message = stringProvider.getString(R.string.message_file_not_found)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is FileAlreadyExistsException -> {
                val message = stringProvider.getString(R.string.message_file_already_exists)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is UnsupportedArchiveException -> {
                val message = stringProvider.getString(R.string.message_unsupported_archive)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is EncryptedArchiveException -> {
                val message = stringProvider.getString(R.string.message_encrypted_archive)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is SplitArchiveException -> {
                val message = stringProvider.getString(R.string.message_split_archive)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is InvalidArchiveException -> {
                val message = stringProvider.getString(R.string.message_invalid_archive)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is UnsupportedOperationException -> {
                val message = stringProvider.getString(R.string.message_operation_not_supported)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            is CancellationException -> {
                val message = stringProvider.getString(R.string.message_operation_cancelled)
                _viewEvent.send(ViewEvent.Toast(message))
            }

            else -> {
                _viewEvent.send(ViewEvent.Toast(e.message.toString()))
            }
        }
        onRefreshClicked()
    }

    private fun List<FileModel>.mapBreadcrumbs(): List<BreadcrumbState> {
        return map { fileModel ->
            BreadcrumbState(fileModel = fileModel)
        }
    }

    private inline fun List<BreadcrumbState>.mapSelected(
        predicate: (BreadcrumbState) -> BreadcrumbState
    ): List<BreadcrumbState> {
        return mapIndexed { index, state ->
            if (index == selectedBreadcrumb) {
                predicate(state)
            } else {
                state
            }
        }
    }

    private fun List<FileModel>.applyFilter(): List<FileModel> {
        return filter { it.name.contains(searchQuery, ignoreCase = true) }
            .filter { if (it.isHidden) showHidden else true }
            .sortedWith(fileComparator(settingsManager.sortMode))
            .sortedBy { it.directory != settingsManager.foldersOnTop }
    }

    class Factory : ViewModelProvider.Factory {

        @Inject
        lateinit var viewModelProvider: Provider<ExplorerViewModel>

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModelProvider.get() as T
        }
    }
}
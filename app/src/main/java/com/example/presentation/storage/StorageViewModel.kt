package com.example.presentation.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.result.AppResult
import com.example.domain.model.storage.RemoteFile
import com.example.domain.model.storage.UploadFileRequest
import com.example.domain.usecase.storage.DeleteStorageFileUseCase
import com.example.domain.usecase.storage.GetStorageFileUrlUseCase
import com.example.domain.usecase.storage.ListStorageFilesUseCase
import com.example.domain.usecase.storage.UploadStorageFileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StorageViewModel(
    private val uploadStorageFileUseCase: UploadStorageFileUseCase,
    private val getStorageFileUrlUseCase: GetStorageFileUrlUseCase,
    private val deleteStorageFileUseCase: DeleteStorageFileUseCase,
    private val listStorageFilesUseCase: ListStorageFilesUseCase
) : ViewModel() {

    private val _files = MutableStateFlow<List<RemoteFile>>(emptyList())
    val files: StateFlow<List<RemoteFile>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun uploadFile(
        bucket: String = "documents",
        path: String,
        data: ByteArray,
        mimeType: String = "application/octet-stream"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val request = UploadFileRequest(
                bucket = bucket,
                path = path,
                data = data,
                mimeType = mimeType
            )
            when (val result = uploadStorageFileUseCase(request)) {
                is AppResult.Success -> {
                    _statusMessage.value = "تم رفع الملف '${result.data.name}' بنجاح"
                    loadFiles(bucket)
                }
                is AppResult.Failure -> {
                    _errorMessage.value = result.error.message
                }
            }
            _isLoading.value = false
        }
    }

    fun loadFiles(bucket: String = "documents") {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = listStorageFilesUseCase(bucket)) {
                is AppResult.Success -> {
                    _files.value = result.data
                }
                is AppResult.Failure -> {
                    _errorMessage.value = result.error.message
                }
            }
            _isLoading.value = false
        }
    }

    fun deleteFile(bucket: String = "documents", path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = deleteStorageFileUseCase(bucket, path)) {
                is AppResult.Success -> {
                    _statusMessage.value = "تم حذف الملف بنجاح"
                    loadFiles(bucket)
                }
                is AppResult.Failure -> {
                    _errorMessage.value = result.error.message
                }
            }
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}

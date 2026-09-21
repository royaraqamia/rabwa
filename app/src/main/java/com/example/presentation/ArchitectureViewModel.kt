package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.dispatcher.CoroutineDispatchers
import com.example.core.result.AppError
import com.example.core.result.AppResult
import com.example.domain.model.ArchitectureRecord
import com.example.domain.model.LayerHealth
import com.example.domain.model.NetworkStatus
import com.example.domain.model.SyncStatus
import com.example.domain.model.SyncSummary
import com.example.domain.usecase.AddArchitectureRecordUseCase
import com.example.domain.usecase.ClearRecordsUseCase
import com.example.domain.usecase.GetLayerHealthUseCase
import com.example.domain.usecase.ObserveNetworkStatusUseCase
import com.example.domain.usecase.ObserveRecordsUseCase
import com.example.domain.usecase.ObserveSyncSummaryUseCase
import com.example.domain.usecase.SyncOfflineRecordsUseCase
import com.example.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchitectureViewModel(
    private val getLayerHealthUseCase: GetLayerHealthUseCase,
    private val addArchitectureRecordUseCase: AddArchitectureRecordUseCase,
    private val observeRecordsUseCase: ObserveRecordsUseCase,
    private val clearRecordsUseCase: ClearRecordsUseCase,
    private val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase,
    private val syncOfflineRecordsUseCase: SyncOfflineRecordsUseCase? = null,
    private val observeSyncSummaryUseCase: ObserveSyncSummaryUseCase? = null,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    constructor(
        getLayerHealthUseCase: GetLayerHealthUseCase,
        addArchitectureRecordUseCase: AddArchitectureRecordUseCase,
        observeRecordsUseCase: ObserveRecordsUseCase,
        clearRecordsUseCase: ClearRecordsUseCase,
        observeNetworkStatusUseCase: ObserveNetworkStatusUseCase,
        dispatchers: CoroutineDispatchers
    ) : this(
        getLayerHealthUseCase = getLayerHealthUseCase,
        addArchitectureRecordUseCase = addArchitectureRecordUseCase,
        observeRecordsUseCase = observeRecordsUseCase,
        clearRecordsUseCase = clearRecordsUseCase,
        observeNetworkStatusUseCase = observeNetworkStatusUseCase,
        syncOfflineRecordsUseCase = null,
        observeSyncSummaryUseCase = null,
        dispatchers = dispatchers
    )

    constructor(
        getLayerHealthUseCase: GetLayerHealthUseCase,
        addArchitectureRecordUseCase: AddArchitectureRecordUseCase,
        observeRecordsUseCase: ObserveRecordsUseCase,
        clearRecordsUseCase: ClearRecordsUseCase,
        dispatchers: CoroutineDispatchers
    ) : this(
        getLayerHealthUseCase = getLayerHealthUseCase,
        addArchitectureRecordUseCase = addArchitectureRecordUseCase,
        observeRecordsUseCase = observeRecordsUseCase,
        clearRecordsUseCase = clearRecordsUseCase,
        observeNetworkStatusUseCase = ObserveNetworkStatusUseCase(object : com.example.domain.network.ConnectivityMonitor {
            override val networkStatus = kotlinx.coroutines.flow.flowOf(NetworkStatus.Available)
            override val isCurrentlyConnected: Boolean = true
        }),
        syncOfflineRecordsUseCase = null,
        observeSyncSummaryUseCase = null,
        dispatchers = dispatchers
    )

    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _syncSummary = MutableStateFlow(SyncSummary())
    val syncSummary: StateFlow<SyncSummary> = _syncSummary.asStateFlow()

    private val _healthState = MutableStateFlow<UiState<List<LayerHealth>>>(UiState.Loading)
    val healthState: StateFlow<UiState<List<LayerHealth>>> = _healthState.asStateFlow()

    private val _inputTitle = MutableStateFlow("")
    val inputTitle: StateFlow<String> = _inputTitle.asStateFlow()

    private val _actionFeedback = MutableStateFlow<AppResult<String>?>(null)
    val actionFeedback: StateFlow<AppResult<String>?> = _actionFeedback.asStateFlow()

    private val _globalError = MutableStateFlow<AppError?>(null)
    val globalError: StateFlow<AppError?> = _globalError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val records: StateFlow<List<ArchitectureRecord>> = observeRecordsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadLayerHealth()
        observeNetworkConnectivity()
        observeSyncSummary()
    }

    private fun observeSyncSummary() {
        if (observeSyncSummaryUseCase == null) return
        viewModelScope.launch(dispatchers.main) {
            observeSyncSummaryUseCase().collect { summary ->
                _syncSummary.value = summary
            }
        }
    }

    private fun observeNetworkConnectivity() {
        viewModelScope.launch(dispatchers.main) {
            observeNetworkStatusUseCase().collect { status ->
                val wasDisconnected = !_networkStatus.value.isConnected
                _networkStatus.value = status
                _isOffline.value = !status.isConnected

                // In an Offline-First architecture, lack of network is an operational state, NOT a fatal error.
                if (status.isConnected) {
                    val current = _globalError.value
                    if (current is AppError.Network) {
                        _globalError.value = null
                    }
                    if (wasDisconnected) {
                        // Connection restored! Auto-trigger non-blocking sync for any pending offline records
                        syncPendingRecords(showFeedbackIfEmpty = false)
                    }
                }
            }
        }
    }

    fun syncPendingRecords(showFeedbackIfEmpty: Boolean = false) {
        if (syncOfflineRecordsUseCase == null) return
        if (!_networkStatus.value.isConnected) {
            _actionFeedback.value = AppResult.Failure(
                AppError.Network(isNoInternet = true, message = "لا يمكن المزامنة حالياً لعدم توفر اتصال بالإنترنت.")
            )
            return
        }

        viewModelScope.launch(dispatchers.main) {
            _isSyncing.value = true
            when (val result = syncOfflineRecordsUseCase()) {
                is AppResult.Success -> {
                    if (result.data > 0) {
                        _actionFeedback.value = AppResult.Success("تمت مزامنة ${result.data} ملاحظة بنجاح.")
                    } else if (showFeedbackIfEmpty) {
                        _actionFeedback.value = AppResult.Success("جميع الملاحظات متزامنة.")
                    }
                }
                is AppResult.Failure -> {
                    _actionFeedback.value = AppResult.Failure(result.error)
                }
            }
            _isSyncing.value = false
        }
    }

    fun loadLayerHealth() {
        viewModelScope.launch(dispatchers.main) {
            _healthState.value = UiState.Loading
            when (val result = getLayerHealthUseCase()) {
                is AppResult.Success -> {
                    _healthState.value = UiState.Success(result.data)
                }
                is AppResult.Failure -> {
                    _healthState.value = UiState.Error(result.error)
                    _globalError.value = result.error
                }
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _inputTitle.value = newTitle
        if (_actionFeedback.value != null) {
            _actionFeedback.value = null
        }
    }

    fun submitRecord() {
        val currentInput = _inputTitle.value
        viewModelScope.launch(dispatchers.main) {
            _isSubmitting.value = true
            when (val result = addArchitectureRecordUseCase(currentInput)) {
                is AppResult.Success -> {
                    _inputTitle.value = ""
                    val isOfflineNow = !_networkStatus.value.isConnected || result.data.syncStatus == SyncStatus.PENDING_SYNC
                    val feedbackMsg = if (isOfflineNow) {
                        "تم الحفظ محلياً بنجاح (سيتم المزامنة تلقائياً عند الاتصال)."
                    } else {
                        "تمت إضافة '${result.data.title}' ومزامنتها بنجاح."
                    }
                    _actionFeedback.value = AppResult.Success(feedbackMsg)
                }
                is AppResult.Failure -> {
                    val userFriendlyMessage = when (result.error) {
                        is AppError.Validation -> "يرجى كتابة نص بين حرفين و 60 حرفاً."
                        is AppError.Database -> "تعذر حفظ الملاحظة، يرجى المحاولة مجدداً."
                        else -> "حدث خطأ أثناء الحفظ، يرجى المحاولة مجدداً."
                    }
                    _actionFeedback.value = AppResult.Failure(AppError.Validation(field = "title", message = userFriendlyMessage))
                }
            }
            _isSubmitting.value = false
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatchers.main) {
            _isClearing.value = true
            when (val result = clearRecordsUseCase()) {
                is AppResult.Success -> {
                    _actionFeedback.value = AppResult.Success("تم مسح كافة الملاحظات بنجاح.")
                }
                is AppResult.Failure -> {
                    _actionFeedback.value = AppResult.Failure(AppError.Database("تعذر مسح الملاحظات."))
                }
            }
            _isClearing.value = false
        }
    }

    fun dismissFeedback() {
        _actionFeedback.value = null
    }

    fun dismissGlobalError() {
        _globalError.value = null
    }

    fun setGlobalError(error: AppError) {
        _globalError.value = error
    }
}

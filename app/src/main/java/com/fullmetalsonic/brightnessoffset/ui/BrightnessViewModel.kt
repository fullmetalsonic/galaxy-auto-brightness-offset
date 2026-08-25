package com.fullmetalsonic.brightnessoffset.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fullmetalsonic.brightnessoffset.data.BrightnessRepository
import com.fullmetalsonic.brightnessoffset.data.BrightnessRepositoryContract
import com.fullmetalsonic.brightnessoffset.diagnostics.DiagnosticReport
import com.fullmetalsonic.brightnessoffset.domain.AdjustmentScale
import com.fullmetalsonic.brightnessoffset.domain.BrightnessSnapshot
import com.fullmetalsonic.brightnessoffset.domain.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BrightnessUiState(
    val snapshot: BrightnessSnapshot = BrightnessSnapshot(
        canWriteSettings = false,
        isAutomaticMode = false,
        currentAdjustment = 0f,
        isManaged = false,
        originalAdjustment = null,
        lastAppliedAdjustment = null,
        restoreOnBoot = false,
        externalChangeDetected = false,
    ),
    val draftAdjustment: Float = 0f,
    val isLoading: Boolean = true,
    val isApplying: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
)

class BrightnessViewModel(
    private val repository: BrightnessRepositoryContract,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BrightnessUiState())
    val uiState: StateFlow<BrightnessUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) { repository.snapshot() }
            _uiState.update { previous ->
                previous.copy(
                    snapshot = snapshot,
                    draftAdjustment = AdjustmentScale.normalize(snapshot.currentAdjustment),
                    isLoading = false,
                )
            }
        }
    }

    fun updateDraft(value: Float) {
        _uiState.update { it.copy(draftAdjustment = AdjustmentScale.normalize(value)) }
    }

    fun applyDraft() {
        val target = _uiState.value.draftAdjustment
        if (_uiState.value.isApplying) return
        _uiState.update { it.copy(isApplying = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repository.applyAdjustment(target) }
            handleResult(result)
        }
    }

    fun restoreOriginal() {
        if (_uiState.value.isApplying) return
        _uiState.update { it.copy(isApplying = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repository.restoreOriginal() }
            handleResult(result)
        }
    }

    fun setRestoreOnBoot(enabled: Boolean) {
        repository.setRestoreOnBoot(enabled)
        refresh()
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null, messageIsError = false) }
    }

    fun diagnosticText(): String = DiagnosticReport.create(_uiState.value.snapshot)

    private suspend fun handleResult(result: OperationResult) {
        val snapshot = withContext(Dispatchers.IO) { repository.snapshot() }
        _uiState.update { previous ->
            when (result) {
                is OperationResult.Success -> previous.copy(
                    snapshot = snapshot,
                    draftAdjustment = AdjustmentScale.normalize(result.verifiedValue),
                    isApplying = false,
                    message = result.message,
                    messageIsError = false,
                )
                is OperationResult.Failure -> previous.copy(
                    snapshot = snapshot,
                    draftAdjustment = AdjustmentScale.normalize(snapshot.currentAdjustment),
                    isApplying = false,
                    message = result.message,
                    messageIsError = true,
                )
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BrightnessViewModel(
                        BrightnessRepository(application.applicationContext),
                    ) as T
                }
            }
    }
}

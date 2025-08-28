package com.mobitech.inventarioabc.ui.leitura

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobitech.inventarioabc.data.repository.InventoryRepository
import com.mobitech.inventarioabc.domain.usecase.ConfirmReadUseCase
import com.mobitech.inventarioabc.domain.usecase.LoadFromCsvUseCase
import kotlinx.coroutines.launch

class LeituraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(application)
    private val loadFromCsvUseCase = LoadFromCsvUseCase(repository)
    private val confirmReadUseCase = ConfirmReadUseCase(repository)

    private val _scannedCode = MutableLiveData<String>()
    val scannedCode: LiveData<String> = _scannedCode

    private val _confirmResult = MutableLiveData<ConfirmResult>()
    val confirmResult: LiveData<ConfirmResult> = _confirmResult

    private val _needsFolderSelection = MutableLiveData<Boolean>()
    val needsFolderSelection: LiveData<Boolean> = _needsFolderSelection

    data class ConfirmResult(
        val success: Boolean,
        val message: String,
        val isDuplicate: Boolean = false,
        val existingQuantity: Int = 0,
        val backupCreated: Boolean = false
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            if (repository.hasFolderPermission()) {
                loadFromCsvUseCase.execute()
            }
        }
    }

    fun onCodeScanned(code: String) {
        _scannedCode.value = code
    }

    fun confirmRead(codigo: String, quantidade: Int) {
        if (quantidade <= 0) {
            _confirmResult.value = ConfirmResult(
                success = false,
                message = "Quantidade deve ser maior que 0"
            )
            return
        }

        viewModelScope.launch {
            try {
                val result = confirmReadUseCase.execute(codigo, quantidade)

                if (result.success) {
                    if (result.isNewItem) {
                        val message = if (result.backupCreated) {
                            "Lido e salvo (backup criado)"
                        } else {
                            "Lido e salvo"
                        }
                        _confirmResult.value = ConfirmResult(
                            success = true,
                            message = message,
                            backupCreated = result.backupCreated
                        )
                    } else {
                        // Item já existe - mostrar diálogo
                        val existingItem = repository.getItem(codigo)
                        _confirmResult.value = ConfirmResult(
                            success = true,
                            message = "",
                            isDuplicate = true,
                            existingQuantity = existingItem?.quantidade ?: 0
                        )
                    }
                } else {
                    _confirmResult.value = ConfirmResult(
                        success = false,
                        message = "Erro ao salvar"
                    )
                }
            } catch (e: Exception) {
                _confirmResult.value = ConfirmResult(
                    success = false,
                    message = "Erro inesperado"
                )
            }
        }
    }

    fun updateExistingItem(codigo: String, quantidade: Int) {
        viewModelScope.launch {
            val success = repository.updateQuantity(codigo, quantidade)
            _confirmResult.value = ConfirmResult(
                success = success,
                message = if (success) "Quantidade atualizada" else "Erro ao atualizar"
            )
        }
    }

    fun onFolderSelected(uri: android.net.Uri) {
        repository.setFolderUri(uri)
        loadData()
        _needsFolderSelection.value = false
    }

    fun requestFolderSelection() { _needsFolderSelection.value = true }

    fun hasFolderPermission(): Boolean = repository.hasFolderPermission()
}

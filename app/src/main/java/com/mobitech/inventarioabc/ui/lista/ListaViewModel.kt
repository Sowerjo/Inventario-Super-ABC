package com.mobitech.inventarioabc.ui.lista

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobitech.inventarioabc.data.repository.InventoryRepository
import com.mobitech.inventarioabc.domain.model.InventoryItem
import com.mobitech.inventarioabc.domain.usecase.DeleteItemUseCase
import com.mobitech.inventarioabc.domain.usecase.EditQuantityUseCase
import com.mobitech.inventarioabc.domain.usecase.LoadFromCsvUseCase
import kotlinx.coroutines.launch

class ListaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(application)
    private val editQuantityUseCase = EditQuantityUseCase(repository)
    private val deleteItemUseCase = DeleteItemUseCase(repository)
    private val loadFromCsvUseCase = LoadFromCsvUseCase(repository)

    private val _items = MutableLiveData<List<InventoryItem>>()
    val items: LiveData<List<InventoryItem>> = _items

    private val _actionResult = MutableLiveData<ActionResult>()
    val actionResult: LiveData<ActionResult> = _actionResult

    data class ActionResult(
        val success: Boolean,
        val message: String
    )

    private var allItems: List<InventoryItem> = emptyList()
    private var currentQuery: String = ""

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            try {
                // Sempre forçar o carregamento completo do CSV para garantir sincronização
                if (repository.hasFolderPermission()) {
                    repository.load() // Força carregamento completo do CSV
                }
                val map = repository.getAllItems()
                // Ordenar por data/hora mais recente primeiro (descendente)
                allItems = map.values.sortedByDescending { it.dataHoraEpochMillis }
                applyFilter()
            } catch (e: Exception) {
                _actionResult.value = ActionResult(
                    success = false,
                    message = "Erro ao carregar itens: ${e.message}"
                )
            }
        }
    }

    private fun applyFilter() {
        val filtered = if (currentQuery.isBlank()) {
            allItems
        } else {
            allItems.filter { it.codigo.contains(currentQuery, ignoreCase = true) }
        }
        _items.value = filtered
    }

    fun setSearchQuery(query: String) {
        currentQuery = query.trim()
        applyFilter()
    }

    fun editQuantity(codigo: String, newQuantity: Int) {
        viewModelScope.launch {
            val result = editQuantityUseCase.execute(codigo, newQuantity)
            _actionResult.value = ActionResult(
                success = result.success,
                message = if (result.success) "Atualizado" else (result.errorMessage ?: "Erro ao atualizar")
            )

            if (result.success) {
                loadItems()
            }
        }
    }

    fun deleteItem(codigo: String) {
        viewModelScope.launch {
            val result = deleteItemUseCase.execute(codigo)
            _actionResult.value = ActionResult(
                success = result.success,
                message = if (result.success) "Excluído" else (result.errorMessage ?: "Erro ao excluir")
            )

            if (result.success) {
                loadItems()
            }
        }
    }

    fun refreshItems() {
        loadItems()
    }
}

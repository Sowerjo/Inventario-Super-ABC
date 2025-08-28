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
            if (repository.hasFolderPermission()) {
                // Recarrega do CSV para garantir sincronização entre telas
                loadFromCsvUseCase.execute()
            }
            val map = repository.getAllItems()
            allItems = map.values.sortedByDescending { it.dataHoraEpochMillis }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = if (currentQuery.isBlank()) allItems else allItems.filter { it.codigo.contains(currentQuery, ignoreCase = true) }
        _items.value = filtered
    }

    fun setSearchQuery(query: String) {
        currentQuery = query.trim()
        applyFilter()
    }

    fun editQuantity(codigo: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            _actionResult.value = ActionResult(
                success = false,
                message = "Quantidade deve ser maior que 0"
            )
            return
        }

        viewModelScope.launch {
            val success = editQuantityUseCase.execute(codigo, newQuantity)
            _actionResult.value = ActionResult(
                success = success,
                message = if (success) "Atualizado" else "Erro ao atualizar"
            )

            if (success) {
                loadItems()
            } else {
                applyFilter()
            }
        }
    }

    fun deleteItem(codigo: String) {
        viewModelScope.launch {
            val success = deleteItemUseCase.execute(codigo)
            _actionResult.value = ActionResult(
                success = success,
                message = if (success) "Excluído" else "Erro ao excluir"
            )

            if (success) {
                loadItems()
            } else {
                applyFilter()
            }
        }
    }

    fun refreshItems() {
        loadItems()
    }
}

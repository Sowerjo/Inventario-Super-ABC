package com.mobitech.inventarioabc.domain.usecase

import android.util.Log
import com.mobitech.inventarioabc.data.repository.InventoryRepository

class EditQuantityUseCase(private val repository: InventoryRepository) {
    companion object {
        private const val TAG = "EditQuantityUseCase"
    }

    data class Result(
        val success: Boolean,
        val errorMessage: String? = null
    )

    fun execute(codigo: String, quantidade: Int): Result {
        return try {
            if (codigo.isBlank()) {
                Log.w(TAG, "Código vazio fornecido para edição")
                return Result(success = false, errorMessage = "Código não pode estar vazio")
            }

            if (quantidade <= 0) {
                Log.w(TAG, "Quantidade inválida para edição: $quantidade")
                return Result(success = false, errorMessage = "Quantidade deve ser maior que zero")
            }

            val existingItem = repository.getItem(codigo)
            if (existingItem == null) {
                Log.w(TAG, "Tentativa de editar item inexistente: $codigo")
                return Result(success = false, errorMessage = "Item não encontrado")
            }

            val success = repository.updateQuantity(codigo, quantidade)
            if (success) {
                Log.d(TAG, "Quantidade editada com sucesso para item $codigo: $quantidade")
                Result(success = true)
            } else {
                Log.e(TAG, "Falha ao editar quantidade do item: $codigo")
                Result(success = false, errorMessage = "Erro ao salvar alteração no arquivo CSV")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao editar quantidade: ${e.message}", e)
            Result(success = false, errorMessage = "Erro interno: ${e.message}")
        }
    }
}

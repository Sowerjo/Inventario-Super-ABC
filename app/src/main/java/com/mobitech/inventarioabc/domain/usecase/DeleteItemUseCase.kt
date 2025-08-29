package com.mobitech.inventarioabc.domain.usecase

import android.util.Log
import com.mobitech.inventarioabc.data.repository.InventoryRepository

class DeleteItemUseCase(private val repository: InventoryRepository) {
    companion object {
        private const val TAG = "DeleteItemUseCase"
    }

    data class Result(
        val success: Boolean,
        val errorMessage: String? = null
    )

    fun execute(codigo: String): Result {
        return try {
            if (codigo.isBlank()) {
                Log.w(TAG, "Código vazio fornecido para exclusão")
                return Result(success = false, errorMessage = "Código não pode estar vazio")
            }

            val existingItem = repository.getItem(codigo)
            if (existingItem == null) {
                Log.w(TAG, "Tentativa de deletar item inexistente: $codigo")
                return Result(success = false, errorMessage = "Item não encontrado")
            }

            val success = repository.delete(codigo)
            if (success) {
                Log.d(TAG, "Item deletado com sucesso: $codigo")
                Result(success = true)
            } else {
                Log.e(TAG, "Falha ao deletar item: $codigo")
                Result(success = false, errorMessage = "Erro ao salvar alteração no arquivo CSV")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar item: ${e.message}", e)
            Result(success = false, errorMessage = "Erro interno: ${e.message}")
        }
    }
}

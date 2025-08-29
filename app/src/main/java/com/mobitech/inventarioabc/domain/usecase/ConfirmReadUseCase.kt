package com.mobitech.inventarioabc.domain.usecase

import android.util.Log
import com.mobitech.inventarioabc.data.repository.InventoryRepository
import com.mobitech.inventarioabc.domain.model.InventoryItem

class ConfirmReadUseCase(private val repository: InventoryRepository) {
    companion object {
        private const val TAG = "ConfirmReadUseCase"
    }

    data class Result(
        val success: Boolean,
        val isNewItem: Boolean,
        val backupCreated: Boolean = false,
        val errorMessage: String? = null
    )

    fun execute(codigo: String, quantidade: Int): Result {
        return try {
            if (codigo.isBlank()) {
                Log.w(TAG, "Código vazio fornecido")
                return Result(success = false, isNewItem = false, errorMessage = "Código não pode estar vazio")
            }

            if (quantidade <= 0) {
                Log.w(TAG, "Quantidade inválida: $quantidade")
                return Result(success = false, isNewItem = false, errorMessage = "Quantidade deve ser maior que zero")
            }

            val existingItem = repository.getItem(codigo)

            if (existingItem == null) {
                // Novo item
                val newItem = InventoryItem(
                    codigo = codigo,
                    quantidade = quantidade,
                    dataHoraEpochMillis = System.currentTimeMillis()
                )

                val saved = repository.upsert(newItem)
                if (saved) {
                    Log.d(TAG, "Novo item salvo: $codigo com quantidade $quantidade")

                    val readCount = repository.incrementReadCounter()
                    val backupCreated = if (readCount >= 5) {
                        val backupSuccess = repository.createBackup()
                        if (backupSuccess) {
                            repository.resetReadCounter()
                            Log.d(TAG, "Backup criado após 5 leituras")
                        } else {
                            Log.w(TAG, "Falha ao criar backup após 5 leituras")
                        }
                        backupSuccess
                    } else {
                        false
                    }

                    Result(success = true, isNewItem = true, backupCreated = backupCreated)
                } else {
                    Log.e(TAG, "Falha ao salvar novo item: $codigo")
                    Result(success = false, isNewItem = true, errorMessage = "Erro ao salvar no arquivo CSV")
                }
            } else {
                // Item já existe
                Log.d(TAG, "Item já existe: $codigo")
                Result(success = true, isNewItem = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao confirmar leitura: ${e.message}", e)
            Result(success = false, isNewItem = false, errorMessage = "Erro interno: ${e.message}")
        }
    }
}

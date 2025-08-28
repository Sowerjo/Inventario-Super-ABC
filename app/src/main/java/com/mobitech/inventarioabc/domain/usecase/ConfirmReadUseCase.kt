package com.mobitech.inventarioabc.domain.usecase

import com.mobitech.inventarioabc.data.repository.InventoryRepository
import com.mobitech.inventarioabc.domain.model.InventoryItem

class ConfirmReadUseCase(private val repository: InventoryRepository) {

    data class Result(
        val success: Boolean,
        val isNewItem: Boolean,
        val backupCreated: Boolean = false
    )

    fun execute(codigo: String, quantidade: Int): Result {
        val existingItem = repository.getItem(codigo)

        return if (existingItem == null) {
            // Novo item
            val newItem = InventoryItem(
                codigo = codigo,
                quantidade = quantidade,
                dataHoraEpochMillis = System.currentTimeMillis()
            )

            val saved = repository.upsert(newItem)
            if (saved) {
                val readCount = repository.incrementReadCounter()
                val backupCreated = if (readCount >= 5) {
                    repository.createBackup().also {
                        if (it) repository.resetReadCounter()
                    }
                } else false

                Result(success = true, isNewItem = true, backupCreated = backupCreated)
            } else {
                Result(success = false, isNewItem = true)
            }
        } else {
            // Item já existe
            Result(success = true, isNewItem = false)
        }
    }
}

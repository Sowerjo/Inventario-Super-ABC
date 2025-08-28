package com.mobitech.inventarioabc.domain.usecase

import com.mobitech.inventarioabc.data.repository.InventoryRepository

class DeleteItemUseCase(private val repository: InventoryRepository) {

    fun execute(codigo: String): Boolean {
        return repository.delete(codigo)
    }
}

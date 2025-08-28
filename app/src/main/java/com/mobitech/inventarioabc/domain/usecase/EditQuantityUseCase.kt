package com.mobitech.inventarioabc.domain.usecase

import com.mobitech.inventarioabc.data.repository.InventoryRepository

class EditQuantityUseCase(private val repository: InventoryRepository) {

    fun execute(codigo: String, quantidade: Int): Boolean {
        return repository.updateQuantity(codigo, quantidade)
    }
}

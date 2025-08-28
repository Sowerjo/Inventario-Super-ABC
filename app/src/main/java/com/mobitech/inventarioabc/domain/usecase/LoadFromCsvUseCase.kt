package com.mobitech.inventarioabc.domain.usecase

import com.mobitech.inventarioabc.data.repository.InventoryRepository

class LoadFromCsvUseCase(private val repository: InventoryRepository) {

    fun execute() = repository.load()
}

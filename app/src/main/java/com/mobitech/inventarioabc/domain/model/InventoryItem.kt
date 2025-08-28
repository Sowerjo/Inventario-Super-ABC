package com.mobitech.inventarioabc.domain.model

data class InventoryItem(
    val codigo: String,
    val quantidade: Int,
    val dataHoraEpochMillis: Long
)

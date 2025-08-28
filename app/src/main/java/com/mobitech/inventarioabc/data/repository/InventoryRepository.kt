package com.mobitech.inventarioabc.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.mobitech.inventarioabc.data.csv.CsvStorage
import com.mobitech.inventarioabc.domain.model.InventoryItem

class InventoryRepository(context: Context) {
    companion object {
        private const val PREFS_NAME = "inventory_counter"
        private const val PREF_READS_SINCE_BACKUP = "reads_since_last_backup"
    }

    private val csvStorage = CsvStorage(context)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val items = mutableMapOf<String, InventoryItem>()

    fun load(): Map<String, InventoryItem> {
        val loadedItems = csvStorage.loadItemsFromCsv()
        items.clear()
        items.putAll(loadedItems)
        return items.toMap()
    }

    fun getAllItems(): Map<String, InventoryItem> = items.toMap()

    fun getItem(codigo: String): InventoryItem? = items[codigo]

    fun upsert(item: InventoryItem): Boolean {
        items[item.codigo] = item
        return saveAll()
    }

    fun updateQuantity(codigo: String, quantidade: Int): Boolean {
        val existingItem = items[codigo] ?: return false
        val updatedItem = existingItem.copy(
            quantidade = quantidade,
            dataHoraEpochMillis = System.currentTimeMillis()
        )
        items[codigo] = updatedItem
        return saveAll()
    }

    fun delete(codigo: String): Boolean {
        items.remove(codigo)
        return saveAll()
    }

    fun incrementReadCounter(): Int {
        val currentCount = prefs.getInt(PREF_READS_SINCE_BACKUP, 0)
        val newCount = currentCount + 1
        prefs.edit().putInt(PREF_READS_SINCE_BACKUP, newCount).apply()
        return newCount
    }

    fun resetReadCounter() {
        prefs.edit().putInt(PREF_READS_SINCE_BACKUP, 0).apply()
    }

    fun getReadCounter(): Int {
        return prefs.getInt(PREF_READS_SINCE_BACKUP, 0)
    }

    fun createBackup(): Boolean {
        return csvStorage.createBackup()
    }

    fun hasFolderPermission(): Boolean {
        return csvStorage.hasFolderPermission()
    }

    fun getFolderUri() = csvStorage.getFolderUri()

    fun setFolderUri(uri: android.net.Uri) {
        csvStorage.setFolderUri(uri)
    }

    private fun saveAll(): Boolean {
        return csvStorage.saveItemsToCsv(items)
    }
}

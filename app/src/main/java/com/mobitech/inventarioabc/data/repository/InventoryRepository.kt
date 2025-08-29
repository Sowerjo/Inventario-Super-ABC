package com.mobitech.inventarioabc.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.mobitech.inventarioabc.data.csv.CsvStorage
import com.mobitech.inventarioabc.domain.model.InventoryItem

class InventoryRepository(context: Context) {
    companion object {
        private const val TAG = "InventoryRepository"
        private const val PREFS_NAME = "inventory_counter"
        private const val PREF_READS_SINCE_BACKUP = "reads_since_last_backup"
    }

    private val csvStorage = CsvStorage(context)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val items = mutableMapOf<String, InventoryItem>()
    private var isLoaded = false

    fun load(): Map<String, InventoryItem> {
        return try {
            val loadedItems = csvStorage.loadItemsFromCsv()
            items.clear()
            items.putAll(loadedItems)
            isLoaded = true
            Log.d(TAG, "Carregados ${items.size} itens na memória")
            items.toMap()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar itens: ${e.message}", e)
            emptyMap()
        }
    }

    fun getAllItems(): Map<String, InventoryItem> {
        // Sempre garantir que os dados estão carregados antes de retornar
        if (!isLoaded && hasFolderPermission()) {
            load()
        }
        return items.toMap()
    }

    fun getItem(codigo: String): InventoryItem? {
        // Sempre garantir que os dados estão carregados antes de consultar
        if (!isLoaded && hasFolderPermission()) {
            load()
        }
        return items[codigo]
    }

    fun upsert(item: InventoryItem): Boolean {
        return try {
            // Sempre carregar dados mais recentes antes de fazer upsert
            if (!isLoaded && hasFolderPermission()) {
                load()
            }

            items[item.codigo] = item
            val success = saveAll()
            if (success) {
                Log.d(TAG, "Item inserido/atualizado: ${item.codigo}")
            } else {
                Log.e(TAG, "Falha ao salvar item: ${item.codigo}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer upsert do item ${item.codigo}: ${e.message}", e)
            false
        }
    }

    fun updateQuantity(codigo: String, quantidade: Int): Boolean {
        return try {
            // Sempre carregar dados mais recentes antes de atualizar
            if (!isLoaded && hasFolderPermission()) {
                load()
            }

            val existingItem = items[codigo]
            if (existingItem == null) {
                Log.w(TAG, "Tentativa de atualizar item inexistente: $codigo")
                return false
            }

            val updatedItem = existingItem.copy(
                quantidade = quantidade
                // NÃO atualizar dataHoraEpochMillis para manter posição original na lista
            )
            items[codigo] = updatedItem

            val success = saveAll()
            if (success) {
                Log.d(TAG, "Quantidade atualizada para item $codigo: $quantidade")
            } else {
                Log.e(TAG, "Falha ao salvar atualização de quantidade para item: $codigo")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar quantidade do item $codigo: ${e.message}", e)
            false
        }
    }

    fun delete(codigo: String): Boolean {
        return try {
            // Sempre carregar dados mais recentes antes de deletar
            if (!isLoaded && hasFolderPermission()) {
                load()
            }

            val removed = items.remove(codigo)
            if (removed == null) {
                Log.w(TAG, "Tentativa de deletar item inexistente: $codigo")
                return false
            }

            val success = saveAll()
            if (success) {
                Log.d(TAG, "Item deletado: $codigo")
            } else {
                // Recolocar o item se não conseguiu salvar
                items[codigo] = removed
                Log.e(TAG, "Falha ao salvar deleção do item: $codigo")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar item $codigo: ${e.message}", e)
            false
        }
    }

    fun incrementReadCounter(): Int {
        return try {
            val currentCount = prefs.getInt(PREF_READS_SINCE_BACKUP, 0)
            val newCount = currentCount + 1
            prefs.edit().putInt(PREF_READS_SINCE_BACKUP, newCount).apply()
            Log.d(TAG, "Contador de leituras incrementado: $newCount")
            newCount
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao incrementar contador: ${e.message}", e)
            0
        }
    }

    fun resetReadCounter() {
        try {
            prefs.edit().putInt(PREF_READS_SINCE_BACKUP, 0).apply()
            Log.d(TAG, "Contador de leituras resetado")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao resetar contador: ${e.message}", e)
        }
    }

    fun getReadCounter(): Int {
        return try {
            prefs.getInt(PREF_READS_SINCE_BACKUP, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter contador: ${e.message}", e)
            0
        }
    }

    fun createBackup(): Boolean {
        return try {
            val success = csvStorage.createBackup()
            if (success) {
                Log.d(TAG, "Backup criado com sucesso")
            } else {
                Log.e(TAG, "Falha ao criar backup")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar backup: ${e.message}", e)
            false
        }
    }

    fun hasFolderPermission(): Boolean {
        return try {
            csvStorage.hasFolderPermission()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar permissão: ${e.message}", e)
            false
        }
    }

    fun getFolderUri() = csvStorage.getFolderUri()

    fun setFolderUri(uri: android.net.Uri) {
        try {
            csvStorage.setFolderUri(uri)
            Log.d(TAG, "URI da pasta configurada: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao configurar URI da pasta: ${e.message}", e)
            throw e
        }
    }

    private fun saveAll(): Boolean {
        return try {
            Log.d(TAG, "Salvando ${items.size} itens no CSV")
            val success = csvStorage.saveItemsToCsv(items)
            if (!success) {
                Log.e(TAG, "Falha ao salvar todos os itens no CSV")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar todos os itens: ${e.message}", e)
            false
        }
    }
}

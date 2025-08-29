package com.mobitech.inventarioabc.data.csv

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mobitech.inventarioabc.domain.model.InventoryItem
import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CsvStorage(private val context: Context) {
    companion object {
        private const val TAG = "CsvStorage"
        private const val INVENTARIO_FOLDER = "INVENTARIO"
        private const val CSV_FILENAME = "inventario.csv"
        private const val CSV_TMP_FILENAME = "inventario.tmp"
        private const val BACKUP_PREFIX = "inventario_bkp_"
        private const val PREFS_NAME = "inventory_prefs"
        private const val PREF_FOLDER_URI = "folder_uri"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val backupDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun getFolderUri(): Uri? {
        val uriString = prefs.getString(PREF_FOLDER_URI, null)
        return uriString?.let { Uri.parse(it) }
    }

    fun setFolderUri(uri: Uri) {
        try {
            prefs.edit()
                .putString(PREF_FOLDER_URI, uri.toString())
                .apply()

            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            Log.d(TAG, "Pasta configurada com sucesso: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao configurar pasta: ${e.message}", e)
            throw e
        }
    }

    fun loadItemsFromCsv(): Map<String, InventoryItem> {
        return try {
            val folderUri = getFolderUri() ?: run {
                Log.w(TAG, "URI da pasta não encontrada")
                return emptyMap()
            }

            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: run {
                Log.w(TAG, "Não foi possível acessar a pasta")
                return emptyMap()
            }

            if (!folder.exists() || !folder.canRead()) {
                Log.w(TAG, "Pasta não existe ou sem permissão de leitura")
                return emptyMap()
            }

            val csvFile = folder.findFile(CSV_FILENAME)
            if (csvFile?.exists() == true && csvFile.canRead()) {
                val content = context.contentResolver.openInputStream(csvFile.uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                } ?: ""

                val items = CsvSerializer.csvToItems(content)
                Log.d(TAG, "Carregados ${items.size} itens do CSV")
                items
            } else {
                Log.d(TAG, "Arquivo CSV não encontrado, retornando lista vazia")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar CSV: ${e.message}", e)
            emptyMap()
        }
    }

    fun saveItemsToCsv(items: Map<String, InventoryItem>): Boolean {
        return try {
            val folderUri = getFolderUri() ?: run {
                Log.e(TAG, "URI da pasta não encontrada para salvar")
                return false
            }

            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: run {
                Log.e(TAG, "Não foi possível acessar a pasta para salvar")
                return false
            }

            if (!folder.exists() || !folder.canWrite()) {
                Log.e(TAG, "Pasta não existe ou sem permissão de escrita")
                return false
            }

            val csvContent = CsvSerializer.itemsToCsv(items.values)
            Log.d(TAG, "Salvando ${items.size} itens no CSV")

            // Escrita atômica: escrever em .tmp e depois renomear
            val tmpFile = getOrCreateFile(folder, CSV_TMP_FILENAME) ?: run {
                Log.e(TAG, "Não foi possível criar arquivo temporário")
                return false
            }

            // Escrever no arquivo temporário
            context.contentResolver.openOutputStream(tmpFile.uri)?.use { output ->
                output.write(csvContent.toByteArray(Charsets.UTF_8))
                output.flush()
            } ?: run {
                Log.e(TAG, "Não foi possível abrir stream de escrita para arquivo temporário")
                return false
            }

            // Remover arquivo existente se houver
            folder.findFile(CSV_FILENAME)?.delete()

            // Renomear tmp para final
            val renamed = tmpFile.renameTo(CSV_FILENAME)
            if (!renamed) {
                Log.e(TAG, "Falha ao renomear arquivo temporário")
                return false
            }

            Log.d(TAG, "CSV salvo com sucesso")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar CSV: ${e.message}", e)
            false
        }
    }

    fun createBackup(): Boolean {
        return try {
            val folderUri = getFolderUri() ?: run {
                Log.e(TAG, "URI da pasta não encontrada para backup")
                return false
            }

            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: run {
                Log.e(TAG, "Não foi possível acessar a pasta para backup")
                return false
            }

            val csvFile = folder.findFile(CSV_FILENAME)
            if (csvFile?.exists() != true) {
                Log.w(TAG, "Arquivo CSV principal não existe para fazer backup")
                return false
            }

            val timestamp = backupDateFormat.format(Date())
            val backupFileName = "$BACKUP_PREFIX$timestamp.csv"

            val backupFile = getOrCreateFile(folder, backupFileName) ?: run {
                Log.e(TAG, "Não foi possível criar arquivo de backup")
                return false
            }

            // Copiar conteúdo do arquivo principal para o backup
            context.contentResolver.openInputStream(csvFile.uri)?.use { input ->
                context.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            } ?: run {
                Log.e(TAG, "Erro ao copiar conteúdo para backup")
                return false
            }

            Log.d(TAG, "Backup criado: $backupFileName")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar backup: ${e.message}", e)
            false
        }
    }

    fun hasFolderPermission(): Boolean {
        val folderUri = getFolderUri() ?: return false
        return try {
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            val hasPermission = folder?.exists() == true && folder.canWrite()
            Log.d(TAG, "Verificação de permissão: $hasPermission")
            hasPermission
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar permissão: ${e.message}", e)
            false
        }
    }

    private fun getOrCreateFile(folder: DocumentFile, fileName: String): DocumentFile? {
        return try {
            folder.findFile(fileName) ?: folder.createFile("text/csv", fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar/encontrar arquivo $fileName: ${e.message}", e)
            null
        }
    }
}

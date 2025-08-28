package com.mobitech.inventarioabc.data.csv

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mobitech.inventarioabc.domain.model.InventoryItem
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CsvStorage(private val context: Context) {
    companion object {
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
        prefs.edit()
            .putString(PREF_FOLDER_URI, uri.toString())
            .apply()

        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    fun loadItemsFromCsv(): Map<String, InventoryItem> {
        return try {
            val folderUri = getFolderUri() ?: return emptyMap()
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyMap()

            val csvFile = folder.findFile(CSV_FILENAME)
            if (csvFile?.exists() == true) {
                val content = context.contentResolver.openInputStream(csvFile.uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                } ?: ""

                CsvSerializer.csvToItems(content)
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveItemsToCsv(items: Map<String, InventoryItem>): Boolean {
        return try {
            val folderUri = getFolderUri() ?: return false
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return false

            val csvContent = CsvSerializer.itemsToCsv(items.values)

            // Escrita atômica: escrever em .tmp e depois renomear
            val tmpFile = getOrCreateFile(folder, CSV_TMP_FILENAME)
            if (tmpFile != null) {
                context.contentResolver.openOutputStream(tmpFile.uri)?.use { output ->
                    output.write(csvContent.toByteArray(Charsets.UTF_8))
                }

                // Remover arquivo existente se houver
                folder.findFile(CSV_FILENAME)?.delete()

                // Renomear tmp para final
                tmpFile.renameTo(CSV_FILENAME)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun createBackup(): Boolean {
        return try {
            val folderUri = getFolderUri() ?: return false
            val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return false

            val csvFile = folder.findFile(CSV_FILENAME)
            if (csvFile?.exists() != true) return false

            val timestamp = backupDateFormat.format(Date())
            val backupFileName = "$BACKUP_PREFIX$timestamp.csv"

            val backupFile = getOrCreateFile(folder, backupFileName)
            if (backupFile != null) {
                // Copiar conteúdo do arquivo principal para o backup
                context.contentResolver.openInputStream(csvFile.uri)?.use { input ->
                    context.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun hasFolderPermission(): Boolean {
        val folderUri = getFolderUri() ?: return false
        return try {
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            folder?.exists() == true && folder.canWrite()
        } catch (e: Exception) {
            false
        }
    }

    private fun getOrCreateFile(folder: DocumentFile, fileName: String): DocumentFile? {
        return folder.findFile(fileName) ?: folder.createFile("text/csv", fileName)
    }
}

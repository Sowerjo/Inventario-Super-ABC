package com.mobitech.inventarioabc.data.csv

import com.mobitech.inventarioabc.domain.model.InventoryItem
import com.mobitech.inventarioabc.util.DateTimeFmt

object CsvSerializer {
    private const val HEADER = "codigo,quantidade,data_hora_iso"
    private const val SEPARATOR = ","

    fun itemsToCsv(items: Collection<InventoryItem>): String {
        val csvBuilder = StringBuilder()
        csvBuilder.appendLine(HEADER)

        items.forEach { item ->
            val linha = "${escapeField(item.codigo)}$SEPARATOR${item.quantidade}$SEPARATOR${DateTimeFmt.formatToIso(item.dataHoraEpochMillis)}"
            csvBuilder.appendLine(linha)
        }

        return csvBuilder.toString()
    }

    fun csvToItems(csvContent: String): Map<String, InventoryItem> {
        val items = mutableMapOf<String, InventoryItem>()
        val lines = csvContent.lines()

        if (lines.isEmpty() || lines[0] != HEADER) {
            return items
        }

        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            try {
                val parts = parseCsvLine(line)
                if (parts.size >= 3) {
                    val codigo = parts[0]
                    val quantidade = parts[1].toInt()
                    val dataHora = DateTimeFmt.parseFromIso(parts[2])

                    val item = InventoryItem(codigo, quantidade, dataHora)
                    items[codigo] = item
                }
            } catch (e: Exception) {
                // Ignora linhas com erro de parsing
            }
        }

        return items
    }

    private fun escapeField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' && !inQuotes -> {
                    inQuotes = true
                }
                char == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        currentField.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    result.add(currentField.toString())
                    currentField.clear()
                }
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }

        result.add(currentField.toString())
        return result
    }
}

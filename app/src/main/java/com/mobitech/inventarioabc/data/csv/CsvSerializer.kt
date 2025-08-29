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

        if (lines.isEmpty()) {
            return items
        }

        // Detecta o separador usado no arquivo (vírgula ou ponto e vírgula)
        val separator = detectSeparator(lines)

        // Verifica se a primeira linha é um cabeçalho válido (ignora caso não seja)
        val startIndex = if (isValidHeader(lines[0], separator)) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            try {
                val parts = parseCsvLine(line, separator)
                if (parts.size >= 3) {
                    val codigo = cleanField(parts[0])
                    val quantidade = cleanField(parts[1]).toInt()
                    val dataHora = DateTimeFmt.parseFromIso(cleanField(parts[2]))

                    val item = InventoryItem(codigo, quantidade, dataHora)
                    items[codigo] = item
                }
            } catch (e: Exception) {
                // Ignora linhas com erro de parsing
                android.util.Log.d("CsvSerializer", "Erro ao processar linha: $line - ${e.message}")
            }
        }

        return items
    }

    private fun detectSeparator(lines: List<String>): String {
        if (lines.isEmpty()) return ","

        val firstLine = lines[0]
        val commaCount = firstLine.count { it == ',' }
        val semicolonCount = firstLine.count { it == ';' }

        return if (semicolonCount > commaCount) ";" else ","
    }

    private fun isValidHeader(line: String, separator: String): Boolean {
        val expectedHeaders = listOf("codigo", "quantidade", "data_hora_iso")
        val parts = parseCsvLine(line, separator).map { cleanField(it).lowercase() }

        return parts.size >= 3 &&
               parts[0] == expectedHeaders[0] &&
               parts[1] == expectedHeaders[1] &&
               parts[2] == expectedHeaders[2]
    }

    private fun cleanField(field: String): String {
        return field.trim().removeSurrounding("\"")
    }

    private fun escapeField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun parseCsvLine(line: String, separator: String = ","): List<String> {
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
                char.toString() == separator && !inQuotes -> {
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

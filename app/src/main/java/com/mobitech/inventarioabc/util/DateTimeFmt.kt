package com.mobitech.inventarioabc.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeFmt {
    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

    // Formatos alternativos que o Excel pode gerar
    private val alternativeFormatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        DateTimeFormatter.ISO_ZONED_DATE_TIME
    )

    fun formatToIso(epochMillis: Long): String {
        val instant = Instant.ofEpochMilli(epochMillis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDateTime.atZone(ZoneId.systemDefault()).format(isoFormatter)
    }

    fun formatToDisplay(epochMillis: Long): String {
        val instant = Instant.ofEpochMilli(epochMillis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDateTime.format(displayFormatter)
    }

    fun parseFromIso(isoString: String): Long {
        val cleanString = isoString.trim().removeSurrounding("\"")

        // Tenta primeiro o formato padrão
        try {
            val zonedDateTime = java.time.ZonedDateTime.parse(cleanString, isoFormatter)
            return zonedDateTime.toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) {
            // Ignora e tenta os formatos alternativos
        }

        // Tenta formatos alternativos
        for (formatter in alternativeFormatters) {
            try {
                return when {
                    cleanString.contains('T') && (cleanString.contains('+') || cleanString.contains('-') || cleanString.contains('Z')) -> {
                        // Formato com timezone
                        val zonedDateTime = java.time.ZonedDateTime.parse(cleanString, formatter)
                        zonedDateTime.toInstant().toEpochMilli()
                    }
                    cleanString.contains('T') -> {
                        // Formato ISO local sem timezone
                        val localDateTime = LocalDateTime.parse(cleanString, formatter)
                        localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }
                    else -> {
                        // Formato de data/hora simples
                        val localDateTime = LocalDateTime.parse(cleanString, formatter)
                        localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }
                }
            } catch (_: DateTimeParseException) {
                // Continua tentando outros formatos
                continue
            }
        }

        // Se nenhum formato funcionou, retorna timestamp atual
        android.util.Log.w("DateTimeFmt", "Não foi possível parsear a data: $cleanString")
        return System.currentTimeMillis()
    }
}

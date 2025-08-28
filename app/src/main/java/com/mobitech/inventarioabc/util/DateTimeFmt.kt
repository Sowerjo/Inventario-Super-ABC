package com.mobitech.inventarioabc.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeFmt {
    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
    private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

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
        return try {
            val zonedDateTime = java.time.ZonedDateTime.parse(isoString, isoFormatter)
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

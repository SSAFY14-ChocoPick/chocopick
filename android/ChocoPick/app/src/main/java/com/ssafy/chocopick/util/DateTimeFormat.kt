package com.ssafy.chocopick.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormat {
    private val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)

    fun format(ms: Long): String {
        if (ms <= 0L) return ""
        return sdf.format(Date(ms))
    }
}
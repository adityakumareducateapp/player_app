package com.example.playerapp.extensions

import com.example.playerapp.FastSeek
import com.example.playerapp.PlayerPreferences

fun PlayerPreferences.shouldFastSeek(duration: Long): Boolean {
    return when (fastSeek) {
        FastSeek.ENABLE -> true
        FastSeek.DISABLE -> false
        FastSeek.AUTO -> duration >= minDurationForFastSeek
    }
}

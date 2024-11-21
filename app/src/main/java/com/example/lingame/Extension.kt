package com.example.lingame

import java.security.MessageDigest

fun String.toSHA256(): String {
    val HEX_CHARS = "0123456789ABCDEF"
    val digest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return digest.joinToString (
        separator = "",
        transform = { byte ->
            String(
                charArrayOf(
                    HEX_CHARS[byte.toInt() shr 4 and 0x0F],
                    HEX_CHARS[byte.toInt() and 0x0F]
                )
            )
        })
}
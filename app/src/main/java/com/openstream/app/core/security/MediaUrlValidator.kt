package com.openstream.app.core.security

object MediaUrlValidator {
    /**
     * Validates that media URLs belong to supported protocols (https, http, file)
     * and do not contain path traversal or injection characters.
     */
    fun isValidMediaUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()

        if (trimmed.startsWith("file://") || trimmed.startsWith("/")) {
            return !trimmed.contains("..")
        }

        if (trimmed.startsWith("https://", ignoreCase = true) || trimmed.startsWith("http://", ignoreCase = true)) {
            val invalidChars = listOf("<", ">", "\"", "'", "\\", "{", "}")
            if (invalidChars.any { trimmed.contains(it) }) return false
            return true
        }

        return false
    }
}

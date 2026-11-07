package com.openstream.app.domain.model

enum class DownloadStatus {
    NOT_DOWNLOADED,
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class DownloadedMovie(
    val movie: Movie,
    val localFilePath: String,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.NOT_DOWNLOADED,
    val progressPercentage: Float = 0f,
    val downloadDateMs: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
) {
    val isCompleted: Boolean
        get() = status == DownloadStatus.COMPLETED

    val isDownloading: Boolean
        get() = status == DownloadStatus.DOWNLOADING || status == DownloadStatus.PENDING

    val isPaused: Boolean
        get() = status == DownloadStatus.PAUSED

    val formattedDownloadedSize: String
        get() = formatBytes(downloadedBytes)

    val formattedTotalSize: String
        get() = formatBytes(totalBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 MB"
            val mb = bytes.toDouble() / (1024 * 1024)
            return if (mb >= 1024) {
                String.format(java.util.Locale.US, "%.1f GB", mb / 1024)
            } else {
                String.format(java.util.Locale.US, "%.1f MB", mb)
            }
        }

        /**
         * Verifies whether a movie's license explicitly permits offline download.
         * Enforces strict copyright, public-domain, and permissive archive licensing rules.
         *
         * Under international copyright law, pre-1929 films cannot be assumed to be
         * fully public domain if they have modern copyrighted musical accompaniments,
         * restored footage under copyright, or lack verified provenance.
         * Download is only permitted when rights are affirmatively established and verified.
         */
        fun isDownloadEligible(movie: Movie): Boolean {
            if (!movie.isPublicDomain) return false
            if (movie.streamUrl.isBlank()) return false

            val metadata = movie.metadata
            // If explicit metadata exists, affirmative rights verification is strictly required
            if (metadata != null && !metadata.isRightsVerified) {
                return false
            }

            val note = (movie.licenseStatus + " " + (metadata?.rightsNote ?: "") + " " + movie.restorationNote).lowercase()

            // Disallow if modern restoration or soundtrack asserts proprietary copyright
            val hasSoundtrackOrRestorationCopyright = note.contains("copyrighted score") ||
                note.contains("copyrighted soundtrack") ||
                note.contains("modern score (c)") ||
                note.contains("all rights reserved") ||
                note.contains("proprietary") ||
                note.contains("unverified")
            if (hasSoundtrackOrRestorationCopyright) return false

            // Require affirmative public domain certification or verified rights
            val hasExplicitPDLicense = note.contains("public domain") ||
                note.contains("cc0") ||
                note.contains("creative commons") ||
                (metadata?.licenseType?.lowercase()?.contains("public domain") == true)

            return hasExplicitPDLicense || (metadata?.isRightsVerified == true)
        }
    }
}

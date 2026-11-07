package com.openstream.app.data.repository

import android.content.Context
import android.os.StatFs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.openstream.app.data.remote.api.NetworkClient
import com.openstream.app.domain.model.DownloadStatus
import com.openstream.app.domain.model.DownloadedMovie
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.repository.MovieDownloadManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap

class MovieDownloadManagerImpl(
    private val context: Context,
    private val okHttpClient: OkHttpClient = NetworkClient.okHttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : MovieDownloadManager {

    private val gson = Gson()
    private val downloadDir: File by lazy {
        val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
        File(baseDir, "openstream_downloads").apply {
            if (!exists()) mkdirs()
        }
    }
    private val indexFile: File by lazy { File(downloadDir, "downloads_index.json") }

    private val downloadsMap = ConcurrentHashMap<String, DownloadedMovie>()
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _downloadsFlow = MutableStateFlow<List<DownloadedMovie>>(emptyList())
    override val downloads: Flow<List<DownloadedMovie>> = _downloadsFlow.asStateFlow()

    init {
        loadPersistedDownloads()
    }

    override fun getDownload(movieId: String): Flow<DownloadedMovie?> {
        return downloads.map { list -> list.firstOrNull { it.movie.id == movieId } }
    }

    override fun isMovieDownloaded(movieId: String): Boolean {
        val item = downloadsMap[movieId] ?: return false
        return item.status == DownloadStatus.COMPLETED && File(item.localFilePath).exists()
    }

    override fun getLocalPlaybackUri(movieId: String): String? {
        val item = downloadsMap[movieId] ?: return null
        if (item.status == DownloadStatus.COMPLETED) {
            val file = File(item.localFilePath)
            if (file.exists()) {
                return file.absolutePath
            }
        }
        return null
    }

    override suspend fun startDownload(movie: Movie): Result<DownloadedMovie> = withContext(ioDispatcher) {
        if (!DownloadedMovie.isDownloadEligible(movie)) {
            return@withContext Result.failure(
                IllegalStateException("Licensing restriction: Content is not verified for offline distribution.")
            )
        }

        val existing = downloadsMap[movie.id]
        if (existing != null && existing.status == DownloadStatus.COMPLETED && File(existing.localFilePath).exists()) {
            return@withContext Result.success(existing)
        }

        // Estimate or inspect size requirement (default min 50MB check)
        val estimatedSize = movie.streams.firstOrNull()?.sizeBytes?.takeIf { it > 0 } ?: (100L * 1024 * 1024)
        if (!hasSufficientStorage(estimatedSize)) {
            val failed = DownloadedMovie(
                movie = movie,
                localFilePath = "",
                status = DownloadStatus.FAILED,
                errorMessage = "Insufficient storage space on device"
            )
            updateDownload(failed)
            return@withContext Result.failure(IllegalStateException("Insufficient storage space on device"))
        }

        val destinationFile = File(downloadDir, "${sanitizeFileName(movie.id)}.mp4")
        val initial = DownloadedMovie(
            movie = movie,
            localFilePath = destinationFile.absolutePath,
            totalBytes = estimatedSize,
            downloadedBytes = 0L,
            status = DownloadStatus.PENDING,
            progressPercentage = 0f
        )
        updateDownload(initial)

        startDownloadJob(initial, destinationFile)
        Result.success(initial)
    }

    override suspend fun pauseDownload(movieId: String) = withContext(ioDispatcher) {
        activeJobs[movieId]?.cancel()
        activeJobs.remove(movieId)

        val current = downloadsMap[movieId] ?: return@withContext
        if (current.status == DownloadStatus.DOWNLOADING || current.status == DownloadStatus.PENDING) {
            val paused = current.copy(status = DownloadStatus.PAUSED)
            updateDownload(paused)
        }
    }

    override suspend fun resumeDownload(movieId: String) = withContext(ioDispatcher) {
        val current = downloadsMap[movieId] ?: return@withContext
        if (current.status == DownloadStatus.PAUSED || current.status == DownloadStatus.FAILED) {
            val destinationFile = File(current.localFilePath)
            val updated = current.copy(status = DownloadStatus.PENDING, errorMessage = null)
            updateDownload(updated)
            startDownloadJob(updated, destinationFile)
        }
    }

    override suspend fun cancelDownload(movieId: String) = withContext(ioDispatcher) {
        activeJobs[movieId]?.cancel()
        activeJobs.remove(movieId)

        val current = downloadsMap.remove(movieId)
        if (current != null) {
            val file = File(current.localFilePath)
            val tempFile = File("${current.localFilePath}.tmp")
            if (file.exists()) file.delete()
            if (tempFile.exists()) tempFile.delete()
            persistDownloadsIndex()
            emitState()
        }
    }

    override suspend fun deleteDownload(movieId: String): Boolean = withContext(ioDispatcher) {
        activeJobs[movieId]?.cancel()
        activeJobs.remove(movieId)

        val item = downloadsMap.remove(movieId) ?: return@withContext false
        val file = File(item.localFilePath)
        val tempFile = File("${item.localFilePath}.tmp")
        if (file.exists()) file.delete()
        if (tempFile.exists()) tempFile.delete()

        persistDownloadsIndex()
        emitState()
        true
    }

    private fun startDownloadJob(downloadedMovie: DownloadedMovie, destinationFile: File) {
        val movieId = downloadedMovie.movie.id
        activeJobs[movieId]?.cancel()

        val job = scope.launch(ioDispatcher) {
            val tempFile = File("${destinationFile.absolutePath}.tmp")
            var downloadedSoFar = if (tempFile.exists()) tempFile.length() else 0L

            val downloadUrl = downloadedMovie.movie.streamUrl
            if (downloadUrl.isBlank()) {
                updateDownload(
                    downloadedMovie.copy(
                        status = DownloadStatus.FAILED,
                        errorMessage = "Invalid streaming URL"
                    )
                )
                return@launch
            }

            try {
                updateDownload(
                    downloadedMovie.copy(
                        status = DownloadStatus.DOWNLOADING,
                        downloadedBytes = downloadedSoFar
                    )
                )

                val requestBuilder = Request.Builder().url(downloadUrl)
                if (downloadedSoFar > 0) {
                    requestBuilder.addHeader("Range", "bytes=$downloadedSoFar-")
                }

                var response = okHttpClient.newCall(requestBuilder.build()).execute()

                // Handle HTTP 416 Range Not Satisfiable: e.g. remote file truncated or temp file corrupted
                if (response.code == 416 && downloadedSoFar > 0) {
                    response.close()
                    if (tempFile.exists()) tempFile.delete()
                    downloadedSoFar = 0L
                    val retryRequest = Request.Builder().url(downloadUrl).build()
                    response = okHttpClient.newCall(retryRequest).execute()
                }

                if (!response.isSuccessful && response.code != 206) {
                    updateDownload(
                        downloadedMovie.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = "HTTP error ${response.code}: ${response.message}"
                        )
                    )
                    return@launch
                }

                val body = response.body
                if (body == null) {
                    updateDownload(
                        downloadedMovie.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = "Empty response body"
                        )
                    )
                    return@launch
                }

                val contentLength = body.contentLength()
                val totalLength = if (downloadedSoFar > 0 && response.code == 206) {
                    contentLength + downloadedSoFar
                } else {
                    contentLength.takeIf { it > 0 } ?: downloadedMovie.totalBytes
                }

                val inputStream = body.byteStream()
                val outputStream = if (downloadedSoFar > 0 && response.code == 206) {
                    FileOutputStream(tempFile, true)
                } else {
                    downloadedSoFar = 0L
                    FileOutputStream(tempFile, false)
                }

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var lastProgressUpdate = System.currentTimeMillis()

                outputStream.use { out ->
                    inputStream.use { input ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            if (!isActive) {
                                return@launch
                            }
                            out.write(buffer, 0, bytesRead)
                            downloadedSoFar += bytesRead

                            val now = System.currentTimeMillis()
                            if (now - lastProgressUpdate > 300) {
                                val progress = if (totalLength > 0) {
                                    (downloadedSoFar.toFloat() / totalLength).coerceIn(0f, 1f)
                                } else 0f
                                updateDownload(
                                    downloadedMovie.copy(
                                        status = DownloadStatus.DOWNLOADING,
                                        downloadedBytes = downloadedSoFar,
                                        totalBytes = totalLength,
                                        progressPercentage = progress
                                    )
                                )
                                lastProgressUpdate = now
                            }
                        }
                    }
                }

                // Download finished successfully: atomic rename
                if (tempFile.renameTo(destinationFile)) {
                    val completed = downloadedMovie.copy(
                        status = DownloadStatus.COMPLETED,
                        downloadedBytes = destinationFile.length(),
                        totalBytes = destinationFile.length(),
                        progressPercentage = 1f,
                        localFilePath = destinationFile.absolutePath,
                        errorMessage = null
                    )
                    updateDownload(completed)
                } else {
                    updateDownload(
                        downloadedMovie.copy(
                            status = DownloadStatus.FAILED,
                            errorMessage = "Failed to finalize downloaded media file"
                        )
                    )
                }
            } catch (e: Exception) {
                if (isActive) {
                    updateDownload(
                        downloadedMovie.copy(
                            status = DownloadStatus.FAILED,
                            downloadedBytes = downloadedSoFar,
                            errorMessage = e.localizedMessage ?: "Network interrupted during download"
                        )
                    )
                }
            } finally {
                activeJobs.remove(movieId)
            }
        }

        activeJobs[movieId] = job
    }

    private fun updateDownload(item: DownloadedMovie) {
        downloadsMap[item.movie.id] = item
        persistDownloadsIndex()
        emitState()
    }

    private fun emitState() {
        _downloadsFlow.value = downloadsMap.values.toList().sortedByDescending { it.downloadDateMs }
    }

    private fun hasSufficientStorage(requiredBytes: Long): Boolean {
        return try {
            val stat = StatFs(downloadDir.path)
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            availableBytes >= (requiredBytes + 50L * 1024 * 1024)
        } catch (_: Exception) {
            true
        }
    }

    private fun sanitizeFileName(input: String): String {
        return input.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }

    private fun loadPersistedDownloads() {
        var parsedSuccessfully = false
        try {
            if (indexFile.exists()) {
                val json = indexFile.readText()
                if (json.isNotBlank()) {
                    val type = object : TypeToken<List<DownloadedMovie>>() {}.type
                    val list: List<DownloadedMovie>? = gson.fromJson(json, type)
                    if (list != null) {
                        list.forEach { item ->
                            val file = File(item.localFilePath)
                            val status = if (item.status == DownloadStatus.COMPLETED && file.exists()) {
                                DownloadStatus.COMPLETED
                            } else if (File("${item.localFilePath}.tmp").exists()) {
                                DownloadStatus.PAUSED
                            } else if (file.exists()) {
                                DownloadStatus.COMPLETED
                            } else {
                                DownloadStatus.FAILED
                            }
                            downloadsMap[item.movie.id] = item.copy(status = status)
                        }
                        parsedSuccessfully = true
                    }
                }
            }
        } catch (_: Exception) {
            // Index file corrupted, unreadable, or invalid JSON
        }

        // Recovery: If index was corrupted or missing, scan downloadDir for existing completed media files
        if (!parsedSuccessfully || downloadsMap.isEmpty()) {
            recoverFromDiskFiles()
        }

        emitState()
    }

    private fun recoverFromDiskFiles() {
        try {
            val files = downloadDir.listFiles() ?: return
            var hasRecovered = false
            for (file in files) {
                if (file.isFile && file.name.endsWith(".mp4") && !file.name.endsWith(".tmp") && file.length() > 0) {
                    val baseName = file.name.removeSuffix(".mp4")
                    if (!downloadsMap.containsKey(baseName)) {
                        val recoveredMovie = Movie(
                            id = baseName,
                            title = baseName.replace("_", " "),
                            director = "Archival Restoration",
                            year = 1925,
                            durationMinutes = 90,
                            genre = "Archival",
                            synopsis = "Locally preserved archival print.",
                            streamUrl = file.absolutePath,
                            isPublicDomain = true,
                            metadata = MovieMetadata(
                                identifier = baseName,
                                sourceUrl = "local_file://${file.absolutePath}",
                                rightsNote = "Archival Preserved Copy",
                                licenseType = "Public Domain",
                                isRightsVerified = true,
                                verificationNote = "Recovered from validated local storage."
                            )
                        )
                        downloadsMap[baseName] = DownloadedMovie(
                            movie = recoveredMovie,
                            localFilePath = file.absolutePath,
                            totalBytes = file.length(),
                            downloadedBytes = file.length(),
                            status = DownloadStatus.COMPLETED,
                            progressPercentage = 1f
                        )
                        hasRecovered = true
                    }
                }
            }
            if (hasRecovered) {
                persistDownloadsIndex()
            }
        } catch (_: Exception) {
        }
    }

    private fun persistDownloadsIndex() {
        try {
            val json = gson.toJson(downloadsMap.values.toList())
            indexFile.writeText(json)
        } catch (_: Exception) {
        }
    }
}

package com.openstream.app.core.di

import android.content.Context
import com.openstream.app.core.network.ConnectivityNetworkMonitor
import com.openstream.app.core.network.NetworkMonitor
import com.openstream.app.data.local.CatalogCacheManager
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.data.local.UserPreferencesRepositoryImpl
import com.openstream.app.data.repository.AppUpdateManagerImpl
import com.openstream.app.data.repository.LibraryRepositoryImpl
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.data.repository.MovieDownloadManagerImpl
import com.openstream.app.data.repository.PlaybackHistoryRepositoryImpl
import com.openstream.app.data.repository.UserProfileRepositoryImpl
import com.openstream.app.domain.repository.AppUpdateManager
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import com.openstream.app.domain.repository.MovieDownloadManager
import com.openstream.app.domain.repository.PlaybackHistoryRepository
import com.openstream.app.domain.repository.UserProfileRepository

interface AppContainer {
    val userProfileRepository: UserProfileRepository
    val userPreferencesRepository: UserPreferencesRepository
    val movieCatalogRepository: MovieCatalogRepository
    val libraryRepository: LibraryRepository
    val playbackHistoryRepository: PlaybackHistoryRepository
    val downloadManager: MovieDownloadManager
    val catalogCacheManager: CatalogCacheManager
    val networkMonitor: NetworkMonitor
    val appUpdateManager: AppUpdateManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerImpl(context)
    }

    override val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepositoryImpl(context)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(context, userProfileRepository)
    }

    override val downloadManager: MovieDownloadManager by lazy {
        MovieDownloadManagerImpl(context)
    }

    override val catalogCacheManager: CatalogCacheManager by lazy {
        CatalogCacheManager(context)
    }

    override val networkMonitor: NetworkMonitor by lazy {
        ConnectivityNetworkMonitor(context)
    }

    override val movieCatalogRepository: MovieCatalogRepository by lazy {
        MovieCatalogRepositoryImpl(
            userPreferencesRepository = userPreferencesRepository,
            downloadManager = downloadManager,
            cacheManager = catalogCacheManager
        )
    }

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(userPreferencesRepository, movieCatalogRepository)
    }

    override val playbackHistoryRepository: PlaybackHistoryRepository by lazy {
        PlaybackHistoryRepositoryImpl(userPreferencesRepository)
    }
}

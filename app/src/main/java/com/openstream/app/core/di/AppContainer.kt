package com.openstream.app.core.di

import android.content.Context
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.data.local.UserPreferencesRepositoryImpl
import com.openstream.app.data.repository.LibraryRepositoryImpl
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.data.repository.PlaybackHistoryRepositoryImpl
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import com.openstream.app.domain.repository.PlaybackHistoryRepository

interface AppContainer {
    val userPreferencesRepository: UserPreferencesRepository
    val movieCatalogRepository: MovieCatalogRepository
    val libraryRepository: LibraryRepository
    val playbackHistoryRepository: PlaybackHistoryRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(context)
    }

    override val movieCatalogRepository: MovieCatalogRepository by lazy {
        MovieCatalogRepositoryImpl(userPreferencesRepository)
    }

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(userPreferencesRepository, movieCatalogRepository)
    }

    override val playbackHistoryRepository: PlaybackHistoryRepository by lazy {
        PlaybackHistoryRepositoryImpl()
    }
}

package com.openstream.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.domain.model.DownloadedMovie
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    WATCHLIST,
    FAVORITES,
    DOWNLOADS
}

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val downloadManager: MovieDownloadManager? = null
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.WATCHLIST)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    val watchlist: StateFlow<List<Movie>> = libraryRepository.getWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Movie>> = libraryRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadedMovie>> = (downloadManager?.downloads ?: flowOf(emptyList()))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            libraryRepository.toggleWatchlist(movie)
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            libraryRepository.toggleFavorite(movie)
        }
    }

    fun pauseDownload(movieId: String) {
        viewModelScope.launch {
            downloadManager?.pauseDownload(movieId)
        }
    }

    fun resumeDownload(movieId: String) {
        viewModelScope.launch {
            downloadManager?.resumeDownload(movieId)
        }
    }

    fun cancelDownload(movieId: String) {
        viewModelScope.launch {
            downloadManager?.cancelDownload(movieId)
        }
    }

    fun deleteDownload(movieId: String) {
        viewModelScope.launch {
            downloadManager?.deleteDownload(movieId)
        }
    }

    companion object {
        fun provideFactory(
            libraryRepository: LibraryRepository,
            downloadManager: MovieDownloadManager? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(libraryRepository, downloadManager) as T
                }
            }
    }
}

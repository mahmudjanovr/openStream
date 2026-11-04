package com.openstream.app.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    WATCHLIST,
    FAVORITES
}

class LibraryViewModel(
    private val libraryRepository: LibraryRepository
) : ViewModel() {
    private val _selectedTab = MutableStateFlow(LibraryTab.WATCHLIST)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()
    val watchlist: StateFlow<List<Movie>> = libraryRepository.getWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites: StateFlow<List<Movie>> = libraryRepository.getFavorites()
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

    

    
    companion object {
        fun provideFactory(libraryRepository: LibraryRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(libraryRepository) as T
                }
            }
    }
}

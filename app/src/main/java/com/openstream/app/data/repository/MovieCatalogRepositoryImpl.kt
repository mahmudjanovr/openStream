package com.openstream.app.data.repository

import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class MovieCatalogRepositoryImpl : MovieCatalogRepository {

    // Curated historical public-domain cinema preview dataset for Phase 1
    private val previewCatalog: List<Movie> = listOf(
        Movie(
            id = "metropolis-1927",
            title = "Metropolis",
            director = "Fritz Lang",
            year = 1927,
            durationMinutes = 153,
            genre = "Sci-Fi & Horror",
            synopsis = "In a futuristic city sharply divided between the working class and the city planners, the son of the city's mastermind falls in love with a working-class prophet.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR"
        ),
        Movie(
            id = "nosferatu-1922",
            title = "Nosferatu",
            director = "F.W. Murnau",
            year = 1922,
            durationMinutes = 94,
            genre = "Sci-Fi & Horror",
            synopsis = "Vampire Count Orlok expresses interest in a new residence and real estate agent Hutter's wife.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR"
        ),
        Movie(
            id = "the-general-1926",
            title = "The General",
            director = "Buster Keaton, Clyde Bruckman",
            year = 1926,
            durationMinutes = 75,
            genre = "Comedy",
            synopsis = "When Union spies steal an engineer's beloved locomotive, he pursues it single-handedly through enemy lines.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G"
        ),
        Movie(
            id = "charade-1963",
            title = "Charade",
            director = "Stanley Donen",
            year = 1963,
            durationMinutes = 113,
            genre = "Film Noir",
            synopsis = "A woman is pursued by several men who want a fortune her murdered husband had stolen. Who can she trust in Paris?",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "PG"
        ),
        Movie(
            id = "his-girl-friday-1940",
            title = "His Girl Friday",
            director = "Howard Hawks",
            year = 1940,
            durationMinutes = 92,
            genre = "Comedy",
            synopsis = "A newspaper editor uses every trick in the book to keep his ace reporter ex-wife from remarrying.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR"
        ),
        Movie(
            id = "trip-to-the-moon-1902",
            title = "A Trip to the Moon",
            director = "Georges Méliès",
            year = 1902,
            durationMinutes = 14,
            genre = "Silent Era",
            synopsis = "A group of astronomers journey to the Moon in a cannon-propelled capsule and encounter underground subterranean creatures.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G"
        ),
        Movie(
            id = "caligari-1920",
            title = "The Cabinet of Dr. Caligari",
            director = "Robert Wiene",
            year = 1920,
            durationMinutes = 67,
            genre = "Silent Era",
            synopsis = "Francis recounts a terrifying experience he and his fiancée had at a local carnival with the mysterious Dr. Caligari.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR"
        ),
        Movie(
            id = "night-living-dead-1968",
            title = "Night of the Living Dead",
            director = "George A. Romero",
            year = 1968,
            durationMinutes = 96,
            genre = "Sci-Fi & Horror",
            synopsis = "A disparate group of individuals takes refuge in an abandoned farmhouse when corpses begin to leave the graveyard.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR"
        ),
        Movie(
            id = "gold-rush-1925",
            title = "The Gold Rush",
            director = "Charlie Chaplin",
            year = 1925,
            durationMinutes = 95,
            genre = "Comedy",
            synopsis = "A lone prospector ventures into Alaska looking for gold and finds both hardship and romance.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G"
        ),
        Movie(
            id = "sherlock-jr-1924",
            title = "Sherlock Jr.",
            director = "Buster Keaton",
            year = 1924,
            durationMinutes = 45,
            genre = "Comedy",
            synopsis = "A film projectionist and amateur detective falls asleep and dreams of entering the movie screen.",
            posterUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G"
        )
    )

    private val recentSearches = MutableStateFlow(
        listOf("Metropolis", "Buster Keaton", "Silent Era", "Film Noir")
    )

    override fun getHeroMovie(): Flow<Movie?> {
        return flowOf(previewCatalog.firstOrNull { it.id == "metropolis-1927" })
    }

    override fun getContinueWatching(): Flow<List<Movie>> {
        // Representative placeholder queue for Continue Watching
        return flowOf(previewCatalog.take(2))
    }

    override fun getFeaturedCollections(): Flow<List<MovieCategory>> {
        val collections = listOf(
            MovieCategory(
                id = "pioneers-silent-cinema",
                title = "Pioneers of Silent Cinema",
                movies = previewCatalog.filter { it.year <= 1927 }
            ),
            MovieCategory(
                id = "expressionist-masterpieces",
                title = "Expressionist Masterpieces",
                movies = previewCatalog.filter { it.genre.contains("Horror") || it.genre.contains("Noir") }
            ),
            MovieCategory(
                id = "golden-age-comedy",
                title = "Golden Age Comedy",
                movies = previewCatalog.filter { it.genre == "Comedy" }
            )
        )
        return flowOf(collections)
    }

    override fun getRecentlyAdded(): Flow<List<Movie>> {
        return flowOf(previewCatalog.take(5))
    }

    override fun getPopularClassics(): Flow<List<Movie>> {
        return flowOf(previewCatalog.reversed().take(5))
    }

    override fun getMoviesByGenre(genre: Genre): Flow<List<Movie>> {
        return flowOf(
            when (genre) {
                Genre.ALL -> previewCatalog
                Genre.SILENT -> previewCatalog.filter { it.genre == "Silent Era" || it.year <= 1927 }
                Genre.NOIR -> previewCatalog.filter { it.genre == "Film Noir" }
                Genre.SCIFI_HORROR -> previewCatalog.filter { it.genre == "Sci-Fi & Horror" }
                Genre.COMEDY -> previewCatalog.filter { it.genre == "Comedy" }
                Genre.DRAMA -> previewCatalog.filter { it.genre == "Drama" }
            }
        )
    }

    override fun searchMovies(query: String): Flow<List<Movie>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return flowOf(emptyList())

        return flowOf(
            previewCatalog.filter { movie ->
                movie.title.contains(trimmed, ignoreCase = true) ||
                    movie.director.contains(trimmed, ignoreCase = true) ||
                    movie.genre.contains(trimmed, ignoreCase = true) ||
                    movie.year.toString().contains(trimmed)
            }
        )
    }

    override fun getRecentSearches(): Flow<List<String>> = recentSearches.asStateFlow()

    override suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            val current = recentSearches.value.toMutableList()
            current.remove(trimmed)
            current.add(0, trimmed)
            recentSearches.value = current.take(10)
        }
    }

    override suspend fun clearRecentSearches() {
        recentSearches.value = emptyList()
    }
}

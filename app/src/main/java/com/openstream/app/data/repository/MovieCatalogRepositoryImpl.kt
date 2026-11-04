package com.openstream.app.data.repository

import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class MovieCatalogRepositoryImpl(
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : MovieCatalogRepository {

    // Curated historical public-domain cinema dataset with rich OTT metadata
    private val previewCatalog: List<Movie> = listOf(
        Movie(
            id = "metropolis-1927",
            title = "Metropolis",
            director = "Fritz Lang",
            year = 1927,
            durationMinutes = 153,
            genre = "Sci-Fi & Horror",
            synopsis = "In a futuristic city sharply divided between the working class and the city planners, the son of the city's mastermind falls in love with a working-class prophet who predicts the coming of a mediator.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Alfred Abel", "Brigitte Helm", "Gustav Fröhlich", "Rudolf Klein-Rogge"),
            restorationNote = "Restored from the 2008 Buenos Aires 16mm negative discovery",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = 0.65f
        ),
        Movie(
            id = "nosferatu-1922",
            title = "Nosferatu",
            director = "F.W. Murnau",
            year = 1922,
            durationMinutes = 94,
            genre = "Sci-Fi & Horror",
            synopsis = "Vampire Count Orlok expresses interest in a new residence and real estate agent Hutter's wife. A foundational pillar of German Expressionist horror and cinematic dread.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Max Schreck", "Gustav von Wangenheim", "Greta Schröder"),
            restorationNote = "Tinted according to original 1922 release notes",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = 0.35f
        ),
        Movie(
            id = "the-general-1926",
            title = "The General",
            director = "Buster Keaton, Clyde Bruckman",
            year = 1926,
            durationMinutes = 75,
            genre = "Comedy",
            synopsis = "When Union spies steal an engineer's beloved locomotive, he pursues it single-handedly through enemy lines in one of cinema's greatest masterclasses in visual comedy and physical rhythm.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G",
            country = "United States",
            leadCast = listOf("Buster Keaton", "Marion Mack", "Glen Cavender"),
            restorationNote = "Digitally scanned and stabilized from original 35mm nitrate fine grain",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = 0.85f
        ),
        Movie(
            id = "charade-1963",
            title = "Charade",
            director = "Stanley Donen",
            year = 1963,
            durationMinutes = 113,
            genre = "Film Noir",
            synopsis = "A woman is pursued by several men who want a fortune her murdered husband had stolen. Romance and suspense collide across Parisian avenues in 'the best Hitchcock film Hitchcock never made.'",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "PG",
            country = "United States",
            leadCast = listOf("Cary Grant", "Audrey Hepburn", "Walter Matthau", "James Coburn"),
            restorationNote = "Technicolor archival transfer in 1.85:1 widescreen aspect ratio",
            licenseStatus = "Public Domain (Entered PD due to defective notice upon release)",
            playbackProgressPercentage = 0.15f
        ),
        Movie(
            id = "his-girl-friday-1940",
            title = "His Girl Friday",
            director = "Howard Hawks",
            year = 1940,
            durationMinutes = 92,
            genre = "Comedy",
            synopsis = "A hard-charging newspaper editor uses every unscrupulous trick in the book to keep his ace reporter and former wife from remarrying and retiring into domestic life.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "United States",
            leadCast = listOf("Cary Grant", "Rosalind Russell", "Ralph Bellamy"),
            restorationNote = "Audio track remastered for crisp overlapping screwball dialogue",
            licenseStatus = "Public Domain (Copyright not renewed in 1968)",
            playbackProgressPercentage = null
        ),
        Movie(
            id = "trip-to-the-moon-1902",
            title = "A Trip to the Moon",
            director = "Georges Méliès",
            year = 1902,
            durationMinutes = 14,
            genre = "Silent Era",
            synopsis = "A visionary society of astronomers embark on a projectile journey to the Moon, discovering surreal landscapes and subterranean inhabitants. Cinema's earliest science fiction triumph.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G",
            country = "France",
            leadCast = listOf("Georges Méliès", "Bleuette Bernon", "François Lallement"),
            restorationNote = "Hand-painted color version digitally reassembled frame by frame",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null
        ),
        Movie(
            id = "caligari-1920",
            title = "The Cabinet of Dr. Caligari",
            director = "Robert Wiene",
            year = 1920,
            durationMinutes = 67,
            genre = "Silent Era",
            synopsis = "Francis recounts a terrifying experience at a carnival with the mysterious Dr. Caligari and his somnambulist Cesare. Characterized by angular, distorted expressionist set architecture.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Werner Krauss", "Conrad Veidt", "Friedrich Fehér"),
            restorationNote = "Friedrich-Wilhelm-Murnau-Stiftung 4K camera negative restoration",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null
        ),
        Movie(
            id = "night-living-dead-1968",
            title = "Night of the Living Dead",
            director = "George A. Romero",
            year = 1968,
            durationMinutes = 96,
            genre = "Sci-Fi & Horror",
            synopsis = "A disparate group of individuals seeks refuge inside a rural Pennsylvania farmhouse as reanimated ghouls surround the house. The revolutionary birth of modern horror.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "United States",
            leadCast = listOf("Duane Jones", "Judith O'Dea", "Karl Hardman"),
            restorationNote = "Museum of Modern Art / Film Foundation 4K restoration",
            licenseStatus = "Public Domain (Original distributor omitted copyright notice)",
            playbackProgressPercentage = null
        ),
        Movie(
            id = "gold-rush-1925",
            title = "The Gold Rush",
            director = "Charlie Chaplin",
            year = 1925,
            durationMinutes = 95,
            genre = "Comedy",
            synopsis = "The Little Tramp ventures into the frozen Alaskan Klondike during the Gold Rush, enduring blizzards, cabin fever, and the legendary dance of the bread rolls.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G",
            country = "United States",
            leadCast = listOf("Charlie Chaplin", "Mack Swain", "Georgia Hale"),
            restorationNote = "Original 1925 silent release restored at 24 frames per second",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null
        ),
        Movie(
            id = "sherlock-jr-1924",
            title = "Sherlock Jr.",
            director = "Buster Keaton",
            year = 1924,
            durationMinutes = 45,
            genre = "Comedy",
            synopsis = "A cinema projectionist and aspiring sleuth falls asleep in his booth and dreams of stepping through the silver screen into the movie itself. A meta-cinematic miracle.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "G",
            country = "United States",
            leadCast = listOf("Buster Keaton", "Kathryn McGuire", "Joe Keaton"),
            restorationNote = "Restored in 4K by Cineteca di Bologna at L'Immagine Ritrovata",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null
        )
    )

    // In-memory fallback if preferences repository is not provided (e.g. simple unit tests)
    private val fallbackSearches = MutableStateFlow(
        listOf("Metropolis", "Buster Keaton", "Silent Era", "Film Noir")
    )

    override fun getHeroMovie(): Flow<Movie?> {
        return flowOf(previewCatalog.firstOrNull { it.id == "metropolis-1927" })
    }

    override fun getContinueWatching(): Flow<List<Movie>> {
        return flowOf(previewCatalog.filter { it.playbackProgressPercentage != null })
    }

    override fun getFeaturedCollections(): Flow<List<MovieCategory>> {
        val collections = listOf(
            MovieCategory(
                id = "pioneers-silent-cinema",
                title = "Pioneers of Silent Cinema",
                subtitle = "The masterworks that defined the visual language of motion pictures",
                movies = previewCatalog.filter { it.year <= 1927 }
            ),
            MovieCategory(
                id = "expressionist-masterpieces",
                title = "German Expressionism & Shadows",
                subtitle = "Chiaroscuro, twisted geometry, and psychological tension",
                movies = previewCatalog.filter { it.genre.contains("Horror") || it.genre.contains("Noir") }
            ),
            MovieCategory(
                id = "golden-age-comedy",
                title = "Golden Age Comedy Masters",
                subtitle = "Impeccable physical timing from Keaton and Chaplin",
                movies = previewCatalog.filter { it.genre == "Comedy" }
            )
        )
        return flowOf(collections)
    }

    override fun getRecentlyAdded(): Flow<List<Movie>> {
        return flowOf(previewCatalog.take(5))
    }

    override fun getPopularClassics(): Flow<List<Movie>> {
        return flowOf(previewCatalog.reversed().take(6))
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
                    movie.year.toString().contains(trimmed) ||
                    movie.leadCast.any { it.contains(trimmed, ignoreCase = true) }
            }
        )
    }

    override fun getMovieById(id: String): Flow<Movie?> {
        return flowOf(previewCatalog.firstOrNull { it.id == id })
    }

    override fun getRelatedMovies(movieId: String): Flow<List<Movie>> {
        val current = previewCatalog.firstOrNull { it.id == movieId } ?: return flowOf(previewCatalog.take(4))
        val related = previewCatalog.filter {
            it.id != movieId && (it.genre == current.genre || it.director == current.director || Math.abs(it.year - current.year) <= 10)
        }
        return flowOf(if (related.isNotEmpty()) related else previewCatalog.filter { it.id != movieId }.take(4))
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return userPreferencesRepository?.recentSearches ?: fallbackSearches.asStateFlow()
    }

    override suspend fun addRecentSearch(query: String) {
        if (userPreferencesRepository != null) {
            userPreferencesRepository.addRecentSearch(query)
        } else {
            val trimmed = query.trim()
            if (trimmed.isNotBlank()) {
                val current = fallbackSearches.value.toMutableList()
                current.remove(trimmed)
                current.add(0, trimmed)
                fallbackSearches.value = current.take(10)
            }
        }
    }

    override suspend fun clearRecentSearches() {
        if (userPreferencesRepository != null) {
            userPreferencesRepository.clearRecentSearches()
        } else {
            fallbackSearches.value = emptyList()
        }
    }
}

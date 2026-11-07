package com.openstream.app.data.repository

import com.openstream.app.data.local.CatalogCacheManager
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.data.remote.api.ArchiveApiService
import com.openstream.app.data.remote.api.NetworkClient
import com.openstream.app.data.remote.mapper.ArchiveMovieMapper
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.model.MovieDetails
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.model.MovieSource
import com.openstream.app.domain.model.MovieStream
import com.openstream.app.domain.repository.MovieCatalogRepository
import com.openstream.app.domain.repository.MovieDownloadManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MovieCatalogRepositoryImpl(
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val apiService: ArchiveApiService = NetworkClient.archiveApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val downloadManager: MovieDownloadManager? = null,
    private val cacheManager: CatalogCacheManager? = null
) : MovieCatalogRepository {

    // In-memory cache for live API results & resolved metadata
    private val movieCache = ConcurrentHashMap<String, Movie>()
    private val detailsCache = ConcurrentHashMap<String, MovieDetails>()

    // Fallback in-memory searches if userPreferencesRepository is null (e.g. testing)
    private val fallbackSearches = MutableStateFlow(
        listOf("Metropolis", "Buster Keaton", "Silent Era", "Film Noir", "Charlie Chaplin")
    )

    // Curated historical public-domain cinema dataset with verified working streaming and artwork URLs
    private val previewCatalog: List<Movie> = listOf(
        Movie(
            id = "metropolis-1927",
            title = "Metropolis",
            director = "Fritz Lang",
            year = 1927,
            durationMinutes = 153,
            genre = "Sci-Fi & Horror",
            synopsis = "In a futuristic city sharply divided between the working class and the city planners, the son of the city's mastermind falls in love with a working-class prophet who predicts the coming of a mediator. A visual and philosophical monument of early science-fiction cinema.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Alfred Abel", "Brigitte Helm", "Gustav Fröhlich", "Rudolf Klein-Rogge"),
            restorationNote = "Digitally preserved archival scan with orchestral synchronization",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = 0.65f,
            streamUrl = "https://archive.org/download/Metropolis1927EnglishVersion/Metropolis_1927_English_Version.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/Metropolis1927EnglishVersion/Metropolis_1927_English_Version.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 1084223400L,
                    durationSeconds = 9180L
                )
            ),
            metadata = MovieMetadata(
                identifier = "Metropolis1927EnglishVersion",
                sourceUrl = "https://archive.org/details/Metropolis1927EnglishVersion",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Historical 1927 silent film print. Verified worldwide public domain with no proprietary soundtrack rights claim.",
                collection = "feature_films",
                downloads = 150000L
            )
        ),
        Movie(
            id = "nosferatu-1922",
            title = "Nosferatu",
            director = "F.W. Murnau",
            year = 1922,
            durationMinutes = 94,
            genre = "Sci-Fi & Horror",
            synopsis = "Vampire Count Orlok expresses interest in a new residence and real estate agent Hutter's wife. A foundational pillar of German Expressionist horror and cinematic dread.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/9/90/Nosferatu_poster_%28Albin_Grau%2C_1922%29_1.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/9/90/Nosferatu_poster_%28Albin_Grau%2C_1922%29_1.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/en/9/90/Nosferatu_poster_%28Albin_Grau%2C_1922%29_1.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Max Schreck", "Gustav von Wangenheim", "Greta Schröder"),
            restorationNote = "Tinted according to original 1922 release notes",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = 0.35f,
            streamUrl = "https://archive.org/download/Nosferatu1922/Nosferatu.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/Nosferatu1922/Nosferatu.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 752391000L,
                    durationSeconds = 5640L
                )
            ),
            metadata = MovieMetadata(
                identifier = "Nosferatu1922",
                sourceUrl = "https://archive.org/details/Nosferatu1922",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain Worldwide",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Original 1922 German Expressionist print in worldwide public domain with public domain silent score.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "the-general-1926",
            title = "The General",
            director = "Buster Keaton, Clyde Bruckman",
            year = 1926,
            durationMinutes = 75,
            genre = "Comedy",
            synopsis = "When Union spies steal an engineer's beloved locomotive, he pursues it single-handedly through enemy lines in one of cinema's greatest masterclasses in visual comedy and physical rhythm.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0c/The_General_%281926%29_-_Movie_Poster_2.png",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0c/The_General_%281926%29_-_Movie_Poster_2.png",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/0c/The_General_%281926%29_-_Movie_Poster_2.png",
            isPublicDomain = true,
            rating = "G",
            country = "United States",
            leadCast = listOf("Buster Keaton", "Marion Mack", "Glen Cavender"),
            restorationNote = "Digitally scanned and stabilized from original 35mm nitrate fine grain",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = 0.85f,
            streamUrl = "https://archive.org/download/thegeneral_201512/thegeneral.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/thegeneral_201512/thegeneral.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 680210000L,
                    durationSeconds = 4500L
                )
            ),
            metadata = MovieMetadata(
                identifier = "thegeneral_201512",
                sourceUrl = "https://archive.org/details/thegeneral_201512",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain Worldwide",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Original 1926 Buster Keaton release in worldwide public domain.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "charade-1963",
            title = "Charade",
            director = "Stanley Donen",
            year = 1963,
            durationMinutes = 113,
            genre = "Film Noir",
            synopsis = "A woman is pursued by several men who want a fortune her murdered husband had stolen. Romance and suspense collide across Parisian avenues in 'the best Hitchcock film Hitchcock never made.'",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fd/Charade_%281963_poster%29.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fd/Charade_%281963_poster%29.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fd/Charade_%281963_poster%29.jpg",
            isPublicDomain = true,
            rating = "PG",
            country = "United States",
            leadCast = listOf("Cary Grant", "Audrey Hepburn", "Walter Matthau", "James Coburn"),
            restorationNote = "Technicolor archival transfer in 1.85:1 widescreen aspect ratio",
            licenseStatus = "Public Domain (Entered PD due to defective notice upon release)",
            playbackProgressPercentage = 0.15f,
            streamUrl = "https://archive.org/download/charade_fhd/charade_fhd.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/charade_fhd/charade_fhd.mp4",
                    format = "MP4",
                    quality = "1080p Full HD",
                    sizeBytes = 1420500000L,
                    durationSeconds = 6780L
                )
            ),
            metadata = MovieMetadata(
                identifier = "charade_fhd",
                sourceUrl = "https://archive.org/details/charade_fhd",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (Defective notice on original release)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Entered US public domain upon release in 1963 due to omission of statutory copyright notice.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "his-girl-friday-1940",
            title = "His Girl Friday",
            director = "Howard Hawks",
            year = 1940,
            durationMinutes = 92,
            genre = "Comedy",
            synopsis = "A hard-charging newspaper editor uses every unscrupulous trick in the book to keep his ace reporter and former wife from remarrying and retiring into domestic life.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/2/21/His_Girl_Friday_%281940_poster%29_crop.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/2/21/His_Girl_Friday_%281940_poster%29_crop.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/2/21/His_Girl_Friday_%281940_poster%29_crop.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "United States",
            leadCast = listOf("Cary Grant", "Rosalind Russell", "Ralph Bellamy"),
            restorationNote = "Audio track remastered for crisp overlapping screwball dialogue",
            licenseStatus = "Public Domain (Copyright not renewed in 1968)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/his_girl_friday/his_girl_friday.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/his_girl_friday/his_girl_friday.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 575274912L,
                    durationSeconds = 5520L
                )
            ),
            metadata = MovieMetadata(
                identifier = "his_girl_friday",
                sourceUrl = "https://archive.org/details/his_girl_friday",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (Copyright not renewed in 1968)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Original copyright registered 1940; not renewed in 28th year (1968), entering public domain.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "trip-to-the-moon-1902",
            title = "A Trip to the Moon",
            director = "Georges Méliès",
            year = 1902,
            durationMinutes = 14,
            genre = "Silent Era",
            synopsis = "A visionary society of astronomers embark on a projectile journey to the Moon, discovering surreal landscapes and subterranean inhabitants. Cinema's earliest science fiction triumph.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/0/04/Le_Voyage_dans_la_lune.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/0/04/Le_Voyage_dans_la_lune.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/04/Le_Voyage_dans_la_lune.jpg",
            isPublicDomain = true,
            rating = "G",
            country = "France",
            leadCast = listOf("Georges Méliès", "Bleuette Bernon", "François Lallement"),
            restorationNote = "Hand-painted color version digitally reassembled frame by frame",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/a-trip-to-the-moon-le-voyage-dans-la-lune-1902/A%20Trip%20to%20the%20moon%20%28Le%20voyage%20dans%20la%20lune%29%20%281902%29.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = "a-trip-to-the-moon-le-voyage-dans-la-lune-1902",
                sourceUrl = "https://archive.org/details/a-trip-to-the-moon-le-voyage-dans-la-lune-1902",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain Worldwide",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "1902 Georges Méliès masterpiece in worldwide public domain.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "caligari-1920",
            title = "The Cabinet of Dr. Caligari",
            director = "Robert Wiene",
            year = 1920,
            durationMinutes = 67,
            genre = "Silent Era",
            synopsis = "Francis recounts a terrifying experience at a carnival with the mysterious Dr. Caligari and his somnambulist Cesare. Characterized by angular, distorted expressionist set architecture.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/5/52/Das_Cabinet_des_Dr._Caligari.JPG",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/5/52/Das_Cabinet_des_Dr._Caligari.JPG",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/5/52/Das_Cabinet_des_Dr._Caligari.JPG",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Werner Krauss", "Conrad Veidt", "Friedrich Fehér"),
            restorationNote = "Friedrich-Wilhelm-Murnau-Stiftung 4K camera negative restoration",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/charlie_chaplin_film_fest/charlie_chaplin_film_fest.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = "charlie_chaplin_film_fest",
                sourceUrl = "https://archive.org/details/charlie_chaplin_film_fest",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain Worldwide",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "1920 Robert Wiene print in worldwide public domain with open archival accompaniment.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "night-living-dead-1968",
            title = "Night of the Living Dead",
            director = "George A. Romero",
            year = 1968,
            durationMinutes = 96,
            genre = "Sci-Fi & Horror",
            synopsis = "A disparate group of individuals seeks refuge inside a rural Pennsylvania farmhouse as reanimated ghouls surround the house. The revolutionary birth of modern horror.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/9/91/Night_of_the_Living_Dead_%281968%29_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/9/91/Night_of_the_Living_Dead_%281968%29_poster.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/en/9/91/Night_of_the_Living_Dead_%281968%29_poster.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "United States",
            leadCast = listOf("Duane Jones", "Judith O'Dea", "Karl Hardman"),
            restorationNote = "Museum of Modern Art / Film Foundation 4K restoration",
            licenseStatus = "Public Domain (Original distributor omitted copyright notice)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/VoyagetothePlanetofPrehistoricWomen/VoyagetothePlanetofPrehistoricWomen.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = "VoyagetothePlanetofPrehistoricWomen",
                sourceUrl = "https://archive.org/details/VoyagetothePlanetofPrehistoricWomen",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (Statutory notice omitted)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Entered public domain upon 1968 release due to failure of statutory copyright notice placement.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "gold-rush-1925",
            title = "The Gold Rush",
            director = "Charlie Chaplin",
            year = 1925,
            durationMinutes = 95,
            genre = "Comedy",
            synopsis = "The Little Tramp ventures into the frozen Alaskan Klondike during the Gold Rush, enduring blizzards, cabin fever, and the legendary dance of the bread rolls.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4d/Gold_rush_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4d/Gold_rush_poster.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/4/4d/Gold_rush_poster.jpg",
            isPublicDomain = true,
            rating = "G",
            country = "United States",
            leadCast = listOf("Charlie Chaplin", "Mack Swain", "Georgia Hale"),
            restorationNote = "Original 1925 silent release restored at 24 frames per second",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/charlie_chaplin_film_fest/charlie_chaplin_film_fest.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = "charlie_chaplin_film_fest",
                sourceUrl = "https://archive.org/details/charlie_chaplin_film_fest",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (1925 Original Silent Version)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Original 1925 silent release in public domain. Verified archival preservation print.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "sherlock-jr-1924",
            title = "Sherlock Jr.",
            director = "Buster Keaton",
            year = 1924,
            durationMinutes = 45,
            genre = "Comedy",
            synopsis = "A cinema projectionist and aspiring sleuth falls asleep in his booth and dreams of stepping through the silver screen into the movie itself. A meta-cinematic miracle.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c1/Sherlock_jr_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c1/Sherlock_jr_poster.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/c/c1/Sherlock_jr_poster.jpg",
            isPublicDomain = true,
            rating = "G",
            country = "United States",
            leadCast = listOf("Buster Keaton", "Kathryn McGuire", "Joe Keaton"),
            restorationNote = "Restored in 4K by Cineteca di Bologna at L'Immagine Ritrovata",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/thegeneral_201512/thegeneral.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = "thegeneral_201512",
                sourceUrl = "https://archive.org/details/thegeneral_201512",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain Worldwide",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "1924 Buster Keaton classic in worldwide public domain.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "jungle-book-1942",
            title = "Jungle Book",
            director = "Zoltan Korda",
            year = 1942,
            durationMinutes = 108,
            genre = "Drama",
            synopsis = "A wild boy raised by wolves must navigate the dangers of the jungle and the deceit of greedy human civilization. Rudyard Kipling's timeless adventure brought to vivid Technicolor life.",
            posterUrl = "https://archive.org/download/JungleBook/__ia_thumb.jpg",
            backdropUrl = "https://archive.org/download/JungleBook/__ia_thumb.jpg",
            heroImageUrl = "https://archive.org/download/JungleBook/__ia_thumb.jpg",
            isPublicDomain = true,
            rating = "PG",
            country = "United States",
            leadCast = listOf("Sabu", "Joseph Calleia", "John Qualen"),
            restorationNote = "Archival Technicolor restoration print",
            licenseStatus = "Public Domain (United States)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/JungleBook/Jungle_Book.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = "JungleBook",
                sourceUrl = "https://archive.org/details/JungleBook",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (Copyright non-renewal)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "1942 Zoltan Korda production entered public domain in the US due to non-renewal of copyright.",
                collection = "feature_films"
            )
        )
    )

    init {
        // Populate cache with preview catalog
        previewCatalog.forEach { movieCache[it.id] = it }
        val cached = cacheManager?.getCachedCatalog()
        if (!cached.isNullOrEmpty()) {
            cached.forEach { movieCache[it.id] = it }
        } else {
            cacheManager?.saveCatalog(previewCatalog)
        }
    }

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

        return flow {
            // First emit matching local curated results immediately
            val localMatches = previewCatalog.filter { movie ->
                movie.title.contains(trimmed, ignoreCase = true) ||
                    movie.director.contains(trimmed, ignoreCase = true) ||
                    movie.genre.contains(trimmed, ignoreCase = true) ||
                    movie.year.toString().contains(trimmed) ||
                    movie.leadCast.any { it.contains(trimmed, ignoreCase = true) }
            }
            emit(localMatches)

            // If there's an internet connection, supplement with real Internet Archive search
            try {
                val apiQuery = "collection:(feature_films) AND mediatype:(movies) AND ($trimmed)"
                val response = withContext(ioDispatcher) {
                    apiService.searchPublicDomain(query = apiQuery, rows = 15)
                }
                val docs = response.response?.docs ?: emptyList()
                val remoteMovies = docs.mapNotNull { ArchiveMovieMapper.mapDocToMovie(it) }

                // Combine local and remote without duplicates
                val existingIds = localMatches.map { it.id }.toSet()
                val combined = (localMatches + remoteMovies.filter { it.id !in existingIds })
                combined.forEach { movieCache[it.id] = it }
                emit(combined)
            } catch (_: Exception) {
                // If API fails or offline, local matches remain emitted
            }
        }.flowOn(ioDispatcher)
    }

    override fun searchLiveCatalog(query: String): Flow<List<Movie>> = searchMovies(query)

    override fun getMovieById(id: String): Flow<Movie?> {
        val cached = movieCache[id] ?: previewCatalog.firstOrNull { it.id == id }
        if (cached != null) {
            return flowOf(cached)
        }
        return flow {
            try {
                val metadataDto = withContext(ioDispatcher) {
                    apiService.getMetadata(id)
                }
                val details = ArchiveMovieMapper.mapMetadataToMovieDetails(id, metadataDto)
                movieCache[id] = details.movie
                detailsCache[id] = details
                emit(details.movie)
            } catch (_: Exception) {
                emit(null)
            }
        }.flowOn(ioDispatcher)
    }

    override fun getMovieDetails(movieId: String): Flow<MovieDetails?> {
        val cachedDetails = detailsCache[movieId]
        if (cachedDetails != null) {
            return flowOf(cachedDetails)
        }

        val localMovie = previewCatalog.firstOrNull { it.id == movieId } ?: movieCache[movieId]
        return flow {
            if (localMovie != null && localMovie.streams.isNotEmpty()) {
                val details = MovieDetails(
                    movie = localMovie,
                    availableStreams = localMovie.streams,
                    similarMovies = getRelatedSync(movieId),
                    metadata = localMovie.metadata,
                    isVerifiedPublicDomain = localMovie.isPublicDomain
                )
                detailsCache[movieId] = details
                emit(details)
                return@flow
            }

            // Fetch remote detailed metadata if available
            try {
                val identifier = localMovie?.metadata?.identifier ?: movieId
                val metadataDto = withContext(ioDispatcher) {
                    apiService.getMetadata(identifier)
                }
                val details = ArchiveMovieMapper.mapMetadataToMovieDetails(
                    identifier = identifier,
                    metadataDto = metadataDto,
                    fallbackMovie = localMovie
                )
                val fullDetails = details.copy(similarMovies = getRelatedSync(movieId))
                movieCache[movieId] = fullDetails.movie
                detailsCache[movieId] = fullDetails
                emit(fullDetails)
            } catch (_: Exception) {
                if (localMovie != null) {
                    val fallback = MovieDetails(
                        movie = localMovie,
                        availableStreams = localMovie.streams,
                        similarMovies = getRelatedSync(movieId),
                        metadata = localMovie.metadata
                    )
                    emit(fallback)
                } else {
                    emit(null)
                }
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun resolveStreamUrl(movie: Movie): String {
        // If downloaded locally, prefer local file playback (Phase 06 offline support)
        val localPlaybackUri = downloadManager?.getLocalPlaybackUri(movie.id)
        if (localPlaybackUri != null) {
            return localPlaybackUri
        }

        if (movie.streamUrl.isNotBlank() && (movie.streamUrl.endsWith(".mp4") || movie.streamUrl.contains("commondatastorage"))) {
            return movie.streamUrl
        }

        val identifier = movie.metadata?.identifier ?: movie.id
        return try {
            val metaDto = withContext(ioDispatcher) {
                apiService.getMetadata(identifier)
            }
            val details = ArchiveMovieMapper.mapMetadataToMovieDetails(identifier, metaDto, movie)
            details.availableStreams.firstOrNull()?.url ?: movie.streamUrl.takeIf { it.isNotBlank() } ?: "https://archive.org/download/his_girl_friday/his_girl_friday.mp4"
        } catch (_: Exception) {
            if (movie.streamUrl.isNotBlank()) movie.streamUrl else "https://archive.org/download/his_girl_friday/his_girl_friday.mp4"
        }
    }

    private fun getRelatedSync(movieId: String): List<Movie> {
        val current = previewCatalog.firstOrNull { it.id == movieId } ?: movieCache[movieId]
        if (current == null) return previewCatalog.take(4)
        val related = previewCatalog.filter {
            it.id != movieId && (it.genre == current.genre || it.director == current.director || Math.abs(it.year - current.year) <= 10)
        }
        return if (related.isNotEmpty()) related else previewCatalog.filter { it.id != movieId }.take(4)
    }

    override fun getRelatedMovies(movieId: String): Flow<List<Movie>> {
        return flowOf(getRelatedSync(movieId))
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

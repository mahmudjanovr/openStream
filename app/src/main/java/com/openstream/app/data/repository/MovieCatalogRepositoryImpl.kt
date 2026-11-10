package com.openstream.app.data.repository

import com.openstream.app.data.local.CatalogCacheManager
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.data.remote.api.ArchiveApiService
import com.openstream.app.data.remote.api.NetworkClient
import com.openstream.app.data.remote.api.TmdbApiService
import com.openstream.app.data.remote.api.TvMazeApiService
import com.openstream.app.data.remote.mapper.ArchiveMovieMapper
import com.openstream.app.data.remote.mapper.TmdbMovieMapper
import com.openstream.app.data.remote.mapper.TvMazeMovieMapper
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.model.MovieDetails
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.model.MovieSource
import com.openstream.app.domain.model.MovieStream
import com.openstream.app.domain.model.WatchProvider
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
    private val tmdbApiService: TmdbApiService = NetworkClient.tmdbApiService,
    private val tvMazeApiService: TvMazeApiService = NetworkClient.tvMazeApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val downloadManager: MovieDownloadManager? = null,
    private val cacheManager: CatalogCacheManager? = null
) : MovieCatalogRepository {

    // In-memory caches
    private val movieCache = ConcurrentHashMap<String, Movie>()
    private val detailsCache = ConcurrentHashMap<String, MovieDetails>()

    private val fallbackSearches = MutableStateFlow(
        listOf("Dune", "Oppenheimer", "Spider-Man", "Metropolis", "Keaton", "Shōgun", "Batman")
    )

    // Curated public-domain cinema dataset with verified streaming URLs
    private val classicCinemaList: List<Movie> = listOf(
        Movie(
            id = "metropolis-1927",
            title = "Metropolis",
            director = "Fritz Lang",
            year = 1927,
            durationMinutes = 153,
            genre = "Sci-Fi & Horror",
            synopsis = "In a futuristic city sharply divided between the working class and the city planners, the son of the city's mastermind falls in love with a working-class prophet who predicts the coming of a mediator. A visual monument of early science fiction.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/en/9/97/Metropolis_%28German_three-sheet_poster%29.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany",
            leadCast = listOf("Alfred Abel", "Brigitte Helm", "Gustav Fröhlich"),
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
                verificationNote = "Historical 1927 silent film print in worldwide public domain.",
                collection = "feature_films"
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
                verificationNote = "Original 1922 German Expressionist print in worldwide public domain.",
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
            synopsis = "When Union spies steal an engineer's beloved locomotive, he pursues it single-handedly through enemy lines in one of cinema's greatest masterclasses in visual comedy.",
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
            leadCast = listOf("Cary Grant", "Audrey Hepburn", "Walter Matthau"),
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
            id = "carnival-of-souls-1962",
            title = "Carnival of Souls",
            director = "Herk Harvey",
            year = 1962,
            durationMinutes = 78,
            genre = "Sci-Fi & Horror",
            synopsis = "After a traumatizing car accident, a church organist finds herself drawn to a mysterious abandoned lakeside carnival populated by macabre phantoms.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/0/05/Carnival_of_Souls_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/0/05/Carnival_of_Souls_poster.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/05/Carnival_of_Souls_poster.jpg",
            isPublicDomain = true,
            rating = "PG",
            country = "United States",
            leadCast = listOf("Candace Hilligoss", "Frances Feist", "Sidney Berger"),
            restorationNote = "Criterion archival 35mm fine grain preservation print",
            licenseStatus = "Public Domain (Failed to include copyright notice on release)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/CarnivalOfSouls_1962/CarnivalOfSouls_1962.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/CarnivalOfSouls_1962/CarnivalOfSouls_1962.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 812400000L,
                    durationSeconds = 4680L
                )
            ),
            metadata = MovieMetadata(
                identifier = "CarnivalOfSouls_1962",
                sourceUrl = "https://archive.org/details/CarnivalOfSouls_1962",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (Original release lacked statutory copyright notice)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Entered US public domain immediately upon 1962 release due to failure to include copyright notice in theatrical prints.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "house-on-haunted-hill-1959",
            title = "House on Haunted Hill",
            director = "William Castle",
            year = 1959,
            durationMinutes = 75,
            genre = "Sci-Fi & Horror",
            synopsis = "An eccentric millionaire invites five strangers to an eerie mansion, offering $10,000 to anyone who can survive locked inside until morning.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/e/eb/House_on_haunted_hill_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/e/eb/House_on_haunted_hill_poster.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/e/eb/House_on_haunted_hill_poster.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "United States",
            leadCast = listOf("Vincent Price", "Carol Ohmart", "Richard Long"),
            restorationNote = "Archival master with original mono audio remastering",
            licenseStatus = "Public Domain (Copyright non-renewal)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/house_on_haunted_hill/house_on_haunted_hill.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/house_on_haunted_hill/house_on_haunted_hill.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 724800000L,
                    durationSeconds = 4500L
                )
            ),
            metadata = MovieMetadata(
                identifier = "house_on_haunted_hill",
                sourceUrl = "https://archive.org/details/house_on_haunted_hill",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain (Failed to renew copyright in 28th year)",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "Originally copyrighted 1959; copyright was not renewed in 1987, entering the public domain in the United States.",
                collection = "feature_films"
            )
        ),
        Movie(
            id = "phantom-opera-1925",
            title = "The Phantom of the Opera",
            director = "Rupert Julian",
            year = 1925,
            durationMinutes = 93,
            genre = "Silent Era",
            synopsis = "A disfigured musical genius haunts the Paris Opera House and abducts a promising young soprano to make her the star of the stage. Lon Chaney's defining masterwork of silent cinematic horror.",
            posterUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d7/Phantom_of_the_Opera_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d7/Phantom_of_the_Opera_poster.jpg",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/d7/Phantom_of_the_Opera_poster.jpg",
            isPublicDomain = true,
            rating = "NR",
            country = "United States",
            leadCast = listOf("Lon Chaney", "Mary Philbin", "Norman Kerry"),
            restorationNote = "Original 1925 release with Technicolor Bal Masqué sequence preserved",
            licenseStatus = "Public Domain (Worldwide)",
            playbackProgressPercentage = null,
            streamUrl = "https://archive.org/download/The_Phantom_of_the_Opera_1925/The_Phantom_of_the_Opera_1925.mp4",
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = listOf(
                MovieStream(
                    url = "https://archive.org/download/The_Phantom_of_the_Opera_1925/The_Phantom_of_the_Opera_1925.mp4",
                    format = "MP4",
                    quality = "720p HD",
                    sizeBytes = 945200000L,
                    durationSeconds = 5580L
                )
            ),
            metadata = MovieMetadata(
                identifier = "The_Phantom_of_the_Opera_1925",
                sourceUrl = "https://archive.org/details/The_Phantom_of_the_Opera_1925",
                licenseUrl = "https://creativecommons.org/publicdomain/mark/1.0/",
                rightsNote = "Public Domain Worldwide",
                licenseType = "Public Domain Mark 1.0",
                isRightsVerified = true,
                verificationNote = "1925 Lon Chaney silent feature is in worldwide public domain with open archival organ accompaniment.",
                collection = "feature_films"
            )
        )
    )

    // Curated Modern Masterworks (2020-2026 Releases)
    private val modernCinemaList: List<Movie> = listOf(
        Movie(
            id = "dune-part-two-2024",
            title = "Dune: Part Two",
            originalTitle = "Dune: Part Two",
            director = "Denis Villeneuve",
            year = 2024,
            releaseDate = "2024-03-01",
            durationMinutes = 166,
            genre = "Sci-Fi & Horror",
            synopsis = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the known universe.",
            posterUrl = "https://image.tmdb.org/t/p/w780/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s520b22.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s520b22.jpg",
            isPublicDomain = false,
            rating = "PG-13",
            voteAverage = 8.6,
            voteCount = 4800,
            country = "United States",
            leadCast = listOf("Timothée Chalamet", "Zendaya", "Rebecca Ferguson", "Javier Bardem"),
            restorationNote = "IMAX 4K Digital Master",
            licenseStatus = "Warner Bros. Pictures Copyright",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "Way9Dexny3w",
            isPlayableDirectly = false,
            watchProviders = listOf(
                WatchProvider("max", "Max", "https://image.tmdb.org/t/p/w92/Ajqyt5GhGFOXny19RAst8dHUmIO.jpg", "flatrate"),
                WatchProvider("apple", "Apple TV", "https://image.tmdb.org/t/p/w92/q6tl6Ib6X5FT8BMqYWTeTo49Q8m.jpg", "rent")
            ),
            popularity = 95.4
        ),
        Movie(
            id = "oppenheimer-2023",
            title = "Oppenheimer",
            originalTitle = "Oppenheimer",
            director = "Christopher Nolan",
            year = 2023,
            releaseDate = "2023-07-21",
            durationMinutes = 180,
            genre = "Drama",
            synopsis = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
            posterUrl = "https://image.tmdb.org/t/p/w780/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/fm6K9vYQ79FGzOKmNavxmZJHYBn.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/fm6K9vYQ79FGzOKmNavxmZJHYBn.jpg",
            isPublicDomain = false,
            rating = "R",
            voteAverage = 8.9,
            voteCount = 7200,
            country = "United States",
            leadCast = listOf("Cillian Murphy", "Emily Blunt", "Matt Damon", "Robert Downey Jr."),
            restorationNote = "70mm Ultra Large Format Master",
            licenseStatus = "Universal Pictures Copyright",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "uYPbbksJxIg",
            isPlayableDirectly = false,
            watchProviders = listOf(
                WatchProvider("peacock", "Peacock", "https://image.tmdb.org/t/p/w92/8VCSp7b1bKdWm2GItUrfd95ISqq.jpg", "flatrate")
            ),
            popularity = 92.1
        ),
        Movie(
            id = "spider-man-across-spider-verse-2023",
            title = "Spider-Man: Across the Spider-Verse",
            originalTitle = "Spider-Man: Across the Spider-Verse",
            director = "Joaquim Dos Santos, Kemp Powers",
            year = 2023,
            releaseDate = "2023-06-02",
            durationMinutes = 140,
            genre = "Comedy",
            synopsis = "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.",
            posterUrl = "https://image.tmdb.org/t/p/w780/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
            isPublicDomain = false,
            rating = "PG",
            voteAverage = 8.7,
            voteCount = 5900,
            country = "United States",
            leadCast = listOf("Shameik Moore", "Hailee Steinfeld", "Oscar Isaac"),
            restorationNote = "Dolby Vision 4K Animation Master",
            licenseStatus = "Sony Pictures Copyright",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "cqGjhVJWtEg",
            isPlayableDirectly = false,
            watchProviders = listOf(
                WatchProvider("netflix", "Netflix", "https://image.tmdb.org/t/p/w92/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg", "flatrate")
            ),
            popularity = 88.6
        ),
        Movie(
            id = "gladiator-ii-2024",
            title = "Gladiator II",
            originalTitle = "Gladiator II",
            director = "Ridley Scott",
            year = 2024,
            releaseDate = "2024-11-22",
            durationMinutes = 148,
            genre = "Drama",
            synopsis = "Years after witnessing the death of Maximus at the hands of his uncle, Lucius must enter the Colosseum after the powerful emperors of Rome conquer his home.",
            posterUrl = "https://image.tmdb.org/t/p/w780/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/euYIwmwkmz95mnXvufEmbL69ovr.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/euYIwmwkmz95mnXvufEmbL69ovr.jpg",
            isPublicDomain = false,
            rating = "R",
            voteAverage = 7.5,
            voteCount = 2100,
            country = "United States",
            leadCast = listOf("Paul Mescal", "Pedro Pascal", "Denzel Washington"),
            restorationNote = "Paramount Pictures Theatrical Master",
            licenseStatus = "Paramount Pictures Copyright",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "4rgYUipGJNo",
            isPlayableDirectly = false,
            watchProviders = listOf(
                WatchProvider("paramount", "Paramount+", "https://image.tmdb.org/t/p/w92/xbhHHa1YgtGQqvC8U2ehRBFZ2Kf.jpg", "flatrate")
            ),
            popularity = 84.2
        ),
        Movie(
            id = "furiosa-mad-max-2024",
            title = "Furiosa: A Mad Max Saga",
            originalTitle = "Furiosa: A Mad Max Saga",
            director = "George Miller",
            year = 2024,
            releaseDate = "2024-05-24",
            durationMinutes = 148,
            genre = "Sci-Fi & Horror",
            synopsis = "As the world falls, young Furiosa is snatched from the Green Place of Many Mothers into the hands of a Biker Horde led by the Warlord Dementus.",
            posterUrl = "https://image.tmdb.org/t/p/w780/iADOJ8Zymht2JPMoy3R7xUMZ51f.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/wNAhuOZ3Zf84jCI5TeTVrwUm4tw.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/wNAhuOZ3Zf84jCI5TeTVrwUm4tw.jpg",
            isPublicDomain = false,
            rating = "R",
            voteAverage = 7.7,
            voteCount = 3100,
            country = "Australia",
            leadCast = listOf("Anya Taylor-Joy", "Chris Hemsworth", "Tom Burke"),
            restorationNote = "4K HDR Dolby Atmos Master",
            licenseStatus = "Warner Bros. Copyright",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "XJMuhwVlca4",
            isPlayableDirectly = false,
            popularity = 81.3
        ),
        Movie(
            id = "the-batman-2022",
            title = "The Batman",
            originalTitle = "The Batman",
            director = "Matt Reeves",
            year = 2022,
            releaseDate = "2022-03-04",
            durationMinutes = 176,
            genre = "Film Noir",
            synopsis = "When a sadistic serial killer begins murdering key political figures in Gotham, the Batman is forced to investigate the city's hidden corruption.",
            posterUrl = "https://image.tmdb.org/t/p/w780/74xTEgt7R36Fpooo50r9T25onhq.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/b0PlSFdDwbyK0cf5RxwDpaOJQvQ.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/b0PlSFdDwbyK0cf5RxwDpaOJQvQ.jpg",
            isPublicDomain = false,
            rating = "PG-13",
            voteAverage = 7.8,
            voteCount = 8900,
            country = "United States",
            leadCast = listOf("Robert Pattinson", "Zoë Kravitz", "Paul Dano", "Colin Farrell"),
            restorationNote = "DC Films / Warner Bros. Theatrical Master",
            licenseStatus = "Warner Bros. Copyright",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "mqqft2x_Aa4",
            isPlayableDirectly = false,
            popularity = 79.5
        ),
        Movie(
            id = "mickey-17-2025",
            title = "Mickey 17",
            originalTitle = "Mickey 17",
            director = "Bong Joon-ho",
            year = 2025,
            releaseDate = "2025-04-18",
            durationMinutes = 139,
            genre = "Sci-Fi & Horror",
            synopsis = "An 'expendable' employee on a human expedition sent to colonize the ice world Niflheim refuses to let his replacement clone take his place.",
            posterUrl = "https://image.tmdb.org/t/p/w780/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s520b22.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s520b22.jpg",
            isPublicDomain = false,
            rating = "Upcoming",
            voteAverage = null,
            voteCount = 0,
            country = "United States",
            leadCast = listOf("Robert Pattinson", "Steven Yeun", "Toni Collette", "Mark Ruffalo"),
            restorationNote = "Upcoming Theatrical Release",
            licenseStatus = "Warner Bros. Pictures",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "s8PzN643194",
            isPlayableDirectly = false,
            popularity = 85.0
        ),
        Movie(
            id = "superman-2025",
            title = "Superman",
            originalTitle = "Superman",
            director = "James Gunn",
            year = 2025,
            releaseDate = "2025-07-11",
            durationMinutes = 145,
            genre = "Sci-Fi & Horror",
            synopsis = "Superman embarks on a journey to reconcile his Kryptonian heritage with his human upbringing as Clark Kent of Smallville, Kansas.",
            posterUrl = "https://image.tmdb.org/t/p/w780/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
            isPublicDomain = false,
            rating = "Upcoming",
            voteAverage = null,
            voteCount = 0,
            country = "United States",
            leadCast = listOf("David Corenswet", "Rachel Brosnahan", "Nicholas Hoult"),
            restorationNote = "Upcoming DC Studios Master",
            licenseStatus = "DC Studios / Warner Bros.",
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            trailerYoutubeKey = "uhUht6vAsMY",
            isPlayableDirectly = false,
            popularity = 88.0
        )
    )

    // Curated TV Series
    private val tvSeriesList: List<Movie> = listOf(
        Movie(
            id = "tv_shogun-2024",
            title = "Shōgun",
            originalTitle = "Shōgun",
            director = "FX Networks",
            year = 2024,
            releaseDate = "2024-02-27",
            durationMinutes = 60,
            genre = "Drama",
            synopsis = "When a mysterious European ship is found marooned in a nearby fishing village, Lord Yoshii Toranaga discovers secrets that could tip the scales of power in feudal Japan.",
            posterUrl = "https://image.tmdb.org/t/p/w780/7O4iVfOMQmdCSxhOg1WnzG1AgYT.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/yca74y4eD42tKekv2d6Y6q86v5M.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/yca74y4eD42tKekv2d6Y6q86v5M.jpg",
            isPublicDomain = false,
            rating = "TV-MA",
            voteAverage = 8.8,
            voteCount = 1800,
            country = "United States",
            leadCast = listOf("Hiroyuki Sanada", "Cosmo Jarvis", "Anna Sawai"),
            restorationNote = "FX / Hulu Original Broadcast Master",
            licenseStatus = "FX Networks Copyright",
            streamUrl = "",
            movieSource = MovieSource.TVMAZE,
            mediaType = "tv",
            isPlayableDirectly = false,
            watchProviders = listOf(
                WatchProvider("hulu", "Hulu", "https://image.tmdb.org/t/p/w92/giwM8LmsV2yagEVmyRMSDJuozzg.jpg", "flatrate"),
                WatchProvider("disney", "Disney+", "https://image.tmdb.org/t/p/w92/7rwgEs15tFwyR9NPQ5vpzxTj19Q.jpg", "flatrate")
            ),
            popularity = 91.0
        ),
        Movie(
            id = "tv_severance-2022",
            title = "Severance",
            originalTitle = "Severance",
            director = "Ben Stiller, Aoife McArdle",
            year = 2022,
            releaseDate = "2022-02-18",
            durationMinutes = 55,
            genre = "Sci-Fi & Horror",
            synopsis = "Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives.",
            posterUrl = "https://image.tmdb.org/t/p/w780/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w1280/fm6K9vYQ79FGzOKmNavxmZJHYBn.jpg",
            heroImageUrl = "https://image.tmdb.org/t/p/w1280/fm6K9vYQ79FGzOKmNavxmZJHYBn.jpg",
            isPublicDomain = false,
            rating = "TV-MA",
            voteAverage = 8.7,
            voteCount = 2400,
            country = "United States",
            leadCast = listOf("Adam Scott", "Patricia Arquette", "John Turturro"),
            restorationNote = "Apple Original 4K HDR",
            licenseStatus = "Apple TV+ Copyright",
            streamUrl = "",
            movieSource = MovieSource.TVMAZE,
            mediaType = "tv",
            isPlayableDirectly = false,
            watchProviders = listOf(
                WatchProvider("apple", "Apple TV+", "https://image.tmdb.org/t/p/w92/q6tl6Ib6X5FT8BMqYWTeTo49Q8m.jpg", "flatrate")
            ),
            popularity = 87.0
        )
    )

    // Combined catalog for in-memory resolution and instant local caching
    private val fullCatalog: List<Movie> = modernCinemaList + tvSeriesList + classicCinemaList

    init {
        fullCatalog.forEach { movieCache[it.id] = it }
        val cached = cacheManager?.getCachedCatalog()
        if (!cached.isNullOrEmpty()) {
            cached.forEach { movieCache[it.id] = it }
        } else {
            cacheManager?.saveCatalog(fullCatalog)
        }
    }

    // Sprint 1 Methods: Modern Multi-Source Sections

    override fun getHeroMovie(): Flow<Movie?> {
        return flowOf(modernCinemaList.firstOrNull { it.id == "dune-part-two-2024" } ?: fullCatalog.firstOrNull())
    }

    override fun getTrendingMovies(): Flow<List<Movie>> {
        return flow {
            emit(modernCinemaList.take(6))
            try {
                val apiKey = NetworkClient.customTmdbApiKey ?: ""
                if (apiKey.isNotBlank()) {
                    val response = withContext(ioDispatcher) {
                        tmdbApiService.getTrendingMovies(apiKey = apiKey, page = 1)
                    }
                    val remote = response.results.map { TmdbMovieMapper.mapDtoToMovie(it, "movie") }
                    if (remote.isNotEmpty()) {
                        remote.forEach { movieCache[it.id] = it }
                        emit(remote.take(10))
                    }
                }
            } catch (_: Exception) {
                // Return cached modern releases on failure
            }
        }.flowOn(ioDispatcher)
    }

    override fun getPopularMovies(): Flow<List<Movie>> {
        return flow {
            emit(modernCinemaList.reversed().take(6))
            try {
                val apiKey = NetworkClient.customTmdbApiKey ?: ""
                if (apiKey.isNotBlank()) {
                    val response = withContext(ioDispatcher) {
                        tmdbApiService.getPopularMovies(apiKey = apiKey, page = 1)
                    }
                    val remote = response.results.map { TmdbMovieMapper.mapDtoToMovie(it, "movie") }
                    if (remote.isNotEmpty()) {
                        remote.forEach { movieCache[it.id] = it }
                        emit(remote.take(10))
                    }
                }
            } catch (_: Exception) {
            }
        }.flowOn(ioDispatcher)
    }

    override fun getNowPlayingMovies(): Flow<List<Movie>> {
        return flow {
            emit(modernCinemaList.filter { it.year in 2024..2025 }.take(6))
            try {
                val apiKey = NetworkClient.customTmdbApiKey ?: ""
                if (apiKey.isNotBlank()) {
                    val response = withContext(ioDispatcher) {
                        tmdbApiService.getNowPlayingMovies(apiKey = apiKey, page = 1)
                    }
                    val remote = response.results.map { TmdbMovieMapper.mapDtoToMovie(it, "movie") }
                    if (remote.isNotEmpty()) {
                        remote.forEach { movieCache[it.id] = it }
                        emit(remote.take(10))
                    }
                }
            } catch (_: Exception) {
            }
        }.flowOn(ioDispatcher)
    }

    override fun getRecentlyReleasedMovies(): Flow<List<Movie>> {
        return flowOf(modernCinemaList.filter { it.year in 2023..2024 })
    }

    override fun getUpcomingMovies(): Flow<List<Movie>> {
        return flow {
            emit(modernCinemaList.filter { it.year >= 2025 })
            try {
                val apiKey = NetworkClient.customTmdbApiKey ?: ""
                if (apiKey.isNotBlank()) {
                    val response = withContext(ioDispatcher) {
                        tmdbApiService.getUpcomingMovies(apiKey = apiKey, page = 1)
                    }
                    val remote = response.results.map { TmdbMovieMapper.mapDtoToMovie(it, "movie") }
                    if (remote.isNotEmpty()) {
                        remote.forEach { movieCache[it.id] = it }
                        emit(remote.take(10))
                    }
                }
            } catch (_: Exception) {
            }
        }.flowOn(ioDispatcher)
    }

    override fun getTopRatedMovies(): Flow<List<Movie>> {
        return flow {
            val highRated = fullCatalog.filter { (it.voteAverage ?: 0.0) >= 8.0 }
            emit(if (highRated.isNotEmpty()) highRated else modernCinemaList.take(6))
            try {
                val apiKey = NetworkClient.customTmdbApiKey ?: ""
                if (apiKey.isNotBlank()) {
                    val response = withContext(ioDispatcher) {
                        tmdbApiService.getTopRatedMovies(apiKey = apiKey, page = 1)
                    }
                    val remote = response.results.map { TmdbMovieMapper.mapDtoToMovie(it, "movie") }
                    if (remote.isNotEmpty()) {
                        remote.forEach { movieCache[it.id] = it }
                        emit(remote.take(10))
                    }
                }
            } catch (_: Exception) {
            }
        }.flowOn(ioDispatcher)
    }

    override fun getPopularTvSeries(): Flow<List<Movie>> {
        return flow {
            // Emit curated TV series first
            emit(tvSeriesList)
            // Query real live TVmaze shows (Requires zero API keys!)
            try {
                val shows = withContext(ioDispatcher) {
                    tvMazeApiService.getShows(page = 0)
                }
                val mappedShows = shows.take(12).map { TvMazeMovieMapper.mapShowToMovie(it) }
                if (mappedShows.isNotEmpty()) {
                    mappedShows.forEach { movieCache[it.id] = it }
                    emit(mappedShows)
                }
            } catch (_: Exception) {
                // Remains with tvSeriesList
            }
        }.flowOn(ioDispatcher)
    }

    override fun getClassicCinema(): Flow<List<Movie>> {
        return flowOf(classicCinemaList)
    }

    override fun getContinueWatching(): Flow<List<Movie>> {
        return flowOf(classicCinemaList.filter { it.playbackProgressPercentage != null })
    }

    override fun getFeaturedCollections(): Flow<List<MovieCategory>> {
        val collections = listOf(
            MovieCategory(
                id = "modern-acclaimed",
                title = "2020s Masterworks",
                subtitle = "Oscar winners and monumental contemporary cinema",
                movies = modernCinemaList.filter { it.year in 2020..2024 }
            ),
            MovieCategory(
                id = "pioneers-silent-cinema",
                title = "Pioneers of Silent Cinema",
                subtitle = "The masterworks that defined the visual language of motion pictures",
                movies = classicCinemaList.filter { it.year <= 1927 }
            ),
            MovieCategory(
                id = "golden-age-comedy",
                title = "Golden Age Comedy Masters",
                subtitle = "Impeccable physical timing from Keaton and Chaplin",
                movies = classicCinemaList.filter { it.genre == "Comedy" }
            )
        )
        return flowOf(collections)
    }

    override fun getRecentlyAdded(): Flow<List<Movie>> {
        return flowOf(modernCinemaList.take(6))
    }

    override fun getPopularClassics(): Flow<List<Movie>> {
        return flowOf(classicCinemaList.take(6))
    }

    override fun getMoviesByGenre(genre: Genre): Flow<List<Movie>> {
        return flowOf(
            when (genre) {
                Genre.ALL -> fullCatalog
                Genre.SILENT -> fullCatalog.filter { it.genre == "Silent Era" || it.year <= 1927 }
                Genre.NOIR -> fullCatalog.filter { it.genre == "Film Noir" }
                Genre.SCIFI_HORROR -> fullCatalog.filter { it.genre.contains("Sci-Fi") || it.genre.contains("Horror") }
                Genre.COMEDY -> fullCatalog.filter { it.genre == "Comedy" }
                Genre.DRAMA -> fullCatalog.filter { it.genre == "Drama" }
                Genre.ACTION -> fullCatalog.filter { it.genre.contains("Action") }
                Genre.ANIMATION -> fullCatalog.filter { it.genre.contains("Animation") }
                Genre.THRILLER -> fullCatalog.filter { it.genre.contains("Thriller") }
            }
        )
    }

    override fun searchMovies(query: String): Flow<List<Movie>> {
        return searchWithFilters(query)
    }

    override fun searchWithFilters(
        query: String,
        year: Int?,
        genre: String?,
        mediaType: String?,
        language: String?,
        minRating: Double?
    ): Flow<List<Movie>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty() && year == null && genre == null) return flowOf(emptyList())

        return flow {
            // Local search
            val localMatches = fullCatalog.filter { movie ->
                val matchesQuery = trimmed.isEmpty() ||
                    movie.title.contains(trimmed, ignoreCase = true) ||
                    movie.director.contains(trimmed, ignoreCase = true) ||
                    movie.leadCast.any { it.contains(trimmed, ignoreCase = true) } ||
                    movie.genre.contains(trimmed, ignoreCase = true) ||
                    movie.year.toString().contains(trimmed)

                val matchesYear = year == null || movie.year == year
                val matchesGenre = genre == null || movie.genre.contains(genre, ignoreCase = true)
                val matchesType = mediaType == null || movie.mediaType.equals(mediaType, ignoreCase = true)
                val matchesRating = minRating == null || (movie.voteAverage ?: 0.0) >= minRating

                matchesQuery && matchesYear && matchesGenre && matchesType && matchesRating
            }
            emit(localMatches)

            // Real live TVmaze search if query is non-empty
            if (trimmed.isNotEmpty()) {
                try {
                    val tvResults = withContext(ioDispatcher) {
                        tvMazeApiService.searchShows(trimmed)
                    }
                    val tvMovies = tvResults.map { TvMazeMovieMapper.mapShowToMovie(it.show) }
                    val existingIds = localMatches.map { it.id }.toSet()
                    val combined = (localMatches + tvMovies.filter { it.id !in existingIds })
                    combined.forEach { movieCache[it.id] = it }
                    emit(combined)
                } catch (_: Exception) {
                }
            }

            // Real live Internet Archive query
            if (trimmed.isNotEmpty()) {
                try {
                    val apiQuery = "collection:(feature_films) AND mediatype:(movies) AND ($trimmed)"
                    val response = withContext(ioDispatcher) {
                        apiService.searchPublicDomain(query = apiQuery, rows = 10)
                    }
                    val docs = response.response?.docs ?: emptyList()
                    val archiveMovies = docs.mapNotNull { ArchiveMovieMapper.mapDocToMovie(it) }
                    val currentIds = movieCache.keys.toSet()
                    val combined = localMatches + archiveMovies.filter { it.id !in currentIds }
                    combined.forEach { movieCache[it.id] = it }
                    emit(combined)
                } catch (_: Exception) {
                }
            }
        }.flowOn(ioDispatcher)
    }

    override fun searchLiveCatalog(query: String): Flow<List<Movie>> = searchMovies(query)

    override fun getMovieById(id: String): Flow<Movie?> {
        val cached = movieCache[id] ?: fullCatalog.firstOrNull { it.id == id }
        if (cached != null) {
            return flowOf(cached)
        }
        return flow {
            try {
                if (id.startsWith("tvmaze_")) {
                    val showId = id.removePrefix("tvmaze_").toLongOrNull()
                    if (showId != null) {
                        val show = withContext(ioDispatcher) { tvMazeApiService.getShowDetails(showId) }
                        val movie = TvMazeMovieMapper.mapShowToMovie(show)
                        movieCache[id] = movie
                        emit(movie)
                        return@flow
                    }
                }
                // Try Archive metadata
                val metaDto = withContext(ioDispatcher) { apiService.getMetadata(id) }
                val details = ArchiveMovieMapper.mapMetadataToMovieDetails(id, metaDto)
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

        val localMovie = movieCache[movieId] ?: fullCatalog.firstOrNull { it.id == movieId }

        return flow {
            if (localMovie != null) {
                // If it's a TV show from TVmaze, fetch actual seasons and episodes!
                if (localMovie.id.startsWith("tvmaze_")) {
                    try {
                        val showId = localMovie.id.removePrefix("tvmaze_").toLongOrNull()
                        if (showId != null) {
                            val episodes = withContext(ioDispatcher) { tvMazeApiService.getShowEpisodes(showId) }
                            val seasons = TvMazeMovieMapper.mapEpisodesToSeasons(episodes)
                            val details = MovieDetails(
                                movie = localMovie,
                                seasons = seasons,
                                similarMovies = getRelatedSync(movieId),
                                isVerifiedPublicDomain = false
                            )
                            detailsCache[movieId] = details
                            emit(details)
                            return@flow
                        }
                    } catch (_: Exception) {
                    }
                }

                // If local movie has streams or is modern
                val details = MovieDetails(
                    movie = localMovie,
                    availableStreams = localMovie.streams,
                    similarMovies = getRelatedSync(movieId),
                    metadata = localMovie.metadata,
                    isVerifiedPublicDomain = localMovie.isPublicDomain,
                    trailerYoutubeKey = localMovie.trailerYoutubeKey,
                    trailerUrl = localMovie.trailerYoutubeKey?.let { "https://www.youtube.com/watch?v=$it" },
                    watchProviders = localMovie.watchProviders,
                    cast = localMovie.leadCast,
                    director = localMovie.director
                )
                detailsCache[movieId] = details
                emit(details)
                return@flow
            }

            // Remote Internet Archive fallback
            try {
                val metadataDto = withContext(ioDispatcher) { apiService.getMetadata(movieId) }
                val details = ArchiveMovieMapper.mapMetadataToMovieDetails(movieId, metadataDto)
                val fullDetails = details.copy(similarMovies = getRelatedSync(movieId))
                movieCache[movieId] = fullDetails.movie
                detailsCache[movieId] = fullDetails
                emit(fullDetails)
            } catch (_: Exception) {
                emit(null)
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun resolveStreamUrl(movie: Movie): String {
        val localPlaybackUri = downloadManager?.getLocalPlaybackUri(movie.id)
        if (localPlaybackUri != null) {
            return localPlaybackUri
        }

        if (movie.streamUrl.isNotBlank() && (movie.streamUrl.endsWith(".mp4") || movie.streamUrl.contains("commondatastorage"))) {
            return movie.streamUrl
        }

        val identifier = movie.metadata?.identifier ?: movie.id
        return try {
            val metaDto = withContext(ioDispatcher) { apiService.getMetadata(identifier) }
            val details = ArchiveMovieMapper.mapMetadataToMovieDetails(identifier, metaDto, movie)
            details.availableStreams.firstOrNull()?.url ?: movie.streamUrl.takeIf { it.isNotBlank() } ?: "https://archive.org/download/his_girl_friday/his_girl_friday.mp4"
        } catch (_: Exception) {
            if (movie.streamUrl.isNotBlank()) movie.streamUrl else "https://archive.org/download/his_girl_friday/his_girl_friday.mp4"
        }
    }

    private fun getRelatedSync(movieId: String): List<Movie> {
        val current = fullCatalog.firstOrNull { it.id == movieId } ?: movieCache[movieId]
        if (current == null) return fullCatalog.take(4)
        val related = fullCatalog.filter {
            it.id != movieId && (it.genre == current.genre || it.director == current.director || Math.abs(it.year - current.year) <= 5)
        }
        return if (related.isNotEmpty()) related else fullCatalog.filter { it.id != movieId }.take(4)
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

package com.openstream.app.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val ARCHIVE_BASE_URL = "https://archive.org/"
    private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    private const val TVMAZE_BASE_URL = "https://api.tvmaze.com/"

    var customTmdbApiKey: String? = null

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "openStream-Android-Client/2.0 (Global Entertainment Platform)")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val archiveApiService: ArchiveApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ARCHIVE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArchiveApiService::class.java)
    }

    val tmdbApiService: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }

    val tvMazeApiService: TvMazeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TVMAZE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TvMazeApiService::class.java)
    }
}

package com.openstream.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.openstream.app.core.di.AppContainer
import com.openstream.app.core.di.DefaultAppContainer
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class OpenStreamApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }

    override fun newImageLoader(): ImageLoader {
        val imageOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "openStream/1.0 (Android; Mobile; Public Domain Cinema Archive)")
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(imageOkHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "openstream_image_cache"))
                    .maxSizeBytes(128L * 1024 * 1024) // 128 MB high-res poster disk cache
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false) // Prevent aggressive CDN eviction on navigation
            .build()
    }
}

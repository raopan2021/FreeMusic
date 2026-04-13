package com.freemusic.di

import com.freemusic.data.remote.api.ItunesApi
import com.freemusic.data.remote.api.LrclibApi
import com.freemusic.data.remote.api.MetingApi
import com.freemusic.data.remote.api.NeteaseApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val NETEASE_BASE_URL = "https://zm.wwoyun.cn/"
    private const val METING_BASE_URL = "https://api.qijieya.cn/"
    private const val LRCLIB_BASE_URL = "https://lrclib.net/"
    private const val ITUNES_BASE_URL = "https://itunes.apple.com/"

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("netease")
    fun provideNeteaseRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NETEASE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @Named("meting")
    fun provideMetingRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(METING_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideNeteaseApi(@Named("netease") retrofit: Retrofit): NeteaseApi {
        return retrofit.create(NeteaseApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMetingApi(@Named("meting") retrofit: Retrofit): MetingApi {
        return retrofit.create(MetingApi::class.java)
    }

    @Provides
    @Singleton
    @Named("lrclib")
    fun provideLrclibRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(LRCLIB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @Named("lrclib")
    fun provideLrclibApi(@Named("lrclib") retrofit: Retrofit): LrclibApi {
        return retrofit.create(LrclibApi::class.java)
    }

    @Provides
    @Singleton
    @Named("itunes")
    fun provideItunesRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideItunesApi(@Named("itunes") retrofit: Retrofit): ItunesApi {
        return retrofit.create(ItunesApi::class.java)
    }
}

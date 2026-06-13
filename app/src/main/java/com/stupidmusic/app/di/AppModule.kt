package com.stupidmusic.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stupidmusic.app.data.api.PipedApi
import com.stupidmusic.app.data.api.YoutubeApi
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
object AppModule {

    // List of Piped instances to try in order
    val PIPED_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks/",
        "https://pipedapi.moomoo.me/",
        "https://piped-api.garudalinux.org/",
        "https://api.piped.yt/",
        "https://pipedapi.in.projectsegfau.lt/"
    )

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    @Named("youtube")
    fun provideYoutubeRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    // Piped uses first instance by default; fallback handled in repository
    @Provides
    @Singleton
    @Named("piped")
    fun providePipedRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(PIPED_INSTANCES[0])
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideYoutubeApi(@Named("youtube") retrofit: Retrofit): YoutubeApi =
        retrofit.create(YoutubeApi::class.java)

    @Provides
    @Singleton
    fun providePipedApi(@Named("piped") retrofit: Retrofit): PipedApi =
        retrofit.create(PipedApi::class.java)
}

package com.stupidmusic.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stupidmusic.app.data.api.StreamApi
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

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
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

    @Provides
    @Singleton
    @Named("stream")
    fun provideStreamRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            // Will be set via BuildConfig after Railway deploy
            .baseUrl(com.stupidmusic.app.BuildConfig.STREAM_SERVER_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideYoutubeApi(@Named("youtube") retrofit: Retrofit): YoutubeApi =
        retrofit.create(YoutubeApi::class.java)

    @Provides
    @Singleton
    fun provideStreamApi(@Named("stream") retrofit: Retrofit): StreamApi =
        retrofit.create(StreamApi::class.java)
}

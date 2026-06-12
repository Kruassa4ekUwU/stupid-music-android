package com.stupidmusic.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.stupidmusic.app.data.api.InvidiousApi
import com.stupidmusic.app.data.network.InstanceManager
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // Add Android User-Agent so instances don't block us
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideInstanceManager(): InstanceManager = InstanceManager()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json, instanceManager: InstanceManager): Retrofit =
        Retrofit.Builder()
            .baseUrl(instanceManager.currentInstance)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideInvidiousApi(retrofit: Retrofit): InvidiousApi =
        retrofit.create(InvidiousApi::class.java)
}

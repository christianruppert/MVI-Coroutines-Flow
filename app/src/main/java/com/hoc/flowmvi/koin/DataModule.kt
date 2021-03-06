package com.hoc.flowmvi.koin

import com.hoc.flowmvi.BuildConfig
import com.hoc.flowmvi.data.mapper.UserDomainToUserBodyMapper
import com.hoc.flowmvi.data.mapper.UserDomainToUserResponseMapper
import com.hoc.flowmvi.data.mapper.UserResponseToUserDomainMapper
import com.hoc.flowmvi.data.remote.UserApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "BASE_URL"

val dataModule = module {
  single { UserApiService(retrofit = get()) }

  single {
    provideRetrofit(
        baseUrl = get(named(BASE_URL)),
        moshi = get(),
        client = get()
    )
  }

  single { provideMoshi() }

  single { provideOkHttpClient() }

  factory(named(BASE_URL)) { "https://mvi-coroutines-flow-server.herokuapp.com/" }

  factory { UserResponseToUserDomainMapper() }

  factory { UserDomainToUserResponseMapper() }

  factory { UserDomainToUserBodyMapper() }
}

private fun provideMoshi(): Moshi {
  return Moshi
      .Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
}

private fun provideRetrofit(baseUrl: String, moshi: Moshi, client: OkHttpClient): Retrofit {
  return Retrofit.Builder()
      .client(client)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .baseUrl(baseUrl)
      .build()
}

private fun provideOkHttpClient(): OkHttpClient {
  return OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS)
      .addInterceptor(
          HttpLoggingInterceptor()
              .apply { level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE }
      )
      .build()
}
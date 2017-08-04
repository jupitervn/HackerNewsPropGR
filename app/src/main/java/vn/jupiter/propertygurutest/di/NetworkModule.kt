package vn.jupiter.propertygurutest.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import vn.jupiter.propertygurutest.data.http.HackerNewsHttpService
import vn.jupiter.propertygurutest.di.AppConfigurationModule.Companion.BASE_URL
import vn.jupiter.propertygurutest.di.AppConfigurationModule.Companion.IS_DEBUG_MODE
import javax.inject.Named
import javax.inject.Singleton


@Module
class NetworkModule {
    @Provides
    @Singleton
    fun providesOkHttpClient(@Named(IS_DEBUG_MODE) isDebug: Boolean): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (isDebug) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(httpLoggingInterceptor)
        }
        return builder
                .build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient, @Named(BASE_URL) baseUrl: String, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
    }

    @Provides
    @Singleton
    fun providesMoshi(): Moshi {
        return Moshi.Builder()
                .build()
    }

    @Provides
    @Singleton
    fun providesHttpService(retrofit: Retrofit): HackerNewsHttpService {
        return retrofit.create(HackerNewsHttpService::class.java)
    }
}
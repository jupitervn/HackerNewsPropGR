package vn.jupiter.propertygurutest.di

import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppConfigurationModule {
    companion object {
        const val IS_DEBUG_MODE = "is_debug_mode"
        const val BASE_URL = "base_url"
    }

    @Provides
    @Singleton
    @Named(IS_DEBUG_MODE)
    fun providesAppMode() = false

    @Provides
    @Singleton
    @Named(BASE_URL)
    fun providesAppApiUrl() = "https://hacker-news.firebaseio.com/v0/"
}
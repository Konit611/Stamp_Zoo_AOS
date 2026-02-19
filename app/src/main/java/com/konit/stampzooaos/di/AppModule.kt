package com.konit.stampzooaos.di

import android.app.Application
import com.konit.stampzooaos.core.localization.LanguageStore
import com.konit.stampzooaos.data.ZooRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideZooRepository(app: Application): ZooRepository {
        return ZooRepository(app)
    }

    @Provides
    @Singleton
    fun provideLanguageStore(app: Application): LanguageStore {
        return LanguageStore(app)
    }
}

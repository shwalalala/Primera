package com.example.primera.feature.wellness.di

import com.example.primera.feature.wellness.data.repository.WellnessRepository
import com.example.primera.feature.wellness.data.repository.WellnessRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WellnessModule {

    @Binds
    @Singleton
    abstract fun bindWellnessRepository(
        wellnessRepositoryImpl: WellnessRepositoryImpl
    ): WellnessRepository
}

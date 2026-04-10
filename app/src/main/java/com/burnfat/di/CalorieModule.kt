package com.burnfat.di

import com.burnfat.domain.calculator.CalorieCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalorieModule {

    @Provides
    @Singleton
    fun provideCalorieCalculator(): CalorieCalculator {
        return CalorieCalculator()
    }
}
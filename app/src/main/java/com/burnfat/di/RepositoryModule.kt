package com.burnfat.di

import com.burnfat.data.repository.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // Repository 通过 @Inject constructor 自动提供，无需额外配置
}
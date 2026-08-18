package com.posly.app.di

import com.posly.app.data.repository.AuthRepositoryImpl
import com.posly.app.data.repository.FinanceRepositoryImpl
import com.posly.app.data.repository.OrderRepositoryImpl
import com.posly.app.data.repository.ProductRepositoryImpl
import com.posly.app.data.repository.SettingsRepositoryImpl
import com.posly.app.domain.repository.AuthRepository
import com.posly.app.domain.repository.FinanceRepository
import com.posly.app.domain.repository.OrderRepository
import com.posly.app.domain.repository.ProductRepository
import com.posly.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds @Singleton
    abstract fun bindFinanceRepository(impl: FinanceRepositoryImpl): FinanceRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

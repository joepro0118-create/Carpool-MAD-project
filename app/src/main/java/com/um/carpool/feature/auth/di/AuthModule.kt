package com.um.carpool.feature.auth.di

import com.um.carpool.feature.auth.data.AuthRepositoryImpl
import com.um.carpool.feature.auth.domain.repository.AuthRepository
import com.um.carpool.feature.auth.ui.AuthViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(androidContext()) }
    viewModelOf(::AuthViewModel)
}

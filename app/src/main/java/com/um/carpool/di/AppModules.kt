package com.um.carpool.di

import com.um.carpool.feature.auth.di.authModule
import org.koin.core.module.Module

/**
 * Central Koin module registry.
 * Each team role registers its own feature module here upon integration into `dev`.
 */
val appModules: List<Module> = listOf(
    authModule
)

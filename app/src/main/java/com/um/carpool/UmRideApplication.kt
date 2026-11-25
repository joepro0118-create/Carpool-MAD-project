package com.um.carpool

import android.app.Application
import com.um.carpool.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class UmRideApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@UmRideApplication)
            modules(appModules)
        }
    }
}

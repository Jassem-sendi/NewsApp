package com.example.news

import android.app.Application
import com.example.news.core.di.coreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(coreModule)
        }
    }
}
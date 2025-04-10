package com.dragon.wheels

import android.app.Application
import com.dragon.wheels.tools.NetworkStateManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkStateManager.initialize(this)
    }

}
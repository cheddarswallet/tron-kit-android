package io.cheddarswallet.tronkit.sample

import android.app.Application
import io.cheddarswallet.tronkit.TronKit
import java.util.logging.Logger

class App : Application() {

    private val logger = Logger.getLogger("App")

    override fun onCreate() {
        super.onCreate()

        TronKit.init()

        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }

}

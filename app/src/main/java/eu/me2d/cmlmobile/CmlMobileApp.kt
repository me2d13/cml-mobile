package eu.me2d.cmlmobile

import android.app.Application
import timber.log.Timber
import eu.me2d.cmlmobile.util.InMemoryLogStore
import eu.me2d.cmlmobile.util.TimberMemoryTree

object LogMemoryStore {
    val instance = InMemoryLogStore()
}

class CmlMobileApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
        Timber.plant(Timber.DebugTree())
        Timber.plant(TimberMemoryTree(LogMemoryStore.instance))
        Timber.d("CmlMobileApp started")
    }

    companion object {
        lateinit var appModule: AppModule
    }
}

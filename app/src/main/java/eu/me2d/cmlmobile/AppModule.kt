package eu.me2d.cmlmobile

import android.content.Context
import eu.me2d.cmlmobile.service.ApiService
import eu.me2d.cmlmobile.service.CryptoService
import eu.me2d.cmlmobile.service.HistoryService
import eu.me2d.cmlmobile.service.NetworkService
import eu.me2d.cmlmobile.service.StorageService

interface AppModule {
    val storageService: StorageService
    val historyService: HistoryService
    val apiService: ApiService
    val networkService: NetworkService
    val cryptoService: CryptoService
}

class AppModuleImpl(
    private val appContext: Context
) : AppModule {
    override val storageService: StorageService by lazy {
        StorageService(appContext)
    }
    override val historyService: HistoryService by lazy {
        HistoryService()
    }
    override val apiService: ApiService by lazy {
        ApiService(networkService)
    }
    override val networkService: NetworkService by lazy {
        NetworkService(appContext)
    }
    override val cryptoService: CryptoService by lazy {
        CryptoService()
    }
}
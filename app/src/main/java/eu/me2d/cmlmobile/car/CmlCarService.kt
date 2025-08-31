package eu.me2d.cmlmobile.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import timber.log.Timber

class CmlCarService : CarAppService() {
    override fun onCreate() {
        super.onCreate()
        Timber.d("CmlCarService: onCreate() called")
    }

    override fun createHostValidator(): HostValidator {
        Timber.d("CmlCarService: createHostValidator() called")
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        Timber.d("CmlCarService: onCreateSession() called")
        return try {
            CmlCarSession()
        } catch (e: Exception) {
            Timber.e(e, "CmlCarService: Failed to create session")
            throw e
        }
    }
}
package eu.me2d.cmlmobile.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session
import timber.log.Timber

class CmlCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        Timber.d("CmlCarSession: onCreateScreen() called")
        return try {
            CommandListScreen(carContext)
        } catch (e: Exception) {
            Timber.e(e, "CmlCarSession: Failed to create screen")
            throw e
        }
    }
}
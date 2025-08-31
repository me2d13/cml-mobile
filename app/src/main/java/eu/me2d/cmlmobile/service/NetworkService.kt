package eu.me2d.cmlmobile.service

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NetworkService(private val context: Context) {
    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.ACCESS_NETWORK_STATE])
    fun getCurrentWifiName(): String? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (isWifi) {
            val info = wifiManager.connectionInfo
            val ssid = info.ssid
            return ssid?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
                ?.removePrefix("\"")  // Remove leading quote
                ?.removeSuffix("\"")  // Remove trailing quote
        }
        return null
    }

    fun requestLocationPermissionIfNeeded(activity: Activity) {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}

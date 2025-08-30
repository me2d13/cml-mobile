package eu.me2d.cmlmobile.service

import android.content.Context
import android.content.SharedPreferences
import eu.me2d.cmlmobile.state.GlobalState
import eu.me2d.cmlmobile.state.StateSettings
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class MigrationService(private val context: Context) {

    companion object {
        // Old app SharedPreferences keys
        private const val KEY_SERVER_URL = "serverUrl"
        private const val KEY_SENT_DATE = "sentDate"
        private const val KEY_PRIVATE_KEY = "privateKey"

        // Default SharedPreferences name (used by PreferenceManager.getDefaultSharedPreferences)
        private const val DEFAULT_PREFS_NAME = "eu.me2d.cmlmobile_preferences"
    }

    private val oldPrefs: SharedPreferences =
        context.getSharedPreferences(DEFAULT_PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if old app data exists that can be migrated
     */
    fun hasOldAppData(): Boolean {
        val hasOldData = oldPrefs.contains(KEY_SERVER_URL) ||
                oldPrefs.contains(KEY_PRIVATE_KEY) ||
                oldPrefs.contains(KEY_SENT_DATE)

        if (hasOldData) {
            Timber.i("Old app data found, available for migration")
        } else {
            Timber.d("No old app data found")
        }

        return hasOldData
    }

    /**
     * Migrate data from old app format to new app format
     * Only called when no new app state exists yet
     */
    fun migrateFromOldApp(): GlobalState {
        Timber.i("Migrating data from old app (first launch of new version)...")

        try {
            // Extract old data
            val oldServerUrl = oldPrefs.getString(KEY_SERVER_URL, null)
            val oldSentDateString = oldPrefs.getString(KEY_SENT_DATE, null)
            val oldPrivateKey = oldPrefs.getString(KEY_PRIVATE_KEY, null)

            Timber.d("Migrating old data - URL: ${oldServerUrl != null}, Date: ${oldSentDateString != null}, Key: ${oldPrivateKey != null}")

            // Convert old data to new format
            val newSettings = StateSettings(
                apiUrl = oldServerUrl ?: "",
                myId = "", // Not available in old app, leave empty
                wifiPattern = "", // Not available in old app, leave empty
                wifiUrl = "" // Not available in old app, leave empty
            )

            // Convert LocalDateTime string to Instant
            val registrationTimestamp =
                if (oldSentDateString != null && oldSentDateString.isNotEmpty()) {
                    try {
                        val localDateTime = LocalDateTime.parse(oldSentDateString)
                        localDateTime.toInstant(ZoneOffset.UTC)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to parse old sent date: $oldSentDateString")
                    null
                }
            } else {
                null
            }

            // Create new state with migrated data
            val migratedState = GlobalState(
                settings = newSettings,
                privateKeyEncoded = oldPrivateKey ?: "",
                registrationTimestamp = registrationTimestamp
            )

            Timber.i(
                "Migration completed successfully - migrated ${
                    listOfNotNull(
                        oldServerUrl,
                        oldPrivateKey,
                        registrationTimestamp
                    ).size
                } fields"
            )
            return migratedState

        } catch (e: Exception) {
            Timber.e(e, "Migration failed, returning default state")
            return GlobalState()
        }
    }

    /**
     * Get summary of what will be migrated (for logging/debugging)
     */
    fun getMigrationSummary(): String {
        val serverUrl = oldPrefs.getString(KEY_SERVER_URL, null)
        val sentDate = oldPrefs.getString(KEY_SENT_DATE, null)
        val privateKey = oldPrefs.getString(KEY_PRIVATE_KEY, null)

        return buildString {
            appendLine("Migration Summary:")
            appendLine("- Server URL: ${if (serverUrl != null) "✓ Found" else "✗ Not found"}")
            appendLine("- Registration Date: ${if (sentDate != null) "✓ Found ($sentDate)" else "✗ Not found"}")
            appendLine("- Private Key: ${if (privateKey != null) "✓ Found" else "✗ Not found"}")
        }
    }
}
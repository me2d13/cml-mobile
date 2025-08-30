package eu.me2d.cmlmobile.service

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import eu.me2d.cmlmobile.state.GlobalState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import timber.log.Timber
import java.time.Instant

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

class StorageService(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("global_state_prefs", Context.MODE_PRIVATE)
    private val key = "global_settings"

    private val json = Json {
        serializersModule = SerializersModule {
            contextual(InstantSerializer)
        }
        ignoreUnknownKeys = true
    }

    fun saveState(state: GlobalState) {
        val jsonString = json.encodeToString(state)
        prefs.edit { putString(key, jsonString) }
        Timber.Forest.d("Saved global state")
    }

    /**
     * Load state with migration support from old app version
     * Only performs migration if no new state exists (first launch)
     */
    fun loadStateWithMigration(migrationService: MigrationService): GlobalState {
        val jsonString = prefs.getString(key, null)

        return if (jsonString != null) {
            // New state exists, load it normally (no migration needed)
            try {
                val state: GlobalState = json.decodeFromString(GlobalState.serializer(), jsonString)
                Timber.Forest.d("Loaded existing global state")
                state
            } catch (e: Exception) {
                Timber.Forest.e(e, "Failed to decode global state, using default")
                GlobalState()
            }
        } else {
            // No new state exists - this is first launch of new app version
            if (migrationService.hasOldAppData()) {
                Timber.Forest.i("First launch detected, attempting migration from old app")
                Timber.Forest.d(migrationService.getMigrationSummary())

                // Perform migration and save the result
                val migratedState = migrationService.migrateFromOldApp()
                saveState(migratedState)

                Timber.Forest.i("Migration completed and state saved")
                migratedState
            } else {
                // No old data either, use default state
                Timber.Forest.d("First launch, no old data to migrate, using default state")
                GlobalState()
            }
        }
    }
}
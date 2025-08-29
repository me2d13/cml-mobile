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

    fun loadState(): GlobalState {
        val jsonString = prefs.getString(key, null) ?: return GlobalState()
        return try {
            val state: GlobalState = json.decodeFromString(jsonString)
            Timber.Forest.d("Loaded global state")
            state
        } catch (e: Exception) {
            Timber.Forest.e(e, "Failed to decode global state")
            GlobalState()
        }
    }
}
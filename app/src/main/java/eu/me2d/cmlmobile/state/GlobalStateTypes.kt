package eu.me2d.cmlmobile.state

import java.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// Type aliases

typealias History = MutableMap<HistoryDate, MutableMap<CommandNo, Int>>
typealias HistoryDate = String
typealias CommandNo = Int

@Serializable
enum class ApiCallStatus {
    IDLE,
    IN_PROGRESS,
    SUCCESS,
    ERROR
}

@Serializable
data class ApiState(
    val status: ApiCallStatus = ApiCallStatus.IDLE,
    val statusMessage: String = "",
    val lastCallType: String = ""
)

@Serializable
data class StateSettings(
    val apiUrl: String = "",
    val myId: String = "",
    val wifiPattern: String = "",
    val wifiUrl: String = ""
)

@Serializable
data class Command(
    val number: Int,
    val name: String,
)

@Serializable
data class GlobalState(
    val settings: StateSettings = StateSettings(),
    val history: History = mutableMapOf(),
    val commands: List<Command> = listOf(),
    val currentPage: Int = 0,
    val registrationTimestamp: @Contextual Instant? = null,
    val privateKeyEncoded: String = "",
    @Transient val apiState: ApiState = ApiState(),
)
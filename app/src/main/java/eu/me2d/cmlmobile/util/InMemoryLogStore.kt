package eu.me2d.cmlmobile.util

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

// Data class for a log entry
data class LogEntry(
    val timestamp: Long,
    val level: String,
    val message: String
)

// In-memory store for last 100 logs
class InMemoryLogStore(private val maxLogs: Int = 100) {
    private val logs = ArrayDeque<LogEntry>()
    private val lock = ReentrantLock()

    fun addLog(entry: LogEntry) {
        lock.withLock {
            if (logs.size == maxLogs) {
                logs.removeFirst()
            }
            logs.addLast(entry)
        }
    }

    fun getLogs(): List<LogEntry> = lock.withLock {
        logs.toList()
    }
}

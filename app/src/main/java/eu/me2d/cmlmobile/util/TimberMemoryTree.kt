package eu.me2d.cmlmobile.util

import timber.log.Timber

class TimberMemoryTree(private val logStore: InMemoryLogStore) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val level = when (priority) {
            android.util.Log.VERBOSE -> "VERBOSE"
            android.util.Log.DEBUG -> "DEBUG"
            android.util.Log.INFO -> "INFO"
            android.util.Log.WARN -> "WARN"
            android.util.Log.ERROR -> "ERROR"
            android.util.Log.ASSERT -> "ASSERT"
            else -> "OTHER"
        }
        val msg = if (tag != null) "[$tag] $message" else message
        val fullMsg = if (t != null) msg + "\n" + android.util.Log.getStackTraceString(t) else msg
        logStore.addLog(
            LogEntry(
                timestamp = System.currentTimeMillis(),
                level = level,
                message = fullMsg
            )
        )
    }
}

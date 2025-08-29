package eu.me2d.cmlmobile.service

import eu.me2d.cmlmobile.state.Command
import eu.me2d.cmlmobile.state.History
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryService {
    private val daysToKeep = 10
    
    fun sortedCommands(commands: List<Command>, history: History) : List<Command> {
        // Sum executions for each command number
        val commandExecCounts = mutableMapOf<Int, Int>()
        for ((_, dailyExecMap) in history) {
            for ((cmdNo, count) in dailyExecMap) {
                commandExecCounts[cmdNo] = commandExecCounts.getOrDefault(cmdNo, 0) + count
            }
        }
        // Sort commands by their summed count descending
        val sorted = commands.sortedByDescending { commandExecCounts.getOrDefault(it.number, 0) }
        return sorted
    }

    fun recordCommandExecution(commandNumber: Int, history: History): History {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        // Mutate a copy to avoid side effects (not strictly necessary for MutableMap, but for safety)
        val newHistory = history.toMutableMap()
        val dailyMap = newHistory.getOrPut(today) { mutableMapOf() }
        dailyMap[commandNumber] = dailyMap.getOrDefault(commandNumber, 0) + 1

        // Enforce daysToKeep: drop oldest if exceeded
        if (newHistory.size > daysToKeep) {
            val sortedKeys = newHistory.keys.sorted()
            val keysToDrop = sortedKeys.take(newHistory.size - daysToKeep)
            for (key in keysToDrop) {
                newHistory.remove(key)
            }
        }
        return newHistory
    }
}
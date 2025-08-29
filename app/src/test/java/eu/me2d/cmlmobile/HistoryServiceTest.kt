package eu.me2d.cmlmobile

import eu.me2d.cmlmobile.service.HistoryService
import eu.me2d.cmlmobile.state.Command
import eu.me2d.cmlmobile.state.History
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryServiceTest {
    private val historyService = HistoryService()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Test
    fun test_recordCommandExecution_trimsOldDays() {
        val history: History = mutableMapOf()
        // Simulate 15 days with executions for command 1 and 2
        repeat(15) { dayIdx ->
            val day = "2023-10-%02d".format(dayIdx + 1)
            history[day] = mutableMapOf(1 to dayIdx + 3, 2 to dayIdx)
        }
        val result = historyService.recordCommandExecution(1, history)
        // Should keep only the last 10 days
        assertEquals(10, result.size)
        // Last day should be today, with command 1 incremented
        val today = dateFormat.format(Date())
        assertEquals(result[today]?.get(1), 1)
    }

    @Test
    fun test_sortedCommands_mostExecutedFirst() {
        val commands = listOf(Command(1, "One"), Command(2, "Two"), Command(3, "Three"))
        val history: History = mutableMapOf(
            "2023-09-20" to mutableMapOf(1 to 5, 2 to 10, 3 to 1),
            "2023-09-21" to mutableMapOf(1 to 4, 2 to 2)
        )
        val sortedList = historyService.sortedCommands(commands, history)
        // Command 2: 10 + 2 = 12, Command 1: 5 + 4 = 9, Command 3: 1
        assertEquals(listOf(2, 1, 3), sortedList.map { it.number })
    }
}

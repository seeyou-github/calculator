package net.youapps.calcyou.persistence

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.you_apps.calcyou.CalculationHistory

class CalculationHistoryRepository(val context: Context) {
    val dataStore get() = context.calculationHistoryDataStore

    val historyFlow: Flow<List<String>> = dataStore.data.map { it.expressionsList }

    suspend fun addExpression(expression: String) {
        dataStore.updateData { data ->
            val builder = data.toBuilder()
            builder.addExpressions(expression)

            // keep only the most recent calculations
            while (builder.expressionsCount > MAX_HISTORY_SIZE) {
                builder.removeExpressions(0)
            }

            builder.build()
        }
    }

    suspend fun clear() {
        dataStore.updateData { CalculationHistory.getDefaultInstance() }
    }

    companion object {
        const val MAX_HISTORY_SIZE = 100
    }
}

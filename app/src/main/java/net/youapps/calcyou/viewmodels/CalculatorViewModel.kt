package net.youapps.calcyou.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.youapps.calcyou.CalculatorApplication
import net.youapps.calcyou.data.CalculatorEvent
import net.youapps.calcyou.data.EventHandler
import net.youapps.calcyou.data.evaluator.TrigonometricMode
import net.youapps.calcyou.persistence.CalculationHistoryRepository

class CalculatorViewModel(context: Context) : ViewModel() {

    private val eventHandler =
        EventHandler(context, onUpdateHistory = this::onUpdateHistory)
    var displayText by mutableStateOf(TextFieldValue(""))
    var resultPreviewText by mutableStateOf<String?>(null)

    /**
     * Whether the display currently shows the evaluated result of the previous calculation.
     */
    var isShowingResult by mutableStateOf(false)
        private set

    /**
     * The expression that was evaluated the last time the equals button was pressed.
     */
    var lastExpression by mutableStateOf("")
        private set

    var history: MutableList<String> = mutableStateListOf()

    val trigonometricMode = mutableStateOf(TrigonometricMode.RADIAN)

    private val historyRepository = CalculationHistoryRepository(context)

    init {
        // restore the calculation history persisted from previous sessions
        viewModelScope.launch {
            val persistedHistory = historyRepository.historyFlow.first()
                .takeLast(CalculationHistoryRepository.MAX_HISTORY_SIZE)
            // guard against racing with a history entry added before the load finished
            if (history.isEmpty()) {
                history.addAll(persistedHistory)
            }
        }
    }

    fun setExpression(text: String) {
        displayText = TextFieldValue(text)
        isShowingResult = false
    }

    fun onEvent(event: CalculatorEvent) {
        // pressing equals on a plain number (or nothing) is not a calculation:
        // don't evaluate, record it or change the display
        if (event == CalculatorEvent.Evaluate &&
            !eventHandler.isCalculableExpression(displayText.text)
        ) {
            return
        }
        if (event == CalculatorEvent.Evaluate) {
            lastExpression = displayText.text
        }
        isShowingResult = event == CalculatorEvent.Evaluate
        displayText = eventHandler.processEvent(event, displayText, trigonometricMode)
        if (displayText.text.isBlank() || event == CalculatorEvent.Evaluate) {
            resultPreviewText = null
        }
        // calculate a preview of the expression result on the fly
        else {
            runCatching {
                eventHandler.evaluateResult(displayText.text, trigonometricMode)?.let {
                    resultPreviewText = it
                }
            }
        }
    }

    fun clearHistory() {
        history.clear()
        viewModelScope.launch {
            historyRepository.clear()
        }
    }

    private fun onUpdateHistory(item: String) {
        // keep only the most recent calculations
        if (history.size >= CalculationHistoryRepository.MAX_HISTORY_SIZE) {
            history.removeAt(0)
        }
        history.add(item)
        viewModelScope.launch {
            historyRepository.addExpression(item)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as CalculatorApplication
                CalculatorViewModel(application)
            }
        }
    }
}
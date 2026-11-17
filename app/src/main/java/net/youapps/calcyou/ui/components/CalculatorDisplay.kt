package net.youapps.calcyou.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.youapps.calcyou.viewmodels.CalculatorViewModel

@Composable
fun ColumnScope.CalculatorDisplay(
    calculatorViewModel: CalculatorViewModel,
    primaryTextStyle: TextStyle = MaterialTheme.typography.displayMedium
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1.0f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
    ) {
        // expression area: a fixed two-line viewport over the calculation expression.
        // older lines scroll out of view and can be revealed by swiping up and down
        val expressionText = if (calculatorViewModel.isShowingResult) {
            calculatorViewModel.lastExpression
        } else {
            calculatorViewModel.displayText.text
        }
        val expressionStyle = primaryTextStyle.copy(
            lineHeight = primaryTextStyle.fontSize * 1.3f
        )
        val expressionLineHeight = with(LocalDensity.current) { expressionStyle.lineHeight.toDp() }
        val expressionScrollState = rememberScrollState()
        // keep the newest lines in view while the expression grows
        LaunchedEffect(expressionText) {
            expressionScrollState.scrollTo(expressionScrollState.maxValue)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(expressionLineHeight * 2)
                .verticalScroll(expressionScrollState),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = expressionText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.End,
                style = expressionStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // result area: always shows the result of the current expression
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(50.dp)),
            contentAlignment = Alignment.Center
        ) {
            val resultText = if (calculatorViewModel.isShowingResult) {
                calculatorViewModel.displayText.text
            } else {
                calculatorViewModel.resultPreviewText
            }
            if (!resultText.isNullOrEmpty()) {
                // shrink the font for long results so they still fit into the area
                val resultScale = if (resultText.length <= 12) 1.5f else 1.0f
                Text(
                    text = resultText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.End,
                    style = primaryTextStyle.copy(fontSize = primaryTextStyle.fontSize * resultScale),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

}

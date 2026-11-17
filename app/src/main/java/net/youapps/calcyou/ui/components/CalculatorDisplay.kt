package net.youapps.calcyou.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.awaitCancellation
import net.youapps.calcyou.viewmodels.CalculatorViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.CalculatorDisplay(
    calculatorViewModel: CalculatorViewModel,
    primaryTextStyle: TextStyle = MaterialTheme.typography.displayMedium,
    secondaryTextStyle: TextStyle = MaterialTheme.typography.displaySmall,
    tertiaryTextStyle: TextStyle = MaterialTheme.typography.titleLarge
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1.0f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
    ) {
        // the expression of the last calculation, shown above its result
        if (calculatorViewModel.isShowingResult && calculatorViewModel.lastExpression.isNotBlank()) {
            Text(
                text = calculatorViewModel.lastExpression,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                textAlign = TextAlign.End,
                style = secondaryTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        InterceptPlatformTextInput(
            // prevent the keyboard from showing up when clicking the calculator input field
            // https://stackoverflow.com/questions/63887321/prevent-the-keyboard-from-appearing-in-a-jetpack-compose-app
            interceptor = { _, _ ->
                awaitCancellation()
            }
        ) {
            BasicTextField(
                value = calculatorViewModel.displayText,
                onValueChange = {
                    calculatorViewModel.displayText = it
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(50.dp))
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                maxLines = 1,
                // enlarge the font while the evaluated result of the previous calculation is shown
                textStyle = (if (calculatorViewModel.isShowingResult) {
                    primaryTextStyle.copy(fontSize = primaryTextStyle.fontSize * 1.5f)
                } else {
                    primaryTextStyle
                }).plus(
                    TextStyle(
                        textAlign = TextAlign.Right,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            )
        }

        calculatorViewModel.resultPreviewText?.let { previewText ->
            if (previewText != calculatorViewModel.displayText.text) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(20.dp))
                        .padding(vertical = 4.dp, horizontal = 16.dp),
                    text = previewText,
                    textAlign = TextAlign.End,
                    style = tertiaryTextStyle,
                )
            }
        }
    }

}

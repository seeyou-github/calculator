package net.youapps.calcyou.ui

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import net.youapps.calcyou.AppNavHost
import net.youapps.calcyou.Destination
import net.youapps.calcyou.R
import net.youapps.calcyou.ui.components.NavDrawerContent
import net.youapps.calcyou.ui.components.NavRailContent
import net.youapps.calcyou.viewmodels.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentDestination by remember {
        mutableStateOf<Destination>(Destination.Calculator)
    }
    // a single instance shared with CalculatorScreen so the history sheet sees the same state
    val calculatorViewModel: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory)
    var showHistorySheet by remember { mutableStateOf(false) }
    val orientation = LocalConfiguration.current.orientation
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || (orientation == ORIENTATION_LANDSCAPE),
        drawerContent = {
            NavDrawerContent(currentDestination = currentDestination, onDestinationSelected = {
                scope.launch {
                    drawerState.close()
                }
                navController.popBackStack()
                navController.navigate(it.route)
                currentDestination = it

            })
        }
    ) {
        Scaffold(topBar = {
            if (orientation == ORIENTATION_PORTRAIT) {
                CenterAlignedTopAppBar(title = {
                    Text(
                        text = stringResource(
                            id = R.string.app_name
                        )
                    )
                }, navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
                    }
                }, actions = {
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(imageVector = Icons.Rounded.History, contentDescription = null)
                    }
                })
            }
        }) { paddingValues ->
            Row(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues)
                    .padding(paddingValues)
            ) {
                if (orientation == ORIENTATION_LANDSCAPE) {
                    NavigationRail {
                        NavRailContent(currentDestination = currentDestination) {
                            navController.popBackStack()
                            navController.navigate(it.route)
                            currentDestination = it
                        }
                    }
                }
                AppNavHost(
                    modifier = Modifier.weight(1f),
                    navHostController = navController,
                    calculatorViewModel = calculatorViewModel
                )
            }

        }

        if (showHistorySheet) {
            ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
                val history = calculatorViewModel.history.filter { it.isNotBlank() }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // take up 60% of the screen height, the LazyColumn inside makes it scrollable
                        .height((LocalConfiguration.current.screenHeightDp * 0.6f).dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.calculation_history),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { calculatorViewModel.clearHistory() }) {
                            Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                        }
                    }
                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(id = R.string.no_history))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // newest first
                            items(history.asReversed()) { expression ->
                                Text(
                                    text = expression,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            calculatorViewModel.setExpression(expression)
                                            showHistorySheet = false
                                            if (currentDestination != Destination.Calculator) {
                                                navController.popBackStack()
                                                navController.navigate(Destination.Calculator.route)
                                                currentDestination = Destination.Calculator
                                            }
                                        }
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.titleLarge,
                                    // show at most two lines, hide any overflow
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

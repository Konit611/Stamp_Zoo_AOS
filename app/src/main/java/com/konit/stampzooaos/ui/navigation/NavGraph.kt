package com.konit.stampzooaos.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.konit.stampzooaos.R
import com.konit.stampzooaos.feature.bingo.AppInfoScreen
import com.konit.stampzooaos.feature.bingo.BingoDetailScreen
import com.konit.stampzooaos.feature.bingo.BingoHomeScreen
import com.konit.stampzooaos.feature.explorer.ExplorerAnimalsScreen
import com.konit.stampzooaos.feature.explorer.ExplorerDetailScreen
import com.konit.stampzooaos.feature.explorer.ExplorerScreen
import com.konit.stampzooaos.feature.explorer.ExplorerViewModel
import com.konit.stampzooaos.feature.fieldguide.FieldGuideViewModel
import com.konit.stampzooaos.feature.fieldguide.FieldGuideDetail
import com.konit.stampzooaos.feature.fieldguide.FieldGuideList
import com.konit.stampzooaos.feature.scanner.ScannerScreen
import com.konit.stampzooaos.feature.scanner.ScannerViewModel
import com.konit.stampzooaos.feature.settings.LanguageSelectionScreen
import com.konit.stampzooaos.feature.settings.SettingsScreen
import com.konit.stampzooaos.ui.theme.ZooPointBlack
import com.konit.stampzooaos.ui.theme.ZooPopGreen
import com.konit.stampzooaos.ui.theme.ZooWhite

@Composable
fun RootNavHost() {
    val navController = rememberNavController()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BingoHomeRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable<BingoHomeRoute> {
                BingoHomeScreen(
                    onQRClick = { navController.navigate(ScannerRoute) },
                    onSettingsClick = { navController.navigate(SettingsRoute) },
                    onAppInfoClick = { navController.navigate(AppInfoRoute) },
                    onDetailClick = { navController.navigate(BingoDetailRoute) }
                )
            }
            composable<ExplorerRoute> {
                ExplorerScreen(navController = navController)
            }
            composable<ExplorerDetailRoute> { backStack ->
                val route = backStack.toRoute<ExplorerDetailRoute>()
                val vm = hiltViewModel<ExplorerViewModel>()
                val list by vm.facilities.collectAsState()
                val facility = list.firstOrNull { it.id == route.facilityId }
                if (facility != null) {
                    ExplorerDetailScreen(
                        facility = facility,
                        onBackClick = { navController.popBackStack() },
                        onAnimalsClick = { navController.navigate(ExplorerAnimalsRoute(facility.id)) }
                    )
                } else {
                    PlaceholderScreen(label = "Facility not found")
                }
            }
            composable<ExplorerAnimalsRoute> { backStack ->
                val route = backStack.toRoute<ExplorerAnimalsRoute>()
                val vm = hiltViewModel<ExplorerViewModel>()
                val list by vm.facilities.collectAsState()
                val facility = list.firstOrNull { it.id == route.facilityId }
                if (facility != null) {
                    ExplorerAnimalsScreen(
                        facility = facility,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    PlaceholderScreen(label = "Facility not found")
                }
            }
            composable<FieldGuideRoute> {
                FieldGuideList(onClick = { animal ->
                    navController.navigate(FieldGuideDetailRoute(animal.id))
                })
            }
            composable<FieldGuideDetailRoute> { backStack ->
                val route = backStack.toRoute<FieldGuideDetailRoute>()
                val vm = hiltViewModel<FieldGuideViewModel>()
                val collected by vm.collectedAnimals.collectAsState()
                val zooData = vm.allAnimals
                val animal = zooData.firstOrNull { it.id == route.animalId }
                    ?: collected.firstOrNull { it.id == route.animalId }

                if (animal != null) {
                    FieldGuideDetail(animal = animal, onBackClick = { navController.popBackStack() })
                } else {
                    PlaceholderScreen(label = "Animal not found: ${route.animalId}")
                }
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLanguageClick = { navController.navigate(LanguageSelectionRoute) },
                    onAppInfoClick = { navController.navigate(AppInfoRoute) }
                )
            }
            composable<LanguageSelectionRoute> {
                LanguageSelectionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<AppInfoRoute> {
                AppInfoScreen(onBackClick = { navController.popBackStack() })
            }
            composable<BingoDetailRoute> {
                BingoDetailScreen(onBackClick = { navController.popBackStack() })
            }
            composable<ScannerRoute> {
                val context = LocalContext.current
                val scannerVm = hiltViewModel<ScannerViewModel>()

                val successMsg = stringResource(id = R.string.stamp_collected_success)
                val alreadyCollectedMsg = stringResource(id = R.string.stamp_already_collected)
                val notFoundMsg = stringResource(id = R.string.animal_not_found)
                val invalidMsg = stringResource(id = R.string.toast_invalid_qr)
                val eventComingMsg = stringResource(id = R.string.toast_event_coming)

                fun toast(msg: String) {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }

                ScannerScreen(
                    onResult = { result ->
                        scannerVm.handleQrResult(
                            rawResult = result,
                            onStampSuccess = {
                                toast(successMsg)
                                navController.navigate(BingoHomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            },
                            onAlreadyCollected = {
                                toast(alreadyCollectedMsg)
                                navController.navigate(BingoHomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            },
                            onAnimalNotFound = {
                                toast(notFoundMsg)
                                navController.navigate(BingoHomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            },
                            onFacilityQr = { facilityId ->
                                navController.navigate(ExplorerDetailRoute(facilityId))
                            },
                            onBingoQr = {
                                navController.navigate(BingoHomeRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            },
                            onEventQr = {
                                toast(eventComingMsg)
                                navController.popBackStack()
                            },
                            onInvalidQr = {
                                toast(invalidMsg)
                                navController.popBackStack()
                            }
                        )
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

private data class BottomItem(
    val route: Any,
    val icon: ImageVector,
    val labelResId: Int
)

@Composable
private fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomItem(BingoHomeRoute, Icons.Filled.Home, R.string.tab_bingo),
        BottomItem(ExplorerRoute, Icons.Filled.Search, R.string.tab_explorer),
        BottomItem(FieldGuideRoute, Icons.Filled.Info, R.string.tab_fieldguide)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar(
        containerColor = ZooPointBlack,
        contentColor = ZooWhite
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isSelected(item.route),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(text = stringResource(id = item.labelResId)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ZooPopGreen,
                    selectedTextColor = ZooPopGreen,
                    unselectedIconColor = ZooWhite,
                    unselectedTextColor = ZooWhite,
                    indicatorColor = ZooPointBlack
                )
            )
        }
    }
}

private fun NavDestination?.isSelected(route: Any): Boolean {
    return this?.hierarchy?.any { dest ->
        when (route) {
            is BingoHomeRoute -> dest.hasRoute<BingoHomeRoute>()
            is ExplorerRoute -> dest.hasRoute<ExplorerRoute>()
            is FieldGuideRoute -> dest.hasRoute<FieldGuideRoute>()
            else -> false
        }
    } == true
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}

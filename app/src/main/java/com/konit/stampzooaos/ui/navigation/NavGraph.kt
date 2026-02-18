package com.konit.stampzooaos.ui.navigation

// 👇 FIX: All imports are correct and organized.
import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.konit.stampzooaos.R
import com.konit.stampzooaos.core.qr.QRPayload
import com.konit.stampzooaos.core.qr.QRParser
import com.konit.stampzooaos.data.ZooRepository
import com.konit.stampzooaos.feature.bingo.BingoHomeScreen
import com.konit.stampzooaos.feature.explorer.ExplorerDetailScreen
import com.konit.stampzooaos.feature.explorer.ExplorerScreen
import com.konit.stampzooaos.feature.explorer.ExplorerViewModel
import com.konit.stampzooaos.feature.fieldguide.FieldGuideDetail
import com.konit.stampzooaos.feature.fieldguide.FieldGuideList
import com.konit.stampzooaos.feature.scanner.ScannerScreen
import com.konit.stampzooaos.feature.settings.SettingsScreen

@Composable
fun RootNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ""
    
    // 세부 화면 라우트들 (하단 padding을 적용하지 않을 화면)
    val detailRoutes = listOf(
        "bingoDetail",
        "appInfo",
        "settings",
        "languageSelection",
        "scanner",
        "explorer/detail/",
        "explorer/animals/",
        "fieldGuide/detail/"
    )
    
    val isDetailScreen = detailRoutes.any { route ->
        currentRoute == route.trimEnd('/') || currentRoute.startsWith(route)
    }
    
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // 탭바는 항상 표시
            BottomBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.BingoHome.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destinations.BingoHome.route) { 
                BingoHomeScreen(
                    onQRClick = { navController.navigate("scanner") },
                    onSettingsClick = { navController.navigate("settings") },
                    onAppInfoClick = { navController.navigate("appInfo") },
                    onDetailClick = { navController.navigate("bingoDetail") }
                )
            }
            composable(Destinations.Explorer.route) { ExplorerScreen(navController = navController) }
            composable(
                route = "explorer/detail/{facilityId}",
                arguments = listOf(navArgument("facilityId") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("facilityId")
                val vm = viewModel<ExplorerViewModel>()
                val list by vm.facilities.collectAsState()
                val facility = list.firstOrNull { it.id == id }
                if (facility != null) {
                    ExplorerDetailScreen(
                        facility = facility,
                        onBackClick = { navController.popBackStack() },
                        onAnimalsClick = { navController.navigate("explorer/animals/${facility.id}") }
                    )
                } else {
                    PlaceholderScreen(label = "Facility not found")
                }
            }
            composable(
                route = "explorer/animals/{facilityId}",
                arguments = listOf(navArgument("facilityId") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("facilityId")
                val vm = viewModel<ExplorerViewModel>()
                val list by vm.facilities.collectAsState()
                val facility = list.firstOrNull { it.id == id }
                if (facility != null) {
                    com.konit.stampzooaos.feature.explorer.ExplorerAnimalsScreen(
                        facility = facility,
                        onBackClick = { navController.popBackStack() }
                    )
                } else {
                    PlaceholderScreen(label = "Facility not found")
                }
            }
            composable(Destinations.FieldGuide.route) {
                FieldGuideList(onClick = { animal ->
                    navController.navigate("fieldGuide/detail/${animal.id}")
                })
            }
            composable(
                route = "fieldGuide/detail/{animalId}",
                arguments = listOf(navArgument("animalId") { type = NavType.StringType })
            ) { backStack ->
                val id = backStack.arguments?.getString("animalId")
                
                // Repository에서 동물 로드 시도
                val context = LocalContext.current
                val app = context.applicationContext as Application
                val repo = remember { ZooRepository(app) }
                val data = repo.loadZooData()
                val animal = data.animals.firstOrNull { it.id == id }
                
                if (animal != null) {
                    FieldGuideDetail(animal = animal, onBackClick = { navController.popBackStack() })
                } else {
                    PlaceholderScreen(label = "Animal not found: $id")
                }
            }
            composable("settings") { 
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLanguageClick = { navController.navigate("languageSelection") },
                    onAppInfoClick = { navController.navigate("appInfo") }
                )
            }
            composable("languageSelection") { 
                com.konit.stampzooaos.feature.settings.LanguageSelectionScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("appInfo") { com.konit.stampzooaos.feature.bingo.AppInfoScreen(onBackClick = { navController.popBackStack() }) }
            composable("bingoDetail") { com.konit.stampzooaos.feature.bingo.BingoDetailScreen(onBackClick = { navController.popBackStack() }) }
            composable("scanner") {
                val context = LocalContext.current
                val app = context.applicationContext as Application
                val repo = remember { ZooRepository(app) }
                val lifecycleOwner = LocalLifecycleOwner.current
                
                // 메시지 리소스 미리 로드
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
                    val parsed = QRParser.parse(result)
                    when (parsed) {
                        is QRPayload.Data -> {
                            when (parsed.type) {
                                QRPayload.Type.ANIMAL -> {
                                    // 동물 스탬프 수집
                                    val zooData = repo.loadZooData()
                                    val animal = zooData.animals.firstOrNull { it.id == parsed.id }
                                    if (animal != null) {
                                        // 시설 이름 찾기
                                        val facility = zooData.facilities.firstOrNull { 
                                            it.facilityId == animal.facilityId || it.id == animal.facilityId 
                                        }
                                        val facilityName = facility?.nameKo ?: "Unknown"
                                        
                                        // 스탬프 수집
                                        lifecycleOwner.lifecycleScope.launch {
                                            val success = repo.collectStamp(
                                                animalId = parsed.id,
                                                qrCode = result,
                                                facilityName = facilityName,
                                                isTestCollection = true
                                            )
                                            if (success) {
                                                toast(successMsg)
                                                navController.navigate(Destinations.BingoHome.route)
                                            } else {
                                                toast(alreadyCollectedMsg)
                                                navController.navigate(Destinations.BingoHome.route)
                                            }
                                        }
                                    } else {
                                        toast(notFoundMsg)
                                        navController.navigate(Destinations.BingoHome.route)
                                    }
                                }
                                QRPayload.Type.FACILITY -> {
                                    // 시설 QR은 탐색으로 이동
                                    navController.navigate("explorer/detail/${parsed.id}")
                                }
                                QRPayload.Type.BINGO -> {
                                    navController.navigate(Destinations.BingoHome.route)
                                }
                                QRPayload.Type.EVENT -> {
                                    toast(eventComingMsg)
                                    navController.popBackStack()
                                }
                            }
                        }
                        else -> { 
                            toast(invalidMsg)
                            navController.popBackStack() 
                        }
                    }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

enum class Destinations(val route: String) {
    BingoHome("bingoHome"), Explorer("explorer"), FieldGuide("fieldGuide");
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomItem(Destinations.BingoHome, Icons.Filled.Home),
        BottomItem(Destinations.Explorer, Icons.Filled.Search),
        BottomItem(Destinations.FieldGuide, Icons.Filled.Info)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar(
        containerColor = com.konit.stampzooaos.ui.theme.ZooPointBlack,
        contentColor = com.konit.stampzooaos.ui.theme.ZooWhite
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isTopLevelDestinationInHierarchy(item.destination),
                onClick = {
                    navController.navigate(item.destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.destination.route) },
                label = {
                    val label = when (item.destination) {
                        Destinations.BingoHome -> stringResource(id = R.string.tab_bingo)
                        Destinations.Explorer -> stringResource(id = R.string.tab_explorer)
                        Destinations.FieldGuide -> stringResource(id = R.string.tab_fieldguide)
                    }
                    Text(text = label)
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = com.konit.stampzooaos.ui.theme.ZooPopGreen,
                    selectedTextColor = com.konit.stampzooaos.ui.theme.ZooPopGreen,
                    unselectedIconColor = com.konit.stampzooaos.ui.theme.ZooWhite,
                    unselectedTextColor = com.konit.stampzooaos.ui.theme.ZooWhite,
                    indicatorColor = com.konit.stampzooaos.ui.theme.ZooPointBlack
                )
            )
        }
    }
}

private data class BottomItem(
    val destination: Destinations,
    val icon: ImageVector
)

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: Destinations): Boolean {
    return this?.hierarchy?.any { it.route == destination.route } == true
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}

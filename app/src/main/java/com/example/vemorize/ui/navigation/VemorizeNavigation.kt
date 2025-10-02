package com.example.vemorize.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vemorize.data.auth.AuthRepository
import com.example.vemorize.data.auth.AuthState
import com.example.vemorize.ui.auth.LoginScreen
import com.example.vemorize.ui.screens.GalleryScreen
import com.example.vemorize.ui.screens.HomeScreen
import com.example.vemorize.ui.screens.SlideshowScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : NavigationItem("home", "Home", Icons.Default.Home)
    data object Gallery : NavigationItem("gallery", "Gallery", Icons.Default.Settings)
    data object Slideshow : NavigationItem("slideshow", "Slideshow", Icons.Default.Info)
}

val navigationItems = listOf(
    NavigationItem.Home,
    NavigationItem.Gallery,
    NavigationItem.Slideshow
)

@HiltViewModel
class AppViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    val authState: StateFlow<AuthState> = authRepository.authState as StateFlow<AuthState>
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VemorizeApp(
    viewModel: AppViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Navigate to login if not authenticated
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Unauthenticated -> navController.navigate("login") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
            AuthState.Authenticated -> {
                if (navController.currentDestination?.route == "login") {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Vemorize", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                navigationItems.forEach { item ->
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Vemorize") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Home, contentDescription = "Add")
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = when (authState) {
                    AuthState.Authenticated -> NavigationItem.Home.route
                    else -> "login"
                },
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(NavigationItem.Home.route) {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                composable(NavigationItem.Home.route) {
                    HomeScreen()
                }
                composable(NavigationItem.Gallery.route) {
                    GalleryScreen()
                }
                composable(NavigationItem.Slideshow.route) {
                    SlideshowScreen()
                }
            }
        }
    }
}

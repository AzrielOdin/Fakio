package com.example.fakio.presentation.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fakio.presentation.ui.gallery.GalleryScreen
import com.example.fakio.presentation.ui.gallery.GalleryViewModel
import com.example.fakio.presentation.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

@Composable
fun MyApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val galleryViewModel: GalleryViewModel = viewModel()

    // Track the upload state from ViewModel
    val uploadState: State<GalleryViewModel.UploadState> =
        galleryViewModel.uploadState.collectAsState()
    val uploadStateValue = uploadState.value

    // Create a SnackbarHostState to show snackbars
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uploadStateValue) {
        when (uploadStateValue) {
            is GalleryViewModel.UploadState.Success -> {
                val message = uploadStateValue.message
                snackbarHostState.showSnackbar(message)
            }

            is GalleryViewModel.UploadState.Error -> {
                val errorMessage = uploadStateValue.message
                snackbarHostState.showSnackbar("Upload failed: $errorMessage")
            }

            else -> { /* No action for other states */
            }
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Add animation specifications to make the drawer movement smoother
        gesturesEnabled = true,
        // Scrim color with alpha for better visual feedback
        scrimColor = Color.Black.copy(alpha = 0.32f),
        drawerContent = {
            // Updated DrawerContent call to include width constraint
            DrawerContent(
                currentRoute = currentRoute,
                onDestinationClicked = { route ->
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AppTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentRoute == Screen.Gallery.route) {
                    FloatingActionButton(
                        onClick = {
                            if (currentRoute == Screen.Gallery.route) {
                                galleryViewModel.uploadSelectedImage()
                            }
                        },
                        containerColor = Color.Red
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }
            },
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Gallery.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                // Simply define composables directly in the NavHost builder
                composable(route = Screen.Gallery.route) {
                    GalleryScreen(galleryViewModel)
                }

                composable(route = Screen.Settings.route) {
                    //TODO should this be on top of the othher one to seemlesly animate
                    SettingsScreen()
                }
            }
        }
    }
}
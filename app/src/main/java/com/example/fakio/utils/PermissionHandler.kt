package com.example.fakio.utils

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    onPermissionsGranted: () -> Unit,
    permissionsRequiredContent: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Determine required permissions based on Android version
    val mediaPermissions = remember {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+
                listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                // Android 12 and below
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Create permission state
    val permissionsState = rememberMultiplePermissionsState(
        permissions = mediaPermissions
    )

    // State to track if settings dialog should be shown
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Check if any permission was permanently denied
    val permissionsPermanentlyDenied = permissionsState.permissions.any {
        !it.status.isGranted && !it.status.shouldShowRationale
    }

    // Handle permission result
    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsPermanentlyDenied) {
        when {
            permissionsState.allPermissionsGranted -> {
                onPermissionsGranted()
            }
            permissionsPermanentlyDenied -> {
                showSettingsDialog = true
            }
        }
    }

    // Request permissions on first launch
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // If permissions aren't granted, show permission UI
    if (!permissionsState.allPermissionsGranted) {
        if (showSettingsDialog) {
            SettingsDialog(
                onDismiss = { showSettingsDialog = false },
                onGoToSettings = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            )
        }

        permissionsRequiredContent()
    }
}

@Composable
private fun SettingsDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("Photo access permission is required for this feature to work. Please grant it in app settings.") },
        confirmButton = {
            Button(onClick = onGoToSettings) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
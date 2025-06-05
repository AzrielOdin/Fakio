@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fakio.presentation.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AppTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Image Gallery") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open menu"
                )
            }
        }
    )
}

@Composable
fun DrawerContent(
    currentRoute: String?,
    onDestinationClicked: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Gallery App",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        Screen.drawerItems.forEach { screen ->
            NavigationDrawerItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = { onDestinationClicked(screen.route) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Gallery : Screen("gallery", "Gallery", Icons.Default.AccountBox)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val drawerItems = listOf(Settings)
        val items = listOf(Gallery, Settings)
    }
}

package com.example.fakio.presentation.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        SectionTitle("Network")

        var wifiOnlyEnabled by remember { mutableStateOf(true) }
        SettingsItem(
            icon = Icons.Default.Call,
            title = "WiFi Only",
            subText = "Photos will be sent only on WiFi",
            checked = wifiOnlyEnabled,
            onCheckedChange = { wifiOnlyEnabled = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("Help")

        SettingsItem(
            icon = Icons.Default.Search,
            title = "FAQ",
            subText = "Frequently asked questions"
        )

        SettingsItem(
            icon = Icons.Default.Person,
            title = "Contact Support",
            subText = "Get help from our team"
        )

        SettingsItem(
            icon = Icons.Default.Settings,
            title = "Troubleshooting",
            subText = "Fix common issues"
        )

        SettingsItem(
            icon = Icons.Default.Info,
            title = "Tutorial",
            subText = "Learn how to use the app"
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("About")

        SettingsItem(
            icon = Icons.Default.Info,
            title = "App Version",
            subText = "1.0.0"
        )

        SettingsItem(
            icon = Icons.Default.Create,
            title = "Privacy Policy",
            subText = "Read our privacy policy"
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    Divider()
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subText: String,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (checked != null && onCheckedChange != null) {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            } else if (checked != null) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Enabled",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
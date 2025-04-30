package com.example.fakio.presentation.ui.gallery

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.fakio.utils.PermissionHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.material3.FloatingActionButton as FloatingActionButton1


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel = viewModel()) {
    val context = LocalContext.current
    val previewHeight = 300.dp
    val density = LocalDensity.current

    // States from the ViewModel
    val mediaItems by viewModel.mediaItems.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    // Use the permission handler
    PermissionHandler(
        onPermissionsGranted = {
            viewModel.loadMedia()
        },
        permissionsRequiredContent = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    val isAndroid14Plus =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

                    Text(
                        text = if (isAndroid14Plus) {
                            "Select photos access required"
                        } else {
                            "Photos access required"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isAndroid14Plus) {
                            "This app needs access to your photos to display them. You can select specific photos to share."
                        } else {
                            "This app needs access to your photos to display them in the gallery."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Placeholder for the permission button
                    // The actual permission request is handled by the PermissionHandler
                    Button(onClick = { /* No-op, handled by PermissionHandler */ }) {
                        Text("Grant Permission")
                    }

                    if (isAndroid14Plus) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                // Launch the photo picker as an alternative (for Android 14+)
                                Toast.makeText(
                                    context,
                                    "Photo picker would launch here",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text("Select Specific Photos")
                        }
                    }
                }
            }
        }
    )

    // Only show gallery content if we have permissions (this check is redundant because
    // PermissionHandler only calls its content if permissions are granted, but it's a safeguard)
    if (mediaItems.isNotEmpty()) {
        // Your gallery UI code
        GalleryContent(
            mediaItems = mediaItems,
            folders = folders,
            selectedFolder = selectedFolder,
            selectedImageUri = selectedImageUri,
            viewModel = viewModel,
            previewHeight = previewHeight,
            density = density
        )
    }

    //TODO move in it's own composable
    FloatingActionButton1(
        onClick = {
            // Handle FAB click - navigate to next screen or process selected image
            Toast.makeText(context, "Selected image: ${selectedImageUri?.toString() ?: "None"}", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .padding(16.dp),
        containerColor = Color.Red
    ) {
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Next",
            tint = Color.White
        )
    }
}

@Composable
private fun GalleryContent(
    mediaItems: List<MediaItem>,
    folders: List<MediaFolder>,
    selectedFolder: MediaFolder?,
    selectedImageUri: Uri?,
    viewModel: GalleryViewModel,
    previewHeight: androidx.compose.ui.unit.Dp,
    density: androidx.compose.ui.unit.Density
) {
    var scrollOffset by remember { mutableStateOf(0f) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Filter images by selected folder
    val filteredImages = remember(selectedFolder, mediaItems) {
        selectedFolder?.let { folder ->
            mediaItems.filter { it.folderId == folder.id }
        } ?: mediaItems
    }

    // Nested scroll connection for sliding behavior
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = scrollOffset + delta
                scrollOffset = newOffset.coerceIn(-previewHeight.value * density.density, 0f)
                return Offset.Zero
            }
        }
    }

    // Calculate offset animation
    val previewOffset by animateDpAsState(
        targetValue = with(density) { scrollOffset.toDp() },
        label = "previewOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Preview Section
        Box(
            modifier = Modifier
                .height(previewHeight)
                .fillMaxWidth()
                .offset(y = previewOffset)
        ) {
            // Selected image preview
            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("No images found")
            }
        }

        // Grid Section with folder selector
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = previewHeight + previewOffset)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Folder selector
                selectedFolder?.let { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { dropdownExpanded = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Folder",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select folder"
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            folders.forEach { folderItem ->
                                DropdownMenuItem(
                                    text = { Text(folderItem.name) },
                                    onClick = {
                                        viewModel.selectFolder(folderItem)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Image grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredImages) { mediaItem ->
                        MediaThumbnail(
                            mediaItem = mediaItem,
                            isSelected = mediaItem.uri == selectedImageUri,
                            onClick = { viewModel.selectImage(mediaItem.uri) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaThumbnail(
    mediaItem: MediaItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(model = mediaItem.uri),
                contentDescription = mediaItem.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }
}


@Preview(
    name = "Gallery Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun GalleryScreenPreview() {
    // Create a simple preview with mock data
    val previewImageUri = Uri.parse("https://placekitten.com/200/300")

    // Sample data for preview
    val mediaItems = listOf(
        MediaItem(1L, previewImageUri, "Cat 1", "Cats"),
        MediaItem(2L, previewImageUri, "Cat 2", "Cats"),
        MediaItem(3L, previewImageUri, "Cat 3", "Cats"),
        MediaItem(4L, previewImageUri, "Dog 1", "Dogs"),
        MediaItem(5L, previewImageUri, "Dog 2", "Dogs")
    )


    // Create a mock UI that resembles the gallery screen
    Column(modifier = Modifier.fillMaxSize()) {
        // Preview area (simplified)
        Box(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Image Preview Area", style = MaterialTheme.typography.bodyLarge)
        }

        // Folder selector (simplified)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Folder",
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = "Cats",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select folder"
            )
        }

        // Grid preview (simplified)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(mediaItems) { mediaItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(mediaItem.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}


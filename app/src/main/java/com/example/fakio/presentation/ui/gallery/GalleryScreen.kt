package com.example.fakio.presentation.ui.gallery

import android.net.Uri
import android.os.Build
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel = viewModel()) {
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
            PermissionRequestContent()
        }
    )

    // Only show gallery content if we have permissions
    if (mediaItems.isNotEmpty()) {
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
}

@Composable
private fun PermissionRequestContent() {
    val context = LocalContext.current
    val isAndroid14Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (isAndroid14Plus) "Select photos access required" else "Photos access required",
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

            Button(onClick = { /* No-op, handled by PermissionHandler */ }) {
                Text("Grant Permission")
            }

            if (isAndroid14Plus) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
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
        ImagePreviewSection(
            selectedImageUri = selectedImageUri,
            previewHeight = previewHeight,
            previewOffset = previewOffset
        )

        // Grid Section with folder selector
        ImagesGridSection(
            filteredImages = filteredImages,
            folders = folders,
            selectedFolder = selectedFolder,
            selectedImageUri = selectedImageUri,
            viewModel = viewModel,
            previewHeight = previewHeight,
            previewOffset = previewOffset
        )
    }
}

@Composable
private fun ImagePreviewSection(
    selectedImageUri: Uri?,
    previewHeight: androidx.compose.ui.unit.Dp,
    previewOffset: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .height(previewHeight)
            .fillMaxWidth()
            .offset(y = previewOffset)
    ) {
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
}

@Composable
private fun ImagesGridSection(
    filteredImages: List<MediaItem>,
    folders: List<MediaFolder>,
    selectedFolder: MediaFolder?,
    selectedImageUri: Uri?,
    viewModel: GalleryViewModel,
    previewHeight: androidx.compose.ui.unit.Dp,
    previewOffset: androidx.compose.ui.unit.Dp
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = previewHeight + previewOffset)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Folder selector
            FolderSelector(
                folders = folders,
                selectedFolder = selectedFolder,
                onFolderSelected = viewModel::selectFolder
            )

            // Image grid
            ImagesGrid(
                images = filteredImages,
                selectedImageUri = selectedImageUri,
                onImageSelected = viewModel::selectImage
            )
        }
    }
}

@Composable
private fun FolderSelector(
    folders: List<MediaFolder>,
    selectedFolder: MediaFolder?,
    onFolderSelected: (MediaFolder) -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select folder"
            )

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                folders.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            onFolderSelected(item)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagesGrid(
    images: List<MediaItem>,
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(images) { item ->
            val isSelected = selectedImageUri == item.uri

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onImageSelected(item.uri) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(model = item.uri),
                        contentDescription = item.name,
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
    }
}

@Preview
@Composable
private fun PermissionRequestPreview() {
    MaterialTheme {
        PermissionRequestContent()
    }
}
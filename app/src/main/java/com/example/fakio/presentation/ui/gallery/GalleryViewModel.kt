package com.example.fakio.presentation.ui.gallery

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fakio.data.repository.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaFolder(val id: String, val name: String)
data class MediaItem(val id: Long, val uri: Uri, val name: String, val folderId: String)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val _folders = MutableStateFlow<List<MediaFolder>>(emptyList())
    val folders: StateFlow<List<MediaFolder>> = _folders.asStateFlow()

    private val _selectedFolder = MutableStateFlow<MediaFolder?>(null)
    val selectedFolder: StateFlow<MediaFolder?> = _selectedFolder.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val uploadRepository = UploadRepository(application.applicationContext)

    init {
        loadMedia()
    }

    fun loadMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val images = loadImagesFromMediaStore()
            _mediaItems.value = images

            // Extract unique folders
            val uniqueFolders = images
                .map { MediaFolder(it.folderId, it.folderId) }
                .distinctBy { it.id }
            _folders.value = uniqueFolders

            // Set initial selections
            if (uniqueFolders.isNotEmpty() && _selectedFolder.value == null) {
                _selectedFolder.value = uniqueFolders.first()
            }

            if (images.isNotEmpty() && _selectedImageUri.value == null) {
                _selectedImageUri.value = images.first().uri
                Log.d("Fakio bug", "Initial selected image: ${_selectedImageUri.value}")
            }
        }
    }

    fun selectFolder(folder: MediaFolder) {
        _selectedFolder.value = folder
    }

    fun selectImage(uri: Uri) {
        Log.d("Fakio bug", "Previous selected image: ${_selectedImageUri.value}")
        _selectedImageUri.value = uri
        Log.d("Fakio bug", "New selected image: ${_selectedImageUri.value}")
    }

    // Function to load images from MediaStore
    private fun loadImagesFromMediaStore(): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
        val context = getApplication<Application>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val bucket = cursor.getString(bucketColumn) ?: "Unknown"

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                images.add(MediaItem(id, contentUri, name, bucket))
            }
        }

        return images
    }

    // Get filtered images based on selected folder
    fun getFilteredImages(): List<MediaItem> {
        return _selectedFolder.value?.let { folder ->
            _mediaItems.value.filter { it.folderId == folder.id }
        } ?: _mediaItems.value
    }

    fun uploadSelectedImage() {
        // Get the current selected URI
        val uri = _selectedImageUri.value ?: return

        Log.d("Fakio bug", "Uploading image: $uri")

        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            uploadRepository.uploadImage(uri).fold(
                onSuccess = { response ->
                    _uploadState.value = UploadState.Success(response.message)
                },
                onFailure = { exception ->
                    _uploadState.value = UploadState.Error(exception.message ?: "Unknown error")
                }
            )
        }
    }

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val message: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }
}
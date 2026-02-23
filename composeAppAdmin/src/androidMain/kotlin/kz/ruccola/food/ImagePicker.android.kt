package kz.ruccola.food

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kz.ruccola.food.viewmodel.DishImagesViewModel

@Composable
actual fun provideImagePicker(): (DishImagesViewModel) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Use a Ref-like pattern to keep track of the current viewModel without triggering recomposition for it
    val lastViewModel = remember { mutableStateOf<DishImagesViewModel?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val cr = context.contentResolver
                    val mime = cr.getType(uri) ?: "application/octet-stream"
                    val name =
                        cr.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                            ?.use { cursor ->
                                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                            } ?: "upload.bin"
                    val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        lastViewModel.value?.uploadImage(name, mime, bytes)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    return { viewModel ->
        lastViewModel.value = viewModel
        pickImageLauncher.launch("image/*")
    }
}

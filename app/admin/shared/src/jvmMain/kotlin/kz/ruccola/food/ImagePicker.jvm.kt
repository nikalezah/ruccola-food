package kz.ruccola.food

import androidx.compose.runtime.Composable
import kz.ruccola.food.feature.dish.DishImagesViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun provideImagePicker(): (DishImagesViewModel) -> Unit = { viewModel ->
    val chooser = JFileChooser()
    chooser.fileFilter = FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "webp")
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val file: File = chooser.selectedFile
        val bytes = file.readBytes()
        val mimeType =
            when (file.extension.lowercase()) {
                "jpg",
                "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "application/octet-stream"
            }
        viewModel.uploadImage(file.name, mimeType, bytes)
    }
}

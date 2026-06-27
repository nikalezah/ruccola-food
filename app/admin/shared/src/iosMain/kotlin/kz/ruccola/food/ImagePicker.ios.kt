package kz.ruccola.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kz.ruccola.food.viewmodel.DishImagesViewModel
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURLKey
import platform.UIKit.UIImagePickerControllerOriginalImageKey
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate(
    private val viewModel: DishImagesViewModel,
    private val onDismiss: () -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onDismiss()

        val url = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURLKey] as? NSURL
        if (url != null) {
            val data = NSData.dataWithContentsOfURL(url) ?: return
            val name = url.lastPathComponent ?: "upload.jpg"
            viewModel.uploadImage(name, mimeTypeFor(name), data.toByteArray())
            return
        }

        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImageKey] as? UIImage ?: return
        val data = UIImageJPEGRepresentation(image, 0.9) ?: return
        viewModel.uploadImage("upload.jpg", "image/jpeg", data.toByteArray())
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onDismiss()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }

private fun mimeTypeFor(name: String): String =
    when (name.substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        else -> "image/jpeg"
    }

@Composable
actual fun provideImagePicker(): (DishImagesViewModel) -> Unit {
    val viewController = LocalUIViewController.current
    val delegateHolder = remember { mutableListOf<ImagePickerDelegate>() }

    return { viewModel ->
        val picker = UIImagePickerController()
        var delegateRef: ImagePickerDelegate? = null
        val delegate =
            ImagePickerDelegate(viewModel) {
                delegateRef?.let { delegateHolder.remove(it) }
            }
        delegateRef = delegate
        delegateHolder.add(delegate)
        picker.delegate = delegate
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        viewController.presentViewController(picker, animated = true, completion = null)
    }
}

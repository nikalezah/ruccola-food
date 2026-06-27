package kz.ruccola.food

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import kz.ruccola.food.feature.dish.DishImagesViewModel
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun provideImagePicker(): (DishImagesViewModel) -> Unit =
    { viewModel ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = {
            val file = input.files?.item(0)!!
            val reader = FileReader()
            reader.onload = { _ ->
                val result = reader.result as ArrayBuffer
                val array = Int8Array(result)
                val bytes = ByteArray(array.length) { i -> array[i] }
                viewModel.uploadImage(file.name, file.type, bytes)
            }
            reader.readAsArrayBuffer(file)
        }
        input.click()
    }

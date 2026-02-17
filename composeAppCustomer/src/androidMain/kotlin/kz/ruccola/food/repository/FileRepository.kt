package kz.ruccola.food.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.ruccola.food.api.FileApi
import kz.ruccola.food.api.FileDto

class FileRepository {
    private val api = FileApi()
    private val tag = "FileRepository"

    suspend fun upload(
        context: Context,
        uri: Uri,
    ): Result<FileDto> =
        withContext(Dispatchers.IO) {
            try {
                val cr = context.contentResolver
                val mime = cr.getType(uri) ?: "application/octet-stream"
                val name = queryDisplayName(context, uri) ?: "upload.bin"
                val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext Result.failure(IllegalStateException("Unable to read file"))

                Result.success(api.upload(name, mime, bytes))
            } catch (e: Exception) {
                Log.e(tag, "Upload failed", e)
                Result.failure(e)
            }
        }

    suspend fun delete(id: Int): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(api.delete(id))
            } catch (e: Exception) {
                Log.e(tag, "Error deleting file", e)
                Result.failure(e)
            }
        }

    private fun queryDisplayName(
        context: Context,
        uri: Uri,
    ): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return null
    }
}

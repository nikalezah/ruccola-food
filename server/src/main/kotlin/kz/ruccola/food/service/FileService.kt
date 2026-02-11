package kz.ruccola.food.service

import io.ktor.http.content.PartData
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.io.readByteArray
import kz.ruccola.food.api.FileDto
import kz.ruccola.food.dbQuery
import kz.ruccola.food.model.Files
import kz.ruccola.food.now
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertReturning
import org.jetbrains.exposed.v1.r2dbc.select
import java.io.File
import java.nio.file.Files as NioFiles

class FileService(
    val filesDirPath: String = FILES_DIR_PATH, // todo: remove?
) {
    companion object {
        const val FILES_DIR_PATH = "files" // todo: move somewhere else
    }

    private fun ensureUploadDir(): File {
        val dir = File(filesDirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun save(part: PartData.FileItem): FileDto =
        dbQuery {
            val dir = ensureUploadDir()
            val originalName = part.originalFileName ?: "file_${System.currentTimeMillis()}"
            val target = File(dir, uniqueName(dir, originalName))
            val bytes = part.provider().readRemaining().readByteArray()
            target.writeBytes(bytes)
            val mime =
                part.contentType?.toString() ?: NioFiles.probeContentType(target.toPath()) ?: "application/octet-stream"
            Files.insertReturning {
                it[filename] = target.name
                it[path] = target.absolutePath
                it[mimeType] = mime
                it[size] = bytes.size.toLong()
                it[createdAt] = now()
            }.single().let(::toDto)
        }

    suspend fun delete(fileId: Int): Boolean =
        dbQuery {
            val path = Files.select(Files.path)
                .where { Files.id eq fileId }
                .singleOrNull()
                ?.get(Files.path)
                ?: return@dbQuery false
            try {
                File(path).takeIf { it.exists() }?.delete()
                Files.deleteWhere { Files.id eq fileId }
                true
            } catch (_: Exception) {
                false
            }
        }

    private fun uniqueName(
        dir: File,
        baseName: String,
    ): String {
        var name = baseName
        var i = 1
        while (File(dir, name).exists()) {
            val dot = baseName.lastIndexOf('.')
            name = if (dot != -1) {
                baseName.substring(0, dot) + "_" + i++ + baseName.substring(dot)
            } else {
                baseName + "_" + i++
            }
        }
        return name
    }

    fun toDto(row: ResultRow): FileDto =
        FileDto(
            row[Files.id].value,
            "/$filesDirPath/${row[Files.filename]}",
            row[Files.filename],
            row[Files.size],
            row[Files.mimeType],
        )
}

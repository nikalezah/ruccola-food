package kz.ruccola.food.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Resource("files")
class Files {
    @Resource("{id}")
    class Id(val parent: Files = Files(), val id: Int)
}

class FileApi(private val client: HttpClient = httpClient) {
    suspend fun upload(filename: String, mimeType: String, bytes: ByteArray): FileDto {
        val response: HttpResponse =
            client.post(Files()) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = bytes,
                                headers =
                                    Headers.build {
                                        append(HttpHeaders.ContentType, mimeType)
                                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                                    },
                            )
                        }
                    )
                )
            }
        if (!response.status.isSuccess()) {
            val msg = runCatching { response.bodyAsText() }.getOrNull() ?: "HTTP ${response.status.value}"
            throw Exception(msg)
        }
        return response.body()
    }

    suspend fun delete(id: Int) = client.delete(Files.Id(id = id)).status.isSuccess()
}

@Serializable
data class FileDto(val id: Int, val url: String, val filename: String, val size: Long, val mimeType: String)

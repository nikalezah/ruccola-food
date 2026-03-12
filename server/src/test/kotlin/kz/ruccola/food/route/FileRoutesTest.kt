package kz.ruccola.food.route

import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kz.ruccola.food.initializeTestDatabase
import kz.ruccola.food.loginAdmin
import kz.ruccola.food.testApp
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileRoutesTest {
    private val storagePath = ApplicationConfig("application-test.conf").property("ktor.storage.path").getString()

    @BeforeTest
    fun cleanUploadsDir() {
        // Initialize a test database to ensure routes have DB access
        initializeTestDatabase()
        // Ensure the files directory is clean before each test run to avoid filename collisions
        val dir = File(storagePath)
        if (dir.exists()) {
            dir.listFiles()?.forEach { it.delete() }
        } else {
            dir.mkdirs()
        }
    }

    @Test
    fun testUploadAndDeleteFileApi() =
        testApp { client ->
            val token = client.loginAdmin()

            // Upload a file via multipart
            val filename = "test.txt"
            val contentBytes = "hello world".toByteArray()

            val response = client.post("/api/files") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = contentBytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        ContentDisposition.File.withParameter(
                                            ContentDisposition.Parameters.Name,
                                            "file",
                                        )
                                            .withParameter(ContentDisposition.Parameters.FileName, filename).toString(),
                                    )
                                },
                            )
                        },
                    ),
                )
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            val id = json["id"]?.jsonPrimitive?.content?.toInt()
            val url = json["url"]?.jsonPrimitive?.content
            val returnedFilename = json["filename"]?.jsonPrimitive?.content
            val size = json["size"]?.jsonPrimitive?.content?.toLong()
            val mime = json["mimeType"]?.jsonPrimitive?.content

            assertNotNull(id)
            assertTrue(id > 0)
            assertNotNull(url)
            assertTrue(url.startsWith("/files/"))
            assertNotNull(returnedFilename)
            assertTrue(returnedFilename.endsWith(".txt"))
            assertEquals(contentBytes.size.toLong(), size)
            assertEquals("text/plain", mime)

            // Verify file exists on disk
            val diskFile = File(storagePath, returnedFilename)
            assertTrue(diskFile.exists(), "Uploaded file should exist on disk")
            assertEquals(contentBytes.size.toLong(), diskFile.length())

            // Delete the file via API
            val deleteResponse = client.delete("/api/files/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, deleteResponse.status)

            // After deletion, the file should not exist
            assertTrue(!diskFile.exists(), "Uploaded file should be removed from disk after deletion")

            // The second delete should return 404
            val deleteAgain = client.delete("/api/files/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.NotFound, deleteAgain.status)
        }

    @Test
    fun testFileApiBadRequests() =
        testApp { client ->
            val token = client.loginAdmin()

            // POST without a file part returns 400
            val badPost = client.post("/api/files") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(MultiPartFormDataContent(formData { }))
            }
            assertEquals(HttpStatusCode.BadRequest, badPost.status)

            // DELETE with invalid id returns 400
            val badDelete = client.delete("/api/files/abc") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.BadRequest, badDelete.status)
        }
}

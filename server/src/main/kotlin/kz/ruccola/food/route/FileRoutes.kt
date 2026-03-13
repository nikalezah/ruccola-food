package kz.ruccola.food.route

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.resources.delete
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kz.ruccola.food.api.FileDto
import kz.ruccola.food.api.Files
import kz.ruccola.food.api.Role
import kz.ruccola.food.service.FileService
import kz.ruccola.food.withRole

fun Route.configureFileRoutes() {
    val fileService = FileService()

    withRole(Role.ADMIN) {
        post<Files> {
            val multipart = call.receiveMultipart()
            var saved: FileDto? = null
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        saved = fileService.save(part)
                    }

                    else -> {}
                }
                part.dispose()
            }
            if (saved == null) {
                call.respond(HttpStatusCode.BadRequest, "No file part")
            } else {
                call.respond(saved)
            }
        }

        delete<Files.Id> { file ->
            val ok = fileService.delete(file.id)
            if (ok) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
    }
}

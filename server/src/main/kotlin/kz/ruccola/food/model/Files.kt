package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Files : IntIdTable() {
    val filename = varchar("filename", 255)
    val path = varchar("path", 512) // absolute or relative path on disk
    val mimeType = varchar("mime_type", 100)
    val size = long("size")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

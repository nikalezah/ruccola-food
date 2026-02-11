package kz.ruccola.food.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.date

object Days : IntIdTable() {
    val date = date("date").uniqueIndex()
}

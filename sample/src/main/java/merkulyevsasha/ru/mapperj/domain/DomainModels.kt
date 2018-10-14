package merkulyevsasha.ru.mapperj.domain

import merkulyevsasha.ru.annotations.Mapper
import merkulyevsasha.ru.annotations.params.Source
import merkulyevsasha.ru.mapperj.entities.DbEntity
import merkulyevsasha.ru.mapperj.network.ModelResponse
import java.util.*

@Mapper(source = Source.Kotlin, twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModel1(
    val id: Int,
    val name: String?,
    val x: Float,
    val y: Double,
    val z: Long,
    val ab: Short,
    val specialName: String
)

data class DomainChild(
    val id: Int,
    val name: String,
    val zz: Byte,
    val ab: Short,
    val date: Date
)




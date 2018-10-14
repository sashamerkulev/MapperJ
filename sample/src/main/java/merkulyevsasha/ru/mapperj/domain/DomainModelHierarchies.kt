package merkulyevsasha.ru.mapperj.domain

import merkulyevsasha.ru.annotations.Mapper
import merkulyevsasha.ru.annotations.params.Source
import merkulyevsasha.ru.mapperj.entities.DbEntityDeep
import merkulyevsasha.ru.mapperj.network.ModelResponse

@Mapper(source = Source.Kotlin, twoWayMapClasses = [DbEntityDeep::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModelHierarchies(
    val id: Int,
    val name: String,
    val x: Float,
    val y: Double,
    val z: Long,
    val ab: Short,
    val children: List<DomainChild>,
    val childrenDeep: List<DomainChildDeep>
)

data class DomainChildDeep(val xyz: String, val childDeep: DomainChildDeepChild)

data class DomainChildDeepChild(val id: Int, val name: String)


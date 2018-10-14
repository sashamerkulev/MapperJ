package merkulyevsasha.ru.mapperj.domain

import merkulyevsasha.ru.annotations.Mapper
import merkulyevsasha.ru.mapperj.entities.DbEntity
import merkulyevsasha.ru.mapperj.network.ModelResponse

@Mapper(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModelHierarchy(
    val id: Int,
    val name: String?,
    val x: Float,
    val y: Double,
    val z: Long,
    val child: DomainChild,
    val ab: Short,
    val children: List<DomainChild>
)


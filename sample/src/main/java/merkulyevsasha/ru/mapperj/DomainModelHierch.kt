package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.Mapper

@Mapper(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModelHierch(
    val id: Int,
    val name: String,
    val x: Float,
    val y: Double,
    val z: Long,
    val child: DomainChild,
    val ab: Short,
    val children: List<DomainChild>
)


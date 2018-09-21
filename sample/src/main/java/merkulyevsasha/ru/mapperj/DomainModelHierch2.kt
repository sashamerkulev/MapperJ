package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.Mapper

@Mapper(twoWayMapClasses = [DbEntityDeep::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModelHierch2(
        val id: Int,
        val name: String,
        val x: Float,
        val y: Double,
        val z: Long,
        val child: DomainChild,
        val ab: Short,
        val children: List<DomainChild>,
        val childrenDeep: List<DomainChildDeep>
)

data class DomainChildDeep(val xyz: String, val childDeep: DomainChildDeepChild)

data class DomainChildDeepChild(val id: Int, val name: String)

data class DbEntityDeep(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short,
        val childrenDeep: List<DbChildDeepEntity>
)

data class DbChildDeepEntity(val xyz: String, val xz: Long, val childDeep: DomainChildDeepChildEntity)

data class DomainChildDeepChildEntity(val name: String)


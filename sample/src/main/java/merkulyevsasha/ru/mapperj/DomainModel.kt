package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.MapperJ

@MapperJ(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModel1(
        val id: Int,
        val name: String,
        val x: Float,
        val y: Double,
        val z: Long,
        val child: DomainChild,
        val ab: Short
)

data class DomainChild(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short
)

data class DbEntity(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short,
        val children: List<DbChildEntity>
)

data class DbChildEntity(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short
)

data class ModelResponse(
        val id2: Int,
        val name: String,
        val x: Float,
        val y: Double,
        val child: ChildResponse
)

data class ChildResponse(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short
)

package merkulyevsasha.ru.mapperj.entities

import java.util.*


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
    val ab: Short,
    val date: Date
)

data class DbEntityDeep(
    val id: Int,
    val name: String,
    val zz: Byte,
    val ab: Short,
    val childrenDeep: List<DbChildDeepEntity>
)

data class DbChildDeepEntity(val xyz: String, val xz: Long, val childDeep: DomainChildDeepChildEntity)

data class DomainChildDeepChildEntity(val name: String)


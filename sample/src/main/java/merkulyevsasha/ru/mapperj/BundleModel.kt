package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.Args
import merkulyevsasha.ru.annotations.Ignore
import merkulyevsasha.ru.annotations.params.Source

@Args(source = Source.Kotlin)
data class BundleModel(
    val id: Int,
    val name: String,
    val shrt: Short,
    val lng: Long,
    val bol: Boolean,
    val bte: Byte,
    val foat: Float,
    val dbl: Double,
    @Ignore val aaa: String
)

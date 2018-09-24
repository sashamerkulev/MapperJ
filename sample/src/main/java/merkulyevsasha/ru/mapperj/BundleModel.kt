package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.Args
import merkulyevsasha.ru.annotations.DefaultValue
import merkulyevsasha.ru.annotations.Ignore
import merkulyevsasha.ru.annotations.params.Source

@Args(source = Source.Kotlin)
data class BundleModel(
    val id: Int,
    @DefaultValue(stringValue = "yes") val name: String,
    val shrt: Short,
    @DefaultValue(longValue = 334) val lng: Long,
    val bol: Boolean,
    val bte: Byte,
    @DefaultValue(floatValue = 334F) val foat: Float,
    val dbl: Double,
    @Ignore val aaa: String
)

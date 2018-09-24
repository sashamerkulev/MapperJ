package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.Args
import merkulyevsasha.ru.annotations.DefaultValue
import merkulyevsasha.ru.annotations.params.Source

@Args(source = Source.Java)
data class FragmentArgsModel(
    @DefaultValue(intValue = -1) val id: Int,
    @DefaultValue(stringValue = "no") val name: String,
    val shrt: Short,
    val lng: Long,
    @DefaultValue(booleanValue = true) val bol: Boolean,
    val bte: Byte,
    val foat: Float,
    val dbl: Double
)

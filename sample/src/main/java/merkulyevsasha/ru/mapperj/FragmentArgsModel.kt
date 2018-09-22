package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.Args
import merkulyevsasha.ru.annotations.Source

@Args(source = Source.Java)
data class FragmentArgsModel(
    val id: Int,
    val name: String,
    val shrt: Short,
    val lng: Long,
    val bol: Boolean,
    val bte: Byte,
    val foat: Float,
    val dbl: Double
)

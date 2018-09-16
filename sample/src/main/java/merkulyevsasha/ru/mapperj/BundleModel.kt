package merkulyevsasha.ru.mapperj

import merkulyevsasha.ru.annotations.ArgsJ

@ArgsJ
data class BundleModel(
        val id: Int,
        val name: String,
        val shrt: Short,
        val lng: Long,
        val bol: Boolean,
        val bte: Byte,
        val foat: Float,
        val dbl: Double
)

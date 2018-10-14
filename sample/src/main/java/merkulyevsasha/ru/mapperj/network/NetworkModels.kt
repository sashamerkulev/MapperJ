package merkulyevsasha.ru.mapperj.network

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
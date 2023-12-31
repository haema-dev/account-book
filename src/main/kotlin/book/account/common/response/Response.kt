package book.account.common.response


data class Response<T>(
    val status: Int,
    val message: String,
    val data: T? = null
)

package airsign.signage.player.data.remote

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(
        val code: Int? = null,
        val message: String? = null,
        val throwable: Throwable? = null,
        val errorBody: String? = null
    ) : ApiResult<Nothing>()

    object Unauthorized : ApiResult<Nothing>()
    object NotFound : ApiResult<Nothing>()
}



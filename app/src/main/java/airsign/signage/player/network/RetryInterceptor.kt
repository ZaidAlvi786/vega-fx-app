package airsign.signage.player.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * An OkHttp Interceptor that retries failed requests with an exponential backoff.
 * Target failures: SocketTimeoutException, IOException, and HTTP 502, 503, 504.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMillis: Long = 2000L
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount <= maxRetries) {
            try {
                if (tryCount > 0) {
                    val delay = initialDelayMillis * (1 shl (tryCount - 1)) // Exponential backoff: 2s, 4s, 8s
                    Log.w(TAG, "Retrying request (${tryCount}/$maxRetries) in ${delay}ms: ${request.url()}")
                    Thread.sleep(delay)
                }

                response = chain.proceed(request)
                
                // If it's a successful response or a non-retryable error, return it
                if (response.isSuccessful || !isRetryable(response.code())) {
                    return response
                }

                // If it's a retryable server error (502, 503, 504), close the response and retry
                Log.e(TAG, "Received retryable error code ${response.code()} for ${request.url()}")
                response.close()

            } catch (e: SocketTimeoutException) {
                exception = e
                Log.e(TAG, "Socket timeout on attempt ${tryCount + 1}: ${e.message}")
            } catch (e: IOException) {
                exception = e
                Log.e(TAG, "I/O exception on attempt ${tryCount + 1}: ${e.message}")
            } catch (e: Exception) {
                // For other non-IO exceptions (like logic errors), don't retry
                Log.e(TAG, "Non-retryable exception: ${e.message}", e)
                throw e
            }

            tryCount++
        }

        // If we still don't have a response after all retries, throw the last exception or return the last response
        if (exception != null) {
            throw exception
        }
        
        return response ?: throw IOException("Unexpected failure: No response after $maxRetries retries")
    }

    private fun isRetryable(code: Int): Boolean {
        return code == 502 || code == 503 || code == 504
    }

    companion object {
        private const val TAG = "RetryInterceptor"
    }
}

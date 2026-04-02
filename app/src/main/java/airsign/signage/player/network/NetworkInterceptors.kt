package airsign.signage.player.data.remote

import airsign.signage.player.data.utils.BasePref
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

class AuthenticationInterceptor(
    private val basePref: BasePref
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val token = basePref.getString(BasePref.AUTH_TOKEN)
        if (token.isNotBlank()) {
            val formattedToken = if (token.startsWith("Bearer", ignoreCase = true)) {
                token
            } else {
                "Bearer $token"
            }
            requestBuilder.header("Authorization", formattedToken.trim())
        }

        return chain.proceed(requestBuilder.build())
    }
}

class NetworkExceptionInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (exception: UnknownHostException) {
            logException("UnknownHostException", exception)
            createDynamicErrorResponse(chain, 503, "No internet connection or DNS issue")
        } catch (exception: SocketTimeoutException) {
            logException("SocketTimeoutException", exception)
            createDynamicErrorResponse(chain, 504, "Request timed out")
        } catch (exception: IOException) {
            logException("IOException", exception)
            createDynamicErrorResponse(chain, 500, "Network I/O error")
        } catch (exception: Exception) {
            logException("Unexpected exception", exception)
            createDynamicErrorResponse(chain, 500, "Unexpected error occurred")
        }
    }

    private fun createDynamicErrorResponse(
        chain: Interceptor.Chain,
        code: Int,
        message: String
    ): Response {
        val responseBody = ResponseBody.create(null, message)
        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(responseBody)
            .build()
    }

    private fun logException(tag: String, throwable: Throwable) {
        Log.e(NETWORK_INTERCEPTOR_TAG, "$tag: ${throwable.message}", throwable)
    }

    companion object {
        private const val NETWORK_INTERCEPTOR_TAG = "NetworkInterceptor"
    }
}

/**
 * Interceptor to log all API calls with request and response details.
 * This provides comprehensive logging for debugging API interactions.
 */
class ApiLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestStartTime = System.currentTimeMillis()

        // Log Request
        logRequest(request)

        // Execute request
        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Request failed: ${request.method()} ${request.url()}", e)
            throw e
        }

        // Log Response
        val requestDuration = System.currentTimeMillis() - requestStartTime
        logResponse(response, requestDuration)

        return response
    }

    private fun logRequest(request: okhttp3.Request) {
        try {
            val url = request.url()
            val method = request.method()
            Log.d(TAG, "═══════════════════════════════════════════════════════")
            Log.d(TAG, "➡️ REQUEST")
            Log.d(TAG, "   Method: $method")
            Log.d(TAG, "   URL: $url")
            Log.d(TAG, "   Headers:")
            Log.d(TAG, "═══════════════════════════════════════════════════════")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging request", e)
        }
    }

    private fun logResponse(response: Response, duration: Long) {
        try {
            val request = response.request()
            val url = request.url()
            val method = request.method()
            val code = response.code()
            val message = response.message()
            val headers = response.headers()

            Log.d(TAG, "   Duration: ${duration}ms")
            
            if (headers.size() > 0) {
                Log.d(TAG, "   Headers:")
                for (i in 0 until headers.size()) {
                    Log.d(TAG, " ${headers.name(i)}: ${headers.value(i)}")
                }
            }

            // Log response body if present
            try {
                val responseBody = response.peekBody(MAX_BODY_LENGTH)
                val contentType = responseBody.contentType()
                val charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
                val bodyString = responseBody.string()

                if (bodyString.isNotBlank()) {
                    // peekBody already limits to MAX_BODY_LENGTH, so check original body size
                    val originalBodySize = response.body()?.contentLength() ?: -1
                    if (originalBodySize > MAX_BODY_LENGTH) {
                        Log.d(TAG, "   Body: $bodyString... [truncated, original size: $originalBodySize bytes]")
                    } else {
                        Log.d(TAG, "   Body: $bodyString")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "   Body: [Unable to read response body: ${e.message}]")
            }

            // Log status indicator
            when {
                code in 200..299 -> Log.d(TAG, "   ✅ Success")
                code in 400..499 -> Log.w(TAG, "   ⚠️ Client Error")
                code in 500..599 -> Log.e(TAG, "   ❌ Server Error")
                else -> Log.d(TAG, "   ℹ️ Info")
            }
            Log.d(TAG, "═══════════════════════════════════════════════════════")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging response", e)
        }
    }

    companion object {
        private const val TAG = "ApiLogging"
        private const val MAX_BODY_LENGTH = 5000L // Limit body logging to 5KB
    }
}



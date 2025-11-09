package og.ogstartracker.utils

import com.squareup.moshi.JsonDataException
import og.ogstartracker.network.ErrorIdentificationImpl
import og.ogstartracker.network.Resource
import og.ogstartracker.network.ResourceUtils
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <E : Any> tryOnline(
	doOnSuccess: suspend ((E?) -> Unit) = {},
	request: suspend () -> Response<E>
): Resource<E> {
	var httpResponse: Response<E>? = null
	return try {
		val response = request()
		httpResponse = response
		if (response.isSuccessful) {
			doOnSuccess(response.body())
			ResourceUtils.success(response.body())
		} else {
			//don't handle any errors
			ResourceUtils.error(ErrorIdentificationImpl.ConnectionProblem)
		}
	} catch (e: HttpException) {
		Timber.e(e)
		ResourceUtils.error(code = e.code())
	} catch (e: SocketTimeoutException) {
		Timber.e(e)
		ResourceUtils.error(ErrorIdentificationImpl.ConnectionProblem)
	} catch (e: UnknownHostException) {
		Timber.e(e)
		ResourceUtils.error(ErrorIdentificationImpl.ConnectionProblem)
	} catch (e: IOException) {
		Timber.e(e)
		if (e is ProtocolException) {
			ResourceUtils.error(ErrorIdentificationImpl.NoContent)
		} else {
			ResourceUtils.error(ErrorIdentificationImpl.Unknown)
		}
	} catch (e: JsonDataException) {
		// JSON parsing failed, but if HTTP response was 200 OK, the tracker is reachable
		// This happens when firmware returns plain text instead of JSON
		Timber.w(e, "JSON parse error, but HTTP response was: ${httpResponse?.code()}")
		if (httpResponse?.isSuccessful == true) {
			Timber.d("Treating as success despite JSON parse failure (tracker responded with 200 OK)")
			// Return success with null body - caller can check wifiConnected=true
			ResourceUtils.success(null)
		} else {
			ResourceUtils.error(ErrorIdentificationImpl.Unknown)
		}
	}
}

fun <T : Any> parseResponseToResource(response: Response<T>): Resource<T> {
	return if (response.isSuccessful) {
		ResourceUtils.success(response.body())
	} else {
		ResourceUtils.error(code = response.code())
	}
}

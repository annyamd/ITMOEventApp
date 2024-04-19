package org.itmo.itmoevent.model.network

import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


class EventNetworkService(private val tokenManager: TokenManager) {

    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()

    private val mainOkHttpClient = OkHttpClient()
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        logging
    }
    private val tokenInjectorInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val token = runBlocking {
                tokenManager.getToken().first()
            }

            Log.d("interceptor", token!!)
            val request = chain.request().newBuilder()
            request.addHeader("Authorization", "Bearer $token")
            chain.proceed(request.build())
        }
    }

    private val recipesOkHttpClient = mainOkHttpClient.newBuilder()
        .addInterceptor(tokenInjectorInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(recipesOkHttpClient)
        .build()

    val eventApi = retrofit.create<EventApi>()
    val rolesApi = retrofit.create<RoleApi>()
    val placeApi = retrofit.create<PlaceApi>()



    companion object {
        private const val BASE_URL: String = "http://95.216.146.187:8080"
   }
}

package uni.moviles.facial_recognition_app.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uni.moviles.facial_recognition_app.BuildConfig
import java.util.concurrent.TimeUnit

// object = singleton: una sola instancia compartida en toda la app
object ApiClient {

    private const val BASE_URL = "https://7-diegodev-7-facial-access-app.hf.space/"

    // 20s de timeout por el cold start del Space gratuito
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(Interceptor { chain ->
            // Agrega la API key en cada peticion automaticamente
            val request = chain.request().newBuilder()
                .addHeader("X-API-Key", BuildConfig.API_KEY)
                .build()
            chain.proceed(request)
        })
        .build()

    // by lazy: Retrofit se crea solo la primera vez que se usa, no al arrancar la app
    val api: HuggingFaceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuggingFaceApi::class.java)
    }
}

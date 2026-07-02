package uni.moviles.facial_recognition_app.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class RegistroResponse(
    val status: String,
    val nombre: String,
    val fotos_procesadas: Int
)

data class VerificacionResponse(
    val match: Boolean,
    val nombre: String?,
    val score: Float
)

data class UsuariosResponse(
    val usuarios: List<String>
)

interface HuggingFaceApi {

    // Envia 5 fotos y un nombre, el servidor genera el perfil facial
    @Multipart
    @POST("registrar")
    suspend fun registrar(
        @Part("nombre") nombre: RequestBody,
        @Part fotos: List<MultipartBody.Part>
    ): Response<RegistroResponse>

    // Envia una foto y el servidor indica si la persona esta registrada
    @Multipart
    @POST("verificar")
    suspend fun verificar(
        @Part foto: MultipartBody.Part
    ): Response<VerificacionResponse>

    // Devuelve la lista de nombres registrados en el sistema
    @GET("usuarios")
    suspend fun obtenerUsuarios(): Response<UsuariosResponse>
}

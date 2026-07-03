package uni.moviles.facial_recognition_app.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Respuesta al registrar una persona
data class RegistroResponse(
    val status: String,
    val nombre: String,
    val fotos_procesadas: Int   // cuantas fotos se procesaron exitosamente
)

// Respuesta al verificar una foto con el endpoint /verificar
data class VerificacionResponse(
    val match: Boolean,         // true si la persona esta registrada en el sistema
    val nombre: String?,        // nombre registrado (null si no hay coincidencia)
    val score: Float            // confianza de la coincidencia, entre 0.0 y 1.0
)

// Respuesta del endpoint /usuarios con todos los nombres registrados
data class UsuariosResponse(
    val usuarios: List<String>
)

// Representa una persona detectada en un frame del modo tiempo real
data class PersonaTiempoReal(
    val match: Boolean,         // true si coincide con alguien registrado
    val nombre: String?,        // nombre (null si la persona es desconocida)
    val score: Float,           // confianza entre 0.0 y 1.0
    val bbox: List<Float>       // rectangulo del rostro: [x1, y1, x2, y2] en pixeles
)

// Respuesta del endpoint /verificar_tiempo_real con todas las personas del frame
data class TiempoRealResponse(
    val personas: List<PersonaTiempoReal>
)

// Define los endpoints del servidor usando anotaciones de Retrofit
interface HuggingFaceApi {

    // Envia 5 fotos y un nombre; el servidor genera el perfil facial de la persona
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

    // Envia un frame y devuelve todas las personas detectadas con sus bboxes
    @Multipart
    @POST("verificar_tiempo_real")
    suspend fun verificarTiempoReal(
        @Part foto: MultipartBody.Part
    ): Response<TiempoRealResponse>
}

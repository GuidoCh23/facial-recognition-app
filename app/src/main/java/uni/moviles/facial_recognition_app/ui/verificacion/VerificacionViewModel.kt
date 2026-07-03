package uni.moviles.facial_recognition_app.ui.verificacion

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import uni.moviles.facial_recognition_app.api.ApiClient
import java.io.File

// Representa todos los datos de la pantalla en un solo objeto
data class VerificacionState(
    val foto: Uri? = null,
    val cargando: Boolean = false,
    val reconocido: Boolean? = null,
    val nombre: String? = null,
    val error: String? = null
)

class VerificacionViewModel : ViewModel() {

    // _state es privado: solo el ViewModel puede modificarlo
    // state es publico: la pantalla lo lee con collectAsState()
    private val _state = MutableStateFlow(VerificacionState())
    val state: StateFlow<VerificacionState> = _state

    fun setFoto(uri: Uri) {
        _state.value = VerificacionState(foto = uri)
    }

    fun verificar(context: Context) {
        val uri = _state.value.foto ?: return

        // viewModelScope.launch: ejecuta la peticion en segundo plano sin bloquear la UI
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                // Copia la imagen a un archivo distinto para evitar leer y escribir el mismo archivo
                val archivo = File(context.cacheDir, "verificar_envio.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    archivo.outputStream().use { output -> input.copyTo(output) }
                }
                val parte = MultipartBody.Part.createFormData(
                    "foto", archivo.name,
                    archivo.asRequestBody("image/jpeg".toMediaType())
                )

                val respuesta = ApiClient.api.verificar(parte)
                if (respuesta.isSuccessful) {
                    val body = respuesta.body()
                    _state.value = _state.value.copy(
                        reconocido = body?.match,
                        nombre = body?.nombre,
                        cargando = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = parsearError(respuesta.errorBody()?.string()),
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, cargando = false)
            }
        }
    }

    // Traduce el error del servidor a un mensaje entendible para el usuario
    private fun parsearError(errorBody: String?): String {
        if (errorBody == null) return "Error desconocido"
        val detail = try {
            org.json.JSONObject(errorBody).optString("detail", errorBody)
        } catch (_: Exception) {
            errorBody
        }
        return when {
            detail.contains("varios rostros", ignoreCase = true) ||
            detail.contains("multiples", ignoreCase = true) ->
                "Se detectaron varios rostros. Asegurate de que solo haya una persona en la imagen."
            detail.contains("ningun rostro", ignoreCase = true) ||
            detail.contains("no se detect", ignoreCase = true) ->
                "No se detecto ningun rostro. Asegurate de que tu cara sea visible y este bien iluminada."
            else -> "Error del servidor: $detail"
        }
    }
}

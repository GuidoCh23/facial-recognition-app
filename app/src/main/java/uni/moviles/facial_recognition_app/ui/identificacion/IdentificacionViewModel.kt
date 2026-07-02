package uni.moviles.facial_recognition_app.ui.identificacion

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

data class IdentificacionState(
    val foto: Uri? = null,
    val cargando: Boolean = false,
    val reconocido: Boolean? = null,
    val nombre: String? = null,
    val error: String? = null
)

class IdentificacionViewModel : ViewModel() {

    private val _state = MutableStateFlow(IdentificacionState())
    val state: StateFlow<IdentificacionState> = _state

    fun setFoto(uri: Uri) {
        _state.value = IdentificacionState(foto = uri)
    }

    fun identificar(context: Context) {
        val uri = _state.value.foto ?: return

        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                // Copia la imagen a un archivo distinto para evitar leer y escribir el mismo archivo
                val archivo = File(context.cacheDir, "verificar_envio.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    archivo.outputStream().use { output -> input.copyTo(output) }
                }
                val fotoPart = MultipartBody.Part.createFormData(
                    "foto", archivo.name,
                    archivo.asRequestBody("image/jpeg".toMediaType())
                )

                val respuesta = ApiClient.api.verificar(fotoPart)
                if (respuesta.isSuccessful) {
                    val body = respuesta.body()
                    _state.value = _state.value.copy(
                        reconocido = body?.match,
                        nombre = body?.nombre,
                        cargando = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "Error del servidor: ${respuesta.code()}",
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, cargando = false)
            }
        }
    }
}

package uni.moviles.facial_recognition_app.ui.registro

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
import okhttp3.RequestBody.Companion.toRequestBody
import uni.moviles.facial_recognition_app.api.ApiClient
import java.io.File

// Representa todos los datos de la pantalla en un solo objeto
data class RegistroState(
    val nombre: String = "",
    val fotos: List<Uri> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: Boolean = false
)

class RegistroViewModel : ViewModel() {

    // _state es privado: solo el ViewModel puede modificarlo
    // state es publico: la pantalla lo lee con collectAsState()
    private val _state = MutableStateFlow(RegistroState())
    val state: StateFlow<RegistroState> = _state

    fun setNombre(nombre: String) {
        // copy() crea una copia del estado actual cambiando solo el campo indicado
        _state.value = _state.value.copy(nombre = nombre)
    }

    fun agregarFoto(uri: Uri) {
        _state.value = _state.value.copy(fotos = _state.value.fotos + uri)
    }

    fun registrar(context: Context) {
        val estado = _state.value
        if (estado.nombre.isBlank() || estado.fotos.size < 5) return

        // viewModelScope.launch: ejecuta la peticion en segundo plano sin bloquear la UI
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                val nombreBody = estado.nombre.toRequestBody("text/plain".toMediaType())

                // Convierte cada URI en una parte multipart para enviarla al servidor
                val partes = estado.fotos.mapIndexed { i, uri ->
                    val archivo = copiarUriACacheDir(context, uri, "foto_$i.jpg")
                    val body = archivo.asRequestBody("image/jpeg".toMediaType())
                    MultipartBody.Part.createFormData("fotos", archivo.name, body)
                }

                val respuesta = ApiClient.api.registrar(nombreBody, partes)
                if (respuesta.isSuccessful) {
                    _state.value = _state.value.copy(exitoso = true, cargando = false)
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

    // Copia la imagen al directorio de cache para poder leerla como File
    // (las URIs de galeria/camara no se pueden leer directamente con File)
    private fun copiarUriACacheDir(context: Context, uri: Uri, nombre: String): File {
        val archivo = File(context.cacheDir, nombre)
        context.contentResolver.openInputStream(uri)?.use { input ->
            archivo.outputStream().use { output -> input.copyTo(output) }
        }
        return archivo
    }
}

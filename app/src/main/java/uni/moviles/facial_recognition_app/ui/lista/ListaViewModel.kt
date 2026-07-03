package uni.moviles.facial_recognition_app.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uni.moviles.facial_recognition_app.api.ApiClient

data class ListaState(
    val nombres: List<String> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class ListaViewModel : ViewModel() {

    private val _state = MutableStateFlow(ListaState())
    val state: StateFlow<ListaState> = _state

    fun cargarPersonas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(cargando = true, error = null)
            try {
                val respuesta = ApiClient.api.obtenerUsuarios()
                if (respuesta.isSuccessful) {
                    _state.value = _state.value.copy(
                        nombres = respuesta.body()?.usuarios ?: emptyList(),
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

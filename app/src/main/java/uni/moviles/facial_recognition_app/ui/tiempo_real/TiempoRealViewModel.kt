package uni.moviles.facial_recognition_app.ui.tiempo_real

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import uni.moviles.facial_recognition_app.api.ApiClient
import uni.moviles.facial_recognition_app.api.PersonaTiempoReal
import java.io.File

class TiempoRealViewModel : ViewModel() {

    // Frame procesado listo para mostrar en pantalla
    val displayBitmap = MutableStateFlow<Bitmap?>(null)

    // Paints reutilizables para no crear objetos nuevos en cada frame
    private val paintRecuadro = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 4f }
    private val paintTexto = Paint().apply { textSize = 42f; isFakeBoldText = true; setShadowLayer(6f, 2f, 2f, android.graphics.Color.BLACK) }

    // Envia el frame al backend, espera la respuesta, dibuja los bbox y actualiza la pantalla
    suspend fun procesarFrame(frame: Bitmap, jpegBytes: ByteArray, context: Context) {
        val personas = verificar(jpegBytes, context)
        dibujar(frame, personas)
        displayBitmap.value = frame
    }

    private suspend fun verificar(jpegBytes: ByteArray, context: Context): List<PersonaTiempoReal> {
        return try {
            val archivo = File(context.cacheDir, "frame_tiempo_real.jpg")
            archivo.writeBytes(jpegBytes)
            val parte = MultipartBody.Part.createFormData(
                "foto", archivo.name,
                archivo.asRequestBody("image/jpeg".toMediaType())
            )
            val respuesta = ApiClient.api.verificarTiempoReal(parte)
            if (respuesta.isSuccessful) respuesta.body()?.personas ?: emptyList()
            else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // Dibuja directamente sobre el bitmap: recuadro verde si reconocido, rojo si desconocido
    private fun dibujar(bitmap: Bitmap, personas: List<PersonaTiempoReal>) {
        val canvas = Canvas(bitmap)
        personas.forEach { persona ->
            val bbox = persona.bbox
            if (bbox.size < 4) return@forEach

            val color = if (persona.match) android.graphics.Color.GREEN else android.graphics.Color.RED
            paintRecuadro.setColor(color)
            paintTexto.setColor(color)

            canvas.drawRect(bbox[0], bbox[1], bbox[2], bbox[3], paintRecuadro)

            val etiqueta = if (persona.match)
                "${persona.nombre} (${(persona.score * 100).toInt()}%)"
            else
                "Desconocido"
            canvas.drawText(etiqueta, bbox[0] + 4f, maxOf(bbox[1] - 10f, 50f), paintTexto)
        }
    }
}

package uni.moviles.facial_recognition_app.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun crearUriTemporal(context: Context, nombre: String): Uri {
    val archivo = File(context.cacheDir, nombre)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

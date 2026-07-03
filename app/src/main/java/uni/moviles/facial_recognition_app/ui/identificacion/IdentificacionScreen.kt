package uni.moviles.facial_recognition_app.ui.identificacion

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import uni.moviles.facial_recognition_app.utils.crearUriTemporal

@Composable
fun IdentificacionScreen(onVolver: () -> Unit, vm: IdentificacionViewModel = viewModel()) {
    val estado by vm.state.collectAsState()
    val context = LocalContext.current
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }

    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { guardada ->
        if (guardada) uriPendiente?.let { vm.setFoto(it) }
    }

    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.setFoto(it) }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            // Nombre unico para que Coil no muestre la foto anterior desde cache
            val uri = crearUriTemporal(context, "verificar_${System.currentTimeMillis()}.jpg")
            uriPendiente = uri
            camaraLauncher.launch(uri)
        }
    }

    fun tomarFoto() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            val uri = crearUriTemporal(context, "verificar_${System.currentTimeMillis()}.jpg")
            uriPendiente = uri
            camaraLauncher.launch(uri)
        } else {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verificar persona", style = MaterialTheme.typography.headlineMedium)

        // Foto seleccionada, borde verde si está registrado, rojo si es desconocido
        estado.foto?.let { uri ->
            val colorBorde = when (estado.reconocido) {
                true -> Color.Green
                false -> Color.Red
                null -> null
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .then(
                        if (colorBorde != null) Modifier.border(4.dp, colorBorde)
                        else Modifier
                    )
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Resultado
        when (estado.reconocido) {
            true -> Text(
                estado.nombre ?: "",
                style = MaterialTheme.typography.headlineSmall
            )
            false -> Text(
                "Desconocido, usuario no registrado",
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
            null -> {}
        }

        if (estado.cargando) {
            CircularProgressIndicator()
        } else {
            // Botones de cámara y galería siempre visibles (salvo mientras carga)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { tomarFoto() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (estado.foto == null) "Cámara" else "Otra foto")
                }
                OutlinedButton(
                    onClick = { galeriaLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galería")
                }
            }

            // Botón verificar solo cuando hay foto y no hay resultado aún
            if (estado.foto != null && estado.reconocido == null) {
                Button(
                    onClick = { vm.identificar(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verificar")
                }
            }
        }

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = onVolver) { Text("Volver") }
    }
}

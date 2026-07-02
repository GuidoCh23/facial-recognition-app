package uni.moviles.facial_recognition_app.ui.registro

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import uni.moviles.facial_recognition_app.utils.crearUriTemporal

@Composable
fun RegistroScreen(onVolver: () -> Unit, vm: RegistroViewModel = viewModel()) {
    val estado by vm.state.collectAsState()
    val context = LocalContext.current
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }

    // Lanzador de la cámara del sistema
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { guardada ->
        if (guardada) uriPendiente?.let { vm.agregarFoto(it) }
    }

    // Lanzador de la galería
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.agregarFoto(it) }
    }

    // Solicitud de permiso de cámara; al concederse abre la cámara de inmediato
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = crearUriTemporal(context, "registro_${estado.fotos.size}.jpg")
            uriPendiente = uri
            camaraLauncher.launch(uri)
        }
    }

    fun tomarFoto() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            val uri = crearUriTemporal(context, "registro_${estado.fotos.size}.jpg")
            uriPendiente = uri
            camaraLauncher.launch(uri)
        } else {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Pantalla de éxito
    if (estado.exitoso) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("¡Persona registrada!", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = onVolver) { Text("Volver") }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Registrar persona", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = { vm.setNombre(it) },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            "Agrega 5 fotos de la persona a registrar. Procura variar la posición y la iluminación para mejores resultados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text("Fotos: ${estado.fotos.size} de 5")

        // Miniaturas de las fotos ya agregadas
        if (estado.fotos.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estado.fotos) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Botones para agregar la siguiente foto
        if (estado.fotos.size < 5) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { tomarFoto() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cámara")
                }
                OutlinedButton(
                    onClick = { galeriaLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galería")
                }
            }
        }

        if (estado.fotos.size == 5 && estado.nombre.isNotBlank()) {
            Button(
                onClick = { vm.registrar(context) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.cargando
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Registrar")
                }
            }
        }

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = onVolver) { Text("Cancelar") }
    }
}

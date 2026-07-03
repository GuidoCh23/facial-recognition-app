package uni.moviles.facial_recognition_app.ui.tiempo_real

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
fun TiempoRealScreen(onVolver: () -> Unit, vm: TiempoRealViewModel = viewModel()) {
    // collectAsState(): convierte el StateFlow en un State que Compose puede observar
    val bitmap by vm.displayBitmap.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Verifica si ya se tiene permiso de camara al entrar a la pantalla
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    // Hilo dedicado para el analisis de frames (no bloquea el hilo principal)
    val executor = remember { Executors.newSingleThreadExecutor() }
    // DisposableEffect: apaga el executor cuando la pantalla se cierra
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    // Pide el permiso la primera vez que se abre la pantalla
    LaunchedEffect(Unit) {
        if (!tienePermiso) permisoLauncher.launch(Manifest.permission.CAMERA)
    }

    // Muestra un mensaje si el usuario no concedio el permiso
    if (!tienePermiso) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Se necesita permiso de camara")
                TextButton(onClick = onVolver) { Text("Volver") }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Muestra el frame procesado con los bboxes dibujados; CircularProgressIndicator mientras llega el primero
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Vista invisible de 1dp: solo mantiene CameraX activo sin mostrar preview
        // (el frame procesado se muestra con el Image de arriba, no con un Preview de CameraX)
        AndroidView(
            factory = { ctx ->
                android.view.View(ctx).also {
                    // ProcessCameraProvider conecta CameraX al ciclo de vida de la pantalla
                    val futuro = ProcessCameraProvider.getInstance(ctx)
                    futuro.addListener({
                        val proveedor = futuro.get()

                        // ImageAnalysis: captura frames para procesarlos (no los muestra en pantalla)
                        val analisis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(640, 480))
                            // STRATEGY_KEEP_ONLY_LATEST: descarta frames viejos si el backend es lento
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        analisis.setAnalyzer(executor) { imageProxy ->
                            val rotacion = imageProxy.imageInfo.rotationDegrees
                            val raw = imageProxy.toBitmap()
                            imageProxy.close() // liberar el frame cuanto antes

                            // Rota el bitmap si la camara entrega el frame girado
                            val frame = if (rotacion != 0) {
                                val matrix = Matrix().apply { postRotate(rotacion.toFloat()) }
                                Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                            } else raw

                            // Comprime a JPEG para reducir el tamano del dato que se envia al servidor
                            val jpegBytes = ByteArrayOutputStream()
                                .also { frame.compress(Bitmap.CompressFormat.JPEG, 80, it) }
                                .toByteArray()

                            // runBlocking: espera la respuesta del backend antes de procesar el siguiente frame
                            // Esto asegura que los bboxes mostrados corresponden al frame enviado
                            runBlocking { vm.procesarFrame(frame, jpegBytes, ctx) }
                        }

                        proveedor.unbindAll()
                        proveedor.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            analisis
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.size(1.dp)
        )

        TextButton(
            onClick = onVolver,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Text("Volver")
        }
    }
}

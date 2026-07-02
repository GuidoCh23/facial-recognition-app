package uni.moviles.facial_recognition_app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onRegistrar: () -> Unit, onVerificar: () -> Unit, onLista: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reconocimiento Facial", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRegistrar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar persona")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onVerificar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verificar persona")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onLista,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver personas registradas")
        }
    }
}

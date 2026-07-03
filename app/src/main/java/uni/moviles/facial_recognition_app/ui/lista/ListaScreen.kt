package uni.moviles.facial_recognition_app.ui.lista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ListaScreen(onVolver: () -> Unit, vm: ListaViewModel = viewModel()) {
    val estado by vm.state.collectAsState()

    // LaunchedEffect(Unit): se ejecuta una sola vez cuando la pantalla aparece
    LaunchedEffect(Unit) { vm.cargarPersonas() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Personas registradas", style = MaterialTheme.typography.headlineMedium)

        when {
            estado.cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            estado.error != null -> {
                Text(estado.error!!, color = MaterialTheme.colorScheme.error)
            }
            estado.nombres.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay personas registradas aún.")
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(estado.nombres) { nombre ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = nombre,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        TextButton(onClick = onVolver) { Text("Volver") }
    }
}

package com.example.miapp

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.miapp.ui.theme.MiAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegistroAccidenteScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroAccidenteScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // -------- FORMULARIO --------
    val tipos = listOf("Choque", "Colisión", "Atropello")
    var tipo by remember { mutableStateOf(tipos[0]) }
    var expanded by remember { mutableStateOf(false) }

    var fecha by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var nombreConductor by remember { mutableStateOf("") }
    var cedulaConductor by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    var fotos by remember { mutableStateOf(listOf<String>()) }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }

    var mensaje by remember { mutableStateOf("") }

    // -------- CÁMARA --------
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getStringExtra("photo_uri")
            if (!uri.isNullOrEmpty()) {
                fotos = fotos + uri
                mensaje = "📸 Foto añadida"
            }
        }
    }

    // -------- UBICACIÓN --------
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            lat = result.data?.getDoubleExtra("lat", 0.0)
            lon = result.data?.getDoubleExtra("lon", 0.0)
            mensaje = "📍 Ubicación obtenida"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text("Registro de Accidentes", style = MaterialTheme.typography.headlineSmall)

        // TIPO
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = tipo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de accidente") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                tipos.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            tipo = it
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha del siniestro") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = matricula,
            onValueChange = { matricula = it },
            label = { Text("Matrícula del vehículo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombreConductor,
            onValueChange = { nombreConductor = it },
            label = { Text("Nombre del conductor") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = cedulaConductor,
            onValueChange = { cedulaConductor = it.filter { c -> c.isDigit() } },
            label = { Text("Cédula del conductor") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // BOTONES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    cameraLauncher.launch(
                        Intent(context, CameraActivity::class.java)
                    )
                }
            ) {
                Text("Tomar Foto")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    locationLauncher.launch(
                        Intent(context, LocationActivity::class.java)
                    )
                }
            ) {
                Text("Obtener GPS")
            }
        }

        // MOSTRAR FOTOS
        if (fotos.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fotos) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Foto",
                        modifier = Modifier.size(90.dp)
                    )
                }
            }
        }

        Text("Ubicación: ${lat ?: "—"}, ${lon ?: "—"}")

        // GUARDAR
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (
                    fecha.isBlank() ||
                    matricula.isBlank() ||
                    nombreConductor.isBlank() ||
                    cedulaConductor.isBlank()
                ) {
                    mensaje = "❌ Completa todos los campos obligatorios"
                    return@Button
                }

                vibrar5Segundos(context)
                mensaje = "✅ Accidente registrado (vibración 5s)"

                // LIMPIAR
                tipo = tipos[0]
                fecha = ""
                matricula = ""
                nombreConductor = ""
                cedulaConductor = ""
                observaciones = ""
                fotos = emptyList()
                lat = null
                lon = null
            }
        ) {
            Text("Guardar")
        }

        if (mensaje.isNotEmpty()) {
            AssistChip(
                onClick = {},
                label = { Text(mensaje) }
            )
        }
    }
}

private fun vibrar5Segundos(context: android.content.Context) {
    val tiempo = 5000L
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(VibratorManager::class.java)
        vm.defaultVibrator.vibrate(
            VibrationEffect.createOneShot(tiempo, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        @Suppress("DEPRECATION")
        val v = context.getSystemService(Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createOneShot(tiempo, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(tiempo)
        }
    }
}

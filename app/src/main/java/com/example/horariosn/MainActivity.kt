package com.example.horariosn

import android.content.Context

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import com.example.horariosn.ui.theme.HorariosnTheme

import com.google.firebase.Firebase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HorariosnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background

                ){
                    TimbreConfigScreen()
                }
            }
        }
    }
}
//PsN9WD50yysiwRSNSKsILzsLa6PUOASA8TMj1MDi
//https://horariosn-807e7-default-rtdb.firebaseio.com/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimbreConfigScreen (){
    val context = LocalContext.current
    val database= Firebase.database.reference.child("timbre_escolar")

    //estados
    var esModoManual by remember { mutableStateOf(false) }
    var timbreManualActivo by remember { mutableStateOf(false)}
    val horasEntrada= remember { mutableStateListOf(*Array(11) {""}) }
    val horasSalida= remember { mutableStateListOf(*Array(11) {""}) }

    val horarioDefecto = listOf(

        "1) 06:15 am - 07:10 am",
        "2) 07:10 am - 08:05 am",
        "3) 08:05 am - 09:00 am",
        "☕ Descanso) 09:00 am - 09:30 am",
        "4) 09:30 am - 10:25 am",
        "5) 10:25 am - 11:20 am",
        "6) 11:20 am - 12:15 pm",
        "🍲 ALMUERZO) 12:15 pm - 01:30 pm",
        "7) 01:30 pm - 02:15 pm",
        "8) 02:15 pm - 03:00 pm",
        "☕ Descanso) 03:00 pm - 03:45 pm",
        "9) 04:05 pm - 04:50 pm",
        "10) 04:50 pm - 05:35 pm"

    )

    //Sincronizacion con Firebase
    DisposableEffect(Unit) {
        val listener= object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //1.Lee el modo o defecto
                val modo= snapshot.child("modo").getValue( String::class.java)?:"defecto"
                esModoManual=(modo=="manual")
                //2.Leer si el boton de pánico está activo
                val manualTrigger= snapshot.child("manual_trigger").getValue(Int::class.java)?:0
                timbreManualActivo= (manualTrigger==1)
                //3.leer los horarios de entrada y salida de la nube en las cajas de texto
                val horariosNube = snapshot.child("horarios_manuales")
                if (horariosNube.exists()){
                    for (i in 0..10){
                         horasEntrada[i]=horariosNube.child("entrada_${i+1}").getValue(String::class.java)?:""
                         horasSalida[i]=horariosNube.child("salida_${i+1}").getValue(String::class.java)?:""
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(listener)
        onDispose {
            database.removeEventListener(listener)
        }
    }

    //vibrador
    LaunchedEffect(timbreManualActivo){
        if (timbreManualActivo){
            vibrar(context, 600)
        }


    }

    Scaffold(
      topBar = {
          TopAppBar(
              title = { Text("Timbre I.E Carlos Ramírez París", fontSize = 18.sp)},
              colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  titleContentColor = MaterialTheme.colorScheme.onPrimary,
              )
          )
      }
    ) {
        padding ->
        Column(
            modifier = Modifier
                 .fillMaxSize()
                .padding(padding)
                .padding(16.dp)

        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text=if (esModoManual) "MODO ACTIVO: MANUAL" else "MODO ACTIVO: POR DEFECTO",
                            fontWeight = FontWeight.Bold,
                            color=if (esModoManual) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                        Text("Activa para personalizar el horario",fontSize = 12.sp)
                    }
                    Switch(
                        checked = esModoManual,
                        onCheckedChange = {isChecked->
                            esModoManual=isChecked
                            database.child("modo").setValue(if (isChecked) "manual" else "defecto")

                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val buttonColor by animateColorAsState(
                targetValue = if (timbreManualActivo) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = 400)
            )

            Button(
                onClick = {
                    val nuevoEstado=  if (timbreManualActivo) 0 else 1
                    database.child("manual_trigger").setValue(nuevoEstado)

                    if (nuevoEstado==1){
                        Toast.makeText(context, "El timbre ha sido activado", Toast.LENGTH_SHORT).show()
                    }

                   },
                  modifier = Modifier.fillMaxWidth().height(50.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = buttonColor)


            ) {
                Icon(
                    imageVector = if (timbreManualActivo) Icons.Default.Notifications else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (timbreManualActivo) "TIMBRE SONANDO (Oprime para apagar)" else " HACER SONAR EL TIMBRE AHORA",
                    fontWeight =  FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Horario de Clases", fontWeight = FontWeight.Bold)
            Spacer(modifier =Modifier.height(8.dp))

            //Vista por defecto

            if (!esModoManual){
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {
                    items(horarioDefecto){hora->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text(text = hora, modifier = Modifier.padding(16.dp,), fontSize = 16.sp)
                        }
                    }
                }
            }else{
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    itemsIndexed(horasEntrada){index, _->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index+1}.", fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                            OutlinedTextField(
                                value = horasEntrada[index],
                                onValueChange = { horasEntrada[index]=it},
                                label = { Text("Entrada")},
                                modifier = Modifier.weight(1f),
                                placeholder = {Text("HH:MM")},
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = horasSalida[index],
                                onValueChange = { horasSalida[index]=it},
                                label = { Text("Salida")},
                                modifier = Modifier.weight(1f),
                                placeholder = {Text("HH:MM")},
                                singleLine = true
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val mapHorarios=mutableMapOf<String, String>()
                        for (i in 0..10){
                            mapHorarios["entrada_${i+1}"]=horasEntrada[i]
                            mapHorarios["salida_${i+1}"]=horasSalida[i]
                        }
                        database.child("horarios_manuales").setValue(mapHorarios)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Horario guardado en la nube", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp) ,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ACTUALIZAR HORARIO MANUAL EN EL TIMBRE")

                }
            }
        }
    }
}







private fun vibrar(context: Context, ms: Long) {
    val vibrator =context.getSystemService(Context.VIBRATOR_SERVICE)as? Vibrator
    if (vibrator?.hasVibrator()==true){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else{
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }
}



package com.example.roomcomplete

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    val workouts by viewModel.workouts.observeAsState(emptyList())
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var music by remember { mutableStateOf("") }
    var weights by remember { mutableStateOf("") }
    var machines by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        @Composable
        fun input(label: String, value: String, onValueChange: (String) -> Unit) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        input("Title", title) { title = it }
        input("Note", text) { text = it }
        input("Activity", activity) { activity = it }
        input("Mood", mood) { mood = it }
        input("Music", music) { music = it }
        input("Weights", weights) { weights = it }
        input("Machines", machines) { machines = it }
        input("Image Path", imagePath) { imagePath = it }

        Row {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addWorkout(
                            title, text, activity, mood, music, weights, machines, imagePath
                        )
                        title = ""; text = ""; activity = ""; mood = ""
                        music = ""; weights = ""; machines = ""; imagePath = ""
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("Add Workout")
            }

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        exportToCSV(context, viewModel)
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("Export CSV")
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(workouts) { workout ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Title: ${workout.title}")
                        Text("Note: ${workout.text}")
                        Text("Activity: ${workout.activity}")
                        Text("Mood: ${workout.mood}")
                        Text("Music: ${workout.music}")
                        Text("Weights: ${workout.weights}")
                        Text("Machines: ${workout.machines}")
                        workout.imagePath?.let { Text("Image Path: $it") }
                    }
                }
            }
        }
    }
}

suspend fun exportToCSV(context: Context, viewModel: WorkoutViewModel) {
    val workouts = viewModel.getAllWorkouts()
    val file = File(context.filesDir, "gym_data.csv")
    val writer = FileWriter(file)

    writer.append("Title,Note,Activity,Mood,Music,Weights,Machines,ImagePath\n")
    for (w in workouts) {
        writer.append(
            "\"${w.title}\",\"${w.text}\",\"${w.activity}\",\"${w.mood}\",\"${w.music}\",\"${w.weights}\",\"${w.machines}\",\"${w.imagePath ?: ""}\"\n"
        )
    }

    writer.flush()
    writer.close()

    withContext(Dispatchers.Main) {
        Toast.makeText(context, "CSV Exported: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}

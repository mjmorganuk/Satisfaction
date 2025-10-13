package com.example.satisfactionsurvey

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.mockk

// --------------------- MainActivity + UI ---------------------
class MainActivity : ComponentActivity() {

    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseRef = FirebaseDatabase.getInstance().getReference("Satisfaction")

        setContent {
            MaterialTheme {

                // --- Start of Navigation Setup ---
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "survey"
                ) {
                    composable("survey") {
                        SatisfactionSurvey(
                            navController = navController,
                            fbRef = this@MainActivity.firebaseRef,
                            onFaceSelected = {
                                onFaceSelected(it)
                            }
                        )
                    }
                    // --- End of Navigation Setup ---
                }
            }
        }
    }


    fun onFaceSelected(selection: String) {
        val context = this
        Toast.makeText(context, "You selected: $selection", Toast.LENGTH_SHORT).show()
    }

}


/**
 * A composable function that displays three large emoticon faces (sad, neutral, happy)
 * for a user satisfaction survey. It allows for a callback to be triggered when a face is selected.
 *
 * @param onFaceSelected A lambda function that is invoked with the selected satisfaction level
 *                       (e.g., "Sad", "Neutral", "Happy") when an icon is clicked.
 */
@Composable
fun SatisfactionSurvey(
    navController: NavController,
    fbRef: DatabaseReference?,  // nullable because Preview can't instantiate a real FirebaseDatabase
    onFaceSelected: (String) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sad face
        IconButton(
            onClick = {
                val choice = "Sad"
                fbRef?.setValue(choice)
                onFaceSelected(choice)
            },
            modifier = Modifier.size(80.dp)
        ) {
            Text(text = "😞", fontSize = 50.sp)
        }

        // Neutral face
        IconButton(
            onClick = {
                val choice = "Neutral"
                fbRef?.setValue(choice)
                onFaceSelected(choice)
            },
            modifier = Modifier.size(80.dp)
        ) {
            Text(text = "😐", fontSize = 50.sp)
        }

        // Happy face
        IconButton(
            onClick = {
                val choice = "Happy"
                fbRef?.setValue(choice)
                onFaceSelected(choice)
            },
            modifier = Modifier.size(80.dp)
        ) {
            Text(text = "😊", fontSize = 50.sp)
        }
    }
}


fun onSelected(selection: String) {
// only exists so the the Preview can call it

}

@Preview(showBackground = true)
@Composable
fun SurveyPreview() {

    // --- Create a Mock DatabaseReference using MockK ---
    // Use 'relaxed = true' here to simplify mocking. It provides default answers (like nulls or empty tasks)
    // for any calls we don't explicitly define, which can prevent some crashes.
    val fakeDbRef: DatabaseReference = mockk(relaxed = true)


    // 3. Call your NoteApp composable within a theme

    MaterialTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "survey"
        ) {
            composable("survey") {
                SatisfactionSurvey(
                    navController = navController,
                    fbRef = fakeDbRef, // <-- Pass the fake reference
                    onFaceSelected = {
                        onSelected(it) // <-- Pass the empty function
                    }
                )
            }
        }
    }

}



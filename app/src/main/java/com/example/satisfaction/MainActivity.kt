// build.gradle (Module: app)
// Make sure you have these dependencies:
//
// implementation "androidx.room:room-runtime:2.6.1"
// kapt "androidx.room:room-compiler:2.6.1"
// implementation "androidx.room:room-ktx:2.6.1"
// implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5"
// implementation "androidx.activity:activity-compose:1.9.3"
// implementation "androidx.compose.ui:ui:1.7.2"
// implementation "androidx.compose.material3:material3:1.3.0"
// implementation "androidx.lifecycle:lifecycle-runtime-compose:2.8.5"

package com.example.satisfaction

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.satisfaction.databinding.ActivityMainBinding
import com.example.satisfaction.models.Contacts
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --------------------- Entity ---------------------
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String
)

// --------------------- DAO ---------------------
@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes")
    fun getAll(): Flow<List<Note>>
}

// --------------------- Database ---------------------
@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

// --------------------- Repository ---------------------
open class NoteRepository(private val dao: NoteDao) {
    open val notes = dao.getAll()
    suspend fun insert(note: Note) = dao.insert(note)
    suspend fun update(note: Note) = dao.update(note)
    suspend fun delete(note: Note) = dao.delete(note)
}

// --------------------- ViewModel ---------------------
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    val notes = repository.notes.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    fun addNote(text: String) = viewModelScope.launch {
        repository.insert(Note(text = text))
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note.copy(text = note.text + " (is updated)"))
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

}

// --------------------- ViewModel Factory ---------------------
class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(repository) as T
    }
}

// --------------------- MainActivity + UI ---------------------
class MainActivity : ComponentActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val dao = AppDatabase.getDatabase(application).noteDao()
        val repository = NoteRepository(dao)
        val factory = NoteViewModelFactory(repository)
        val noteViewModel: NoteViewModel by viewModels { factory }


        firebaseRef = FirebaseDatabase.getInstance().getReference("Contacts")

        setContent {
            MaterialTheme {
//                NoteApp(noteViewModel, this.firebaseRef)
                // --- Start of Navigation Setup ---
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "note_app"
                ) {
                    composable("note_app") {
                        NoteApp(
                            viewModel = noteViewModel,
                            fbRef = this@MainActivity.firebaseRef,
                            navController = navController // Pass NavController
                        )
                    }
                    composable("contacts") {
                        ContactForm(
                            navController = navController,
                            fbRef = this@MainActivity.firebaseRef
                        ) // Pass NavController
                    }
                    composable("contact_list") {
                        ContactList(
                            navController = navController,
                            fbRef = this@MainActivity.firebaseRef
                        )
                    }

                }
                // --- End of Navigation Setup ---
            }
        }
    }
}

// NEW: SecondScreen Composable
@Composable
fun ContactForm(
    navController: NavController,
    fbRef: DatabaseReference?  // nullable because Preview can't instantiate a real FirebaseDatabase
) {
    var otlname by remember { mutableStateOf("") }
    var otlemail by remember { mutableStateOf("") }
    var otlphone by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Contacts Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // name input field
        OutlinedTextField(
            value = otlname,
            onValueChange = { otlname = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // email input field
        OutlinedTextField(
            value = otlemail,
            onValueChange = { otlemail = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // phone input field
        OutlinedTextField(
            value = otlphone,
            onValueChange = { otlphone = it },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Save the data to the Repository
                val contactId = fbRef?.push()?.key!!
                val contacts =
                    Contacts(id = contactId, name = otlname, email = otlemail, phone = otlphone)

                fbRef?.child(contactId)?.setValue(contacts)
                    ?.addOnCompleteListener {
                        Toast.makeText(context, "Contact Saved", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(context, "Failed to save contact", Toast.LENGTH_SHORT).show()
                    }

            },
            // Disable the button if Firebase is not available (i.e., in a preview)
            enabled = fbRef != null
        ) {
            Text("Save Contact")
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            // Navigate back to the NoteApp screen
            navController.popBackStack()
        }) {
            Text("Go Back to Notes")
        }
    }
}


@Composable
fun ContactList(
    navController: NavController,
    fbRef: DatabaseReference?
) {

    var contacts by remember { mutableStateOf<List<Contacts>>(emptyList()) }
    val context = LocalContext.current

// Use DisposableEffect to manage the listener's lifecycle
    DisposableEffect(fbRef) { // Re-run if fbRef changes
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newContacts = mutableListOf<Contacts>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { childSnapshot ->
                        childSnapshot.getValue(Contacts::class.java)?.let { contact ->
                            newContacts.add(contact)
                        }
                    }
                }
                contacts = newContacts
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to load contacts: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // Add the listener when the effect starts
        fbRef?.addValueEventListener(listener)

        // The onDispose block is called when the composable leaves the screen
        onDispose {
            // IMPORTANT: Remove the listener to prevent memory leaks and crashes
            fbRef?.removeEventListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Contact List", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (contacts.isEmpty()) {
            Text("No contacts")
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items = contacts,
                      key = { contact -> contact.id } // Use the unique ID from Firebase
                ) { contact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Email: ${contact.email}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Phone: ${contact.phone}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // Navigate back to the NoteApp screen
            navController.popBackStack()
        }) {
            Text("Go Back to Notes")
        }
    }
}

@Composable
fun NoteApp(
    viewModel: NoteViewModel = viewModel(),
    fbRef: DatabaseReference?,  // nullable because Preview can't instantiate a real FirebaseDatabase
    navController: NavController? // Add NavController, nullable for Preview
) {
    val notes by viewModel.notes.collectAsState()
    var newText by remember { mutableStateOf("") }
    val context = LocalContext.current // 1. Get the context

    Column(
        modifier = Modifier
            .padding(16.dp)
    )
    {
        // Add a button to navigate to the second screen
        Button(
            onClick = { navController?.navigate("contacts") },
            enabled = navController != null // Disable in preview
        ) {
            Text("Edit Contacts")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController?.navigate("contact_list") },
            enabled = navController != null // Disable in preview
        ) {
            Text("View Contacts")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = newText,
            onValueChange = { newText = it },
            label = { Text("Enter note") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

// write text to Firebase Database
        Button(
            onClick = {
                if (newText.isNotBlank()) {
                    fbRef?.setValue(newText)

                        ?.addOnSuccessListener {
                            // 2. Show Toast on success
                            Toast.makeText(context, "Value updated in Firebase", Toast.LENGTH_SHORT)
                                .show()
                        }
                        ?.addOnFailureListener {
                            // Optional: Show a different message on failure
                            Toast.makeText(context, "Failed to update value", Toast.LENGTH_SHORT)
                                .show()
                        }
                    newText = ""
                }
            },
// Disable the button if Firebase is not available (i.e., in a preview)
            enabled = fbRef != null
        ) {
            Text("Update DB")
        }

        Button(onClick = {
            if (newText.isNotBlank()) {
                viewModel.addNote(newText)
                newText = ""
            }
        }) {
            Text("Add Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(notes.size) { index ->
                val thisNote = notes[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(thisNote.text)
                    Row {
                        Button(onClick = { viewModel.updateNote(thisNote) }) {
                            Text("Upd")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(onClick = { viewModel.deleteNote(thisNote) }) {
                            Text("Del")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteAppPreview() {
    // 1. Create a FAKE repository with hardcoded data for the preview
    val fakeRepository = object : NoteRepository(dao = FakeNoteDao()) {
        // Override the 'notes' property to return a hardcoded list for the preview
        override val notes: Flow<List<Note>> = flowOf(
            listOf(
                Note(id = 1, text = "first note"),
                Note(id = 2, text = "second longer note"),
                Note(id = 3, text = "third in the list"),
                Note(id = 4, text = "fourth note")
            )
        )
    }

    // 2. Create a real ViewModel instance, but give it the fake repository
    val fakeViewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(fakeRepository) as T
        }
    }

    // 3. Call your NoteApp composable within a theme

    MaterialTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "note_app"
        ) {
            composable("note_app") {
                NoteApp(
                    viewModel = viewModel(factory = fakeViewModelFactory),
                    fbRef = null, // <-- Pass null for the preview
                    navController = navController // Pass NavController
                )
            }
            composable("contacts") {
                ContactForm(
                    navController = navController,
                    fbRef = null
                ) // <-- Pass null for the preview) // Pass NavController
            }
            composable("contact_list") {
                ContactList(
                    navController = navController,
                    fbRef = null
                )
            }
        }
    }
}

// 5. Create a Fake DAO implementation to satisfy the NoteRepository constructor.
//    Its methods don't need to do anything.
class FakeNoteDao : NoteDao {
    override suspend fun insert(note: Note) {}
    override suspend fun update(note: Note) {}
    override suspend fun delete(note: Note) {}
    override fun getAll(): Flow<List<Note>> = flowOf(emptyList()) // Default empty list
}
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
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
    @Insert suspend fun insert(note: Note)
    @Update suspend fun update(note: Note)
    @Delete suspend fun delete(note: Note)
    @Query("SELECT * FROM notes") fun getAll(): Flow<List<Note>>
}

// --------------------- Database ---------------------
@Database(entities = [Note::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

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
class NoteRepository(private val dao: NoteDao) {
    val notes = dao.getAll()
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.getDatabase(application).noteDao()
        val repository = NoteRepository(dao)
        val factory = NoteViewModelFactory(repository)
        val noteViewModel: NoteViewModel by viewModels { factory }

        setContent {
            MaterialTheme {
                NoteApp(noteViewModel)
            }
        }
    }
}

@Composable
fun NoteApp(viewModel: NoteViewModel = viewModel()) {
    val notes by viewModel.notes.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter note") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (text.isNotBlank()) {
                viewModel.addNote(text)
                text = ""
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(thisNote.text)
                    Row {
                        Button(onClick = { viewModel.updateNote(thisNote) }) {
                            Text("Update")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(onClick = { viewModel.deleteNote(thisNote) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

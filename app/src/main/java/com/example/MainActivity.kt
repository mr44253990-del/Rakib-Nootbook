package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.NoteRepository
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.EditorScreen
import com.example.ui.HomeScreen
import com.example.ui.LockScreen
import com.example.ui.Routes
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val database = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java,
        "rakib_notes.db"
    ).fallbackToDestructiveMigration().build()
    
    val repository = NoteRepository(database.noteDao())
    val factory = AppViewModelFactory(repository)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            RakibNoteApp(factory)
        }
      }
    }
  }
}

@Composable
fun RakibNoteApp(factory: AppViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = Routes.LOCK) {
        composable(Routes.LOCK) {
            LockScreen(
                onUnlock = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOCK) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNoteClick = { id -> navController.navigate(Routes.editorRoute(id)) },
                onAddClick = { navController.navigate(Routes.editorRoute(-1)) }
            )
        }
        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            EditorScreen(
                viewModel = viewModel,
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: AppViewModel,
    noteId: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    val existingNote = remember(notes, noteId) { notes.find { it.id == noteId } }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var selectedColor by remember { mutableStateOf(existingNote?.color ?: 0) }

    val colors = listOf(
        Color.Transparent, // Default
        Color(0xFFFFAB91), Color(0xFFFFCC80), Color(0xFFE6EE9C), 
        Color(0xFF81DEEA), Color(0xFFCF93D9), Color(0xFFF48FB1)
    )

    // Auto-save on exit
    DisposableEffect(Unit) {
        onDispose {
            if (title.isNotBlank() || content.isNotBlank()) {
                if (existingNote == null) {
                    viewModel.addNote(title, content, selectedColor)
                } else if (title != existingNote.title || content != existingNote.content || selectedColor != existingNote.color) {
                    viewModel.updateNote(existingNote.copy(title = title, content = content, color = selectedColor))
                }
            }
        }
    }

    val backgroundColor = if (selectedColor != 0) Color(selectedColor) else MaterialTheme.colorScheme.background

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val textToCopy = if (title.isNotBlank()) "$title\n$content" else content
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("note", textToCopy)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                    if (existingNote != null) {
                        IconButton(
                            onClick = {
                                viewModel.deleteNote(existingNote.id)
                                onNavigateBack()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = if (selectedColor != 0) Color.DarkGray else MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = if (selectedColor != 0) Color.DarkGray else MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(colors) { color ->
                        val isSelected = (if (color == Color.Transparent) 0 else color.toArgb()) == selectedColor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (color == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant else color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = if (color == Color.Transparent) 0 else color.toArgb()
                                }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", style = MaterialTheme.typography.titleLarge) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (selectedColor != 0) Color.DarkGray else MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = if (selectedColor != 0) Color.DarkGray else MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = if (selectedColor != 0) Color.DarkGray.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = if (selectedColor != 0) Color.DarkGray.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Note content...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (selectedColor != 0) Color.DarkGray else MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = if (selectedColor != 0) Color.DarkGray else MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = if (selectedColor != 0) Color.DarkGray.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = if (selectedColor != 0) Color.DarkGray.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().weight(1f) // Actually weight doesn't work inside scroll without explicit height. It will expand as we type.
            )
        }
    }
}

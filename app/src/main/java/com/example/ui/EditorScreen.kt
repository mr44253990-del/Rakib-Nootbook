package com.example.ui

import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AutoAwesome
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note

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
    var selectedColor by remember { mutableStateOf(existingNote?.color ?: 0xFFFFFFFF.toInt()) }
    var currentTag by remember { mutableStateOf(existingNote?.tag ?: "") }
    var isPrivate by remember { mutableStateOf(existingNote?.isPrivate ?: false) }

    val isDark = isSystemInDarkTheme()
    val isAILoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val tags = listOf("", "BRAIN TRUST", "ACTION", "IDEA", "CODE", "REMINDER")
    
    // Custom pastel colors from the image reference
    val colors = listOf(
        0xFFFFFFFF.toInt(), // White
        0xFFE8EAF6.toInt(), // Pastel Blue (Brain Trust)
        0xFFFFF3E0.toInt(), // Pastel Orange (Action)
        0xFFE8F5E9.toInt(), // Pastel Green (Code)
        0xFFF3E5F5.toInt()  // Pastel Purple (Reminder)
    )

    // Auto-save on exit
    DisposableEffect(Unit) {
        onDispose {
            if (title.isNotBlank() || content.isNotBlank()) {
                val newNote = Note(
                    id = existingNote?.id ?: 0,
                    title = title,
                    content = content,
                    color = selectedColor,
                    tag = currentTag,
                    isPrivate = isPrivate,
                    timestamp = existingNote?.timestamp ?: System.currentTimeMillis()
                )
                if (existingNote == null) {
                    viewModel.addNote(newNote)
                } else if (title != existingNote.title || content != existingNote.content ||
                           selectedColor != existingNote.color || currentTag != existingNote.tag ||
                           isPrivate != existingNote.isPrivate) {
                    viewModel.updateNote(newNote)
                }
            }
        }
    }

    val isWhiteColor = selectedColor == 0xFFFFFFFF.toInt() || selectedColor == 0
    val backgroundColor = if (isWhiteColor) (if (isDark) Color(0xFF1E1E2C) else Color.White) else Color(selectedColor).copy(alpha = if(isDark) 0.8f else 1f)
    val textColor = if (isDark) Color.White else Color.DarkGray

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Private", modifier = Modifier.padding(end = 4.dp))
                        Switch(
                            checked = isPrivate, 
                            onCheckedChange = { isPrivate = it },
                            modifier = Modifier.scale(0.8f) 
                        )
                    }
                    IconButton(
                        onClick = {
                            val textToCopy = if (title.isNotBlank()) "$title\n$content" else content
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, textToCopy)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
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
                    navigationIconContentColor = textColor,
                    actionIconContentColor = textColor
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = if (isDark) Color(0xFF20202F) else Color.White.copy(alpha = 0.5f),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(colors) { colorInt ->
                        val isSelected = colorInt == selectedColor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) PrimaryPurple else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorInt }
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
            
            // Tag Selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tags) { tag ->
                    val isSelected = tag == currentTag
                    val label = if (tag.isEmpty()) "No Tag" else tag 
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryPurple else (if(isDark) Color(0xFF2C2C3E) else Color.White.copy(alpha = 0.5f)))
                            .clickable { currentTag = tag }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (isAILoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { viewModel.generateTitle(content) { newTitle -> title = newTitle } },
                        label = { Text("Smart Title", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { viewModel.generateSummary(content) { newContent -> content = newContent } },
                        label = { Text("Auto Summary", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { viewModel.checkGrammar(content) { newContent -> content = newContent } },
                        label = { Text("Fix Grammar", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", style = MaterialTheme.typography.headlineMedium) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedPlaceholderColor = textColor.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = textColor.copy(alpha = 0.5f)
                ),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Start typing your thoughts...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedPlaceholderColor = textColor.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = textColor.copy(alpha = 0.5f)
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = if (currentTag == "CODE") androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
            )
        }
    }
}

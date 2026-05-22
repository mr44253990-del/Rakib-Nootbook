package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.isSystemInDarkTheme

val PrimaryPurple = Color(0xFF5A35FF)
val BgColor = Color(0xFFF7F9FC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNoteClick: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val currentBgColor = if (isDark) Color(0xFF1E1E2C) else BgColor
    val headerTextColor = if (isDark) Color.White else Color(0xFF1E1E2C)
    val cardBgColor = if (isDark) Color(0xFF2C2C3E) else Color.White

    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Private", "Ideas", "To-Do")

    val filteredNotes = notes.filter { note ->
        when (selectedFilter) {
            "Private" -> note.isPrivate
            "Ideas" -> note.tag == "IDEA" || note.tag == "BRAIN TRUST"
            "To-Do" -> note.tag == "ACTION"
            else -> true // "All"
        }
    }

    Scaffold(
        containerColor = currentBgColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(64.dp).offset(y = 40.dp) // Offset to overlap bottom bar
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppNavigationBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Rakib's Brain", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = headerTextColor)
                    Text(
                        "SMART ASSISTANT ACTIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RN", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Search Bar
            var searchQuery by remember { mutableStateOf("") }
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFEAEAEA), CircleShape),
                placeholder = { Text("Search your thoughts...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardBgColor,
                    unfocusedContainerColor = cardBgColor,
                    disabledContainerColor = cardBgColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = headerTextColor,
                    unfocusedTextColor = headerTextColor
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) PrimaryPurple else cardBgColor)
                            .border(1.dp, if (isSelected) PrimaryPurple else Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), CircleShape)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else (if(isDark) Color.LightGray else Color.Gray),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notes Grid
            if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes found.", color = Color.Gray)
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    items(filteredNotes.filter { it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true) }, key = { it.id }) { note ->
                        NoteCard(note = note, onClick = { onNoteClick(note.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val isWhite = note.color == 0xFFFFFFFF.toInt() || note.color == 0
    
    // In dark mode, if color is white, we make it dark gray. Otherwise we dim the color for dark mode.
    val baseColor = if (isWhite) (if (isDark) Color(0xFF2C2C3E) else Color.White) else Color(note.color)
    val backgroundColor = if (isDark && !isWhite) baseColor.copy(alpha = 0.8f) else baseColor
    val textColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF2C2C2C)
    
    val tagColor = when(note.tag) {
        "ACTION" -> Color(0xFFD97736)
        "BRAIN TRUST" -> PrimaryPurple
        "CODE" -> Color(0xFF2E7D32)
        "REMINDER" -> PrimaryPurple
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(
                width = if (isWhite) 1.dp else 0.dp,
                color = if (isWhite) Color(0xFFEAEAEA) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        // Top section of card
        if (note.isPrivate) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Private", tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Private Note", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                    val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(note.timestamp))
                    Text("Biometric Lock • $timeStr", fontSize = 10.sp, color = Color.Gray)
                }
            }
        } else {
            if (note.tag.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.tag == "REMINDER" || note.tag == "BRAIN TRUST") {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(tagColor))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = note.tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (note.tag == "ACTION") {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = tagColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    lineHeight = 22.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = if (note.title.isNotBlank()) 4 else 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (note.tag == "BRAIN TRUST") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(PrimaryPurple))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Active", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PrimaryPurple)
                }
            }
        }
    }
}

@Composable
fun BottomAppNavigationBar() {
    val isDark = isSystemInDarkTheme()
    val barColor = if (isDark) Color(0xFF20202F) else Color.White
    Surface(
        color = barColor,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Description, "NOTES", true)
            BottomNavItem(Icons.Default.Search, "SEARCH", false)
            Spacer(modifier = Modifier.width(48.dp)) // Space for FAB
            BottomNavItem(Icons.Default.Notifications, "REMIND", false)
            BottomNavItem(Icons.Default.Menu, "MENU", false)
        }
    }
}

@Composable
fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            icon, 
            contentDescription = label, 
            tint = if (isSelected) PrimaryPurple else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label, 
            fontSize = 10.sp, 
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PrimaryPurple else Color.Gray
        )
    }
}

package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE) }

    val notes by viewModel.allNotes.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Private", "Ideas", "To-Do")

    // Active bottom navigation tab
    var activeTab by remember { mutableStateOf("NOTES") }

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
                modifier = Modifier.size(64.dp).offset(y = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppNavigationBar(activeTab) { tab ->
                activeTab = tab
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Rakib's Brain", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = headerTextColor)
                    Text(
                        when (activeTab) {
                            "NOTES" -> "SMART ASSISTANT ACTIVE"
                            "SEARCH" -> "KNOWLEDGE RETRIEVAL HUB"
                            "REMIND" -> "TIME & SCHEDULER ACTIVE"
                            else -> "SYSTEM PREFERENCES & SETTINGS"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0).copy(alpha = if (isDark) 0.1f else 1f), CircleShape)
                        .background(if (isDark) Color(0xFF2C2C3E) else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("RN", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Tab View content panel
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                when (targetTab) {
                    "NOTES" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            
                            // Stats row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DashboardMiniCard(
                                    title = "মোট নোট",
                                    value = "${notes.size}",
                                    icon = Icons.Default.LibraryBooks,
                                    bgColor = cardBgColor,
                                    textColor = headerTextColor,
                                    modifier = Modifier.weight(1f)
                                )
                                DashboardMiniCard(
                                    title = "লকড",
                                    value = "${notes.filter { it.isPrivate }.size}",
                                    icon = Icons.Default.Lock,
                                    bgColor = cardBgColor,
                                    textColor = headerTextColor,
                                    modifier = Modifier.weight(1.1f)
                                )
                                DashboardMiniCard(
                                    title = "গুরুত্বপূর্ণ",
                                    value = "${notes.filter { it.tag == "BRAIN TRUST" || it.tag == "ACTION" }.size}",
                                    icon = Icons.Default.AutoAwesome,
                                    bgColor = cardBgColor,
                                    textColor = headerTextColor,
                                    modifier = Modifier.weight(1.1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

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

                            Spacer(modifier = Modifier.height(16.dp))

                            // Notes Grid
                            if (filteredNotes.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Notes,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("কোনো নোট খুঁজে পাওয়া যায়নি।", color = Color.Gray, fontSize = 14.sp)
                                    }
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
                                    items(filteredNotes, key = { it.id }) { note ->
                                        NoteCard(note = note, onClick = { onNoteClick(note.id) })
                                    }
                                }
                            }
                        }
                    }
                    
                    "SEARCH" -> {
                        var searchQuery by remember { mutableStateOf("") }
                        var activeSearchTag by remember { mutableStateOf("") }
                        
                        val searchResults = notes.filter { note ->
                            val matchesText = note.title.contains(searchQuery, true) || note.content.contains(searchQuery, true)
                            val matchesTag = activeSearchTag.isBlank() || note.tag == activeSearchTag
                            matchesText && matchesTag
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        ) {
                            // Search input
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), CircleShape),
                                placeholder = { Text("নোটের শিরোনাম বা তথ্য খুঁজুন...", color = Color.Gray, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                        }
                                    }
                                },
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

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Tags visualizer
                            Text(
                                "ট্যাগ ফিল্টার করুন (Filter by Tags)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val searchTags = listOf("ACTION", "BRAIN TRUST", "IDEA", "CODE", "REMINDER")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (activeSearchTag.isBlank()) PrimaryPurple else cardBgColor)
                                            .border(1.dp, if (activeSearchTag.isBlank()) PrimaryPurple else Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), CircleShape)
                                            .clickable { activeSearchTag = "" }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("সব (All)", fontSize = 12.sp, color = if (activeSearchTag.isBlank()) Color.White else Color.Gray)
                                    }
                                }
                                items(searchTags) { tag ->
                                    val isSelected = activeSearchTag == tag
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) PrimaryPurple else cardBgColor)
                                            .border(1.dp, if (isSelected) PrimaryPurple else Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), CircleShape)
                                            .clickable { activeSearchTag = tag }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(tag, fontSize = 12.sp, color = if (isSelected) Color.White else Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Search Results
                            if (searchQuery.isBlank() && activeSearchTag.isBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.YoutubeSearchedFor, contentDescription = null, modifier = Modifier.size(56.dp), tint = PrimaryPurple.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("এআই স্মার্ট সার্চ অ্যাক্টিভ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = headerTextColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("নোট থেকে যেকোনো লেখা বা কী-ওয়ার্ড খুঁজুন মুহূর্তেই", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                                }
                            } else if (searchResults.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("কোনো তথ্য খুঁজে পাওয়া যায়নি!", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                                }
                            } else {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalItemSpacing = 12.dp
                                ) {
                                    items(searchResults, key = { it.id }) { note ->
                                        NoteCard(note = note, onClick = { onNoteClick(note.id) })
                                    }
                                }
                            }
                        }
                    }
                    
                    "REMIND" -> {
                        val actionNotes = notes.filter { it.tag == "REMINDER" || it.tag == "ACTION" }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                "রিমাইন্ডার এবং অ্যাকশন টাস্কস",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                            Text(
                                "আপনার দেওয়া ACTION বা REMINDER টাস্কগুলোর সময়সূচী ও সতর্কতা এখানে দেখতে পাবেন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            if (actionNotes.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.NotificationsNone, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(60.dp), 
                                            tint = PrimaryPurple.copy(alpha = 0.4f)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("কোনো রিমাইন্ডার সেট করা নেই", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = headerTextColor)
                                        Text("নতুন নোটে 'REMINDER' বা 'ACTION' ট্যাগ সিলেক্ট করুন।", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            } else {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Fixed(1),
                                    modifier = Modifier.fillMaxSize(),
                                    verticalItemSpacing = 16.dp
                                ) {
                                    items(actionNotes, key = { it.id }) { note ->
                                        ReminderItemCard(
                                            note = note,
                                            sharedPrefs = sharedPrefs,
                                            isDark = isDark,
                                            cardBgColor = cardBgColor,
                                            headerTextColor = headerTextColor,
                                            onNoteClick = { onNoteClick(note.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    "MENU" -> {
                        var showPinSheet by remember { mutableStateOf(false) }
                        val activeModel by viewModel.selectedModel.collectAsStateWithLifecycle()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            
                            // Model choices
                            Text(
                                "🧠 Gemini AI মডেল পরিবর্তন করুন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                            Text(
                                "Gemini AI-এর বিভিন্ন মডেলের গতি ও নির্ভুলতা এক্সপেরিমেন্ট করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            ModelSelectorCard(
                                displayName = "Gemini 3.5 Flash (Default Standard)",
                                description = "দ্রুত ও বুদ্ধিদীপ্ত উত্তর, টেক্সট সামারি এবং রাইটিং সম্পাদনা।",
                                speedBenchmark = "Speed: 1.2s • Balanced Quality",
                                isSelected = activeModel == "gemini-3.5-flash",
                                cardBg = cardBgColor,
                                textClr = headerTextColor,
                                onClick = {
                                    viewModel.selectModel("gemini-3.5-flash")
                                    Toast.makeText(context, "Gemini 3.5 Flash Active!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ModelSelectorCard(
                                displayName = "Gemini 3.1 Flash Lite (Lite Edition)",
                                description = "সর্বোচ্চ গতিসম্পন্ন আল্ট্রা-লাইট মডেল। যেকোনো কোড ও ছোট তথ্যের জন্য উপযুক্ত।",
                                speedBenchmark = "Speed: 0.7s • High Speed & Low Latency",
                                isSelected = activeModel == "gemini-3.1-flash-lite-preview" || activeModel.contains("lite"),
                                cardBg = cardBgColor,
                                textClr = headerTextColor,
                                onClick = {
                                    viewModel.selectModel("gemini-3.1-flash-lite-preview")
                                    Toast.makeText(context, "Gemini 3.1 Flash Lite Active!", Toast.LENGTH_SHORT).show()
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Security Lock Reset
                            Text(
                                "🔒 সিকিউরিটি সেটিংস (Security Vault)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = cardBgColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), RoundedCornerShape(20.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showPinSheet = true }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryPurple)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("গোপন পিন কোড পরিবর্তন করুন", fontWeight = FontWeight.Bold, color = headerTextColor, fontSize = 14.sp)
                                            Text("পরিবর্তন করতে এখানে ক্লিক করুন (Reset Master PIN)", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                }
                            }

                            if (showPinSheet) {
                                var newPin by remember { mutableStateOf("") }
                                var confirmPin by remember { mutableStateOf("") }
                                var step by remember { mutableStateOf(1) }
                                var pinError by remember { mutableStateOf<String?>(null) }

                                AlertDialog(
                                    onDismissRequest = { showPinSheet = false },
                                    containerColor = if (isDark) Color(0xFF242435) else Color.White,
                                    title = {
                                        Text(
                                            if (step == 1) "নতুন ৪-সংখ্যার পিন দিন" else "পিনটি নিশ্চিত করুন",
                                            color = headerTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            if (pinError != null) {
                                                Text(pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                            
                                            val currentLength = if (step == 1) newPin.length else confirmPin.length
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                for (i in 0 until 4) {
                                                    val isFilled = i < currentLength
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isFilled) PrimaryPurple else Color.Transparent)
                                                            .border(2.dp, if (isFilled) PrimaryPurple else Color.Gray, CircleShape)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            val nums = listOf(
                                                listOf("1", "2", "3"),
                                                listOf("4", "5", "6"),
                                                listOf("7", "8", "9"),
                                                listOf("", "0", "DEL")
                                            )
                                            for (row in nums) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    for (n in row) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(CircleShape)
                                                                .background(if (n.isNotEmpty() && n != "DEL") (if(isDark) Color.White.copy(alpha=0.04f) else Color.Black.copy(alpha=0.03f)) else Color.Transparent)
                                                                .clickable(enabled = n.isNotEmpty()) {
                                                                    if (n == "DEL") {
                                                                        if (step == 1) {
                                                                            if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                                                                        } else {
                                                                            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                                                        }
                                                                        pinError = null
                                                                    } else {
                                                                        if (step == 1) {
                                                                            if (newPin.length < 4) newPin += n
                                                                        } else {
                                                                            if (confirmPin.length < 4) confirmPin += n
                                                                        }
                                                                        pinError = null
                                                                    }
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(n, fontWeight = FontWeight.Bold, color = headerTextColor, fontSize = 16.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                            onClick = {
                                                if (step == 1) {
                                                    if (newPin.length == 4) {
                                                        step = 2
                                                    } else {
                                                        pinError = "পিন অবশ্যই ৪ সংখ্যার হতে হবে!"
                                                    }
                                                } else {
                                                    if (confirmPin == newPin) {
                                                        sharedPrefs.edit().putString("master_pinkey", newPin).apply()
                                                        Toast.makeText(context, "পিন সফলভাবে পরিবর্তন করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        showPinSheet = false
                                                    } else {
                                                        pinError = "পিন মেলেনি! পুনরায় চেষ্টা করুন।"
                                                        confirmPin = ""
                                                        step = 1
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(if (step == 1) "পরবর্তী" else "নিশ্চিত করুন", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showPinSheet = false }) {
                                            Text("বন্ধ করুন", color = Color.Gray)
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Brain Metrics Analytics
                            Text(
                                "📊 মেমোরি ডেটা অ্যানালিটিক্স",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = cardBgColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), RoundedCornerShape(20.dp))
                                    .padding(20.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    BrainMetricItem("টোটাল সেভ করা নোটস:", "${notes.size} টি নোট", isDark)
                                    BrainMetricItem("কোড স্পেসিফিকেশন:", "${notes.count { it.tag == "CODE" }} টি ফাইল", isDark)
                                    BrainMetricItem("পদ্ধতিগত রিমাইন্ডার:", "${notes.count { it.tag == "REMINDER" }} টি এলার্ট", isDark)
                                    BrainMetricItem("ব্যক্তিগত ভোল্ট নোট:", "${notes.count { it.isPrivate }} টি সিকিউরড", isDark)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier.height(72.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryPurple)
            }
            Column(verticalArrangement = Arrangement.Center) {
                Text(title, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun BrainMetricItem(label: String, valStr: String, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if(isDark) Color.LightGray else Color.DarkGray, fontSize = 13.sp)
        Text(valStr, fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 13.sp)
    }
}

@Composable
fun ModelSelectorCard(
    displayName: String,
    description: String,
    speedBenchmark: String,
    isSelected: Boolean,
    cardBg: Color,
    textClr: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryPurple.copy(alpha = 0.08f) else cardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryPurple else Color(0xFFEAEAEA).copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isSelected) PrimaryPurple else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        displayName,
                        fontWeight = FontWeight.Bold,
                        color = textClr,
                        fontSize = 14.sp
                    )
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(speedBenchmark, fontSize = 10.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReminderItemCard(
    note: Note,
    sharedPrefs: android.content.SharedPreferences,
    isDark: Boolean,
    cardBgColor: Color,
    headerTextColor: Color,
    onNoteClick: () -> Unit
) {
    val context = LocalContext.current
    val prefKey = "reminder_time_note_${note.id}"
    var storedTime by remember { mutableStateOf(sharedPrefs.getString(prefKey, null)) }
    
    // Setting Setup interface state
    var isTimeInputOpen by remember { mutableStateOf(false) }
    var inputHour by remember { mutableStateOf("09") }
    var inputMin by remember { mutableStateOf("30") }
    var inputPeriod by remember { mutableStateOf("AM") }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardBgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).clickable { onNoteClick() }) {
                    Text(
                        text = if(note.title.isNotBlank()) note.title else "বিনা শিরোনামের নোট",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = headerTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = note.content,
                        maxLines = 1,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Active status badge & setup clock triggering
                IconButton(
                    onClick = { isTimeInputOpen = !isTimeInputOpen }
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = "Schedule",
                        tint = if (storedTime != null) PrimaryPurple else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline alarm time setup configuration drawer
            if (isTimeInputOpen) {
                Surface(
                    color = if(isDark) Color(0xFF1E1E2C) else Color(0xFFF3F5F9),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "রিমাইন্ডার সেট করুন (Setup Daily Alert)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = headerTextColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour selector column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hour", fontSize = 10.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = {
                                        val hr = inputHour.toIntOrNull() ?: 9
                                        val nextHr = if (hr == 12) 1 else hr + 1
                                        inputHour = String.format("%02d", nextHr)
                                    }) {
                                        Text(inputHour, fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 18.sp)
                                    }
                                }
                            }
                            
                            Text(":", fontWeight = FontWeight.Bold, color = headerTextColor)
                            
                            // Minute selector column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Min", fontSize = 10.sp, color = Color.Gray)
                                TextButton(onClick = {
                                    val m = inputMin.toIntOrNull() ?: 0
                                    val nextMin = (m + 5) % 60
                                    inputMin = String.format("%02d", nextMin)
                                }) {
                                    Text(inputMin, fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 18.sp)
                                }
                            }

                            // Period AM/PM selector column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Period", fontSize = 10.sp, color = Color.Gray)
                                TextButton(onClick = {
                                    inputPeriod = if (inputPeriod == "AM") "PM" else "AM"
                                }) {
                                    Text(inputPeriod, fontWeight = FontWeight.Bold, color = PrimaryPurple, fontSize = 18.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isTimeInputOpen = false }) {
                                Text("বাতিল", color = Color.Gray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                onClick = {
                                    val compiledTime = "$inputHour:$inputMin $inputPeriod"
                                    sharedPrefs.edit().putString(prefKey, compiledTime).apply()
                                    storedTime = compiledTime
                                    isTimeInputOpen = false
                                    Toast.makeText(context, "রিমাইন্ডার সেট করা হয়েছে: $compiledTime!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("সংরক্ষণ করুন", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Stored display visual status notification
            if (storedTime != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AlarmOn,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আজ $storedTime টায় এলার্ট দেওয়া হবে।",
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "মুছে ফেলুন",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            sharedPrefs.edit().remove(prefKey).apply()
                            storedTime = null
                            Toast.makeText(context, "রিমাইন্ডার বাতিল করা হয়েছে।", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } else if (!isTimeInputOpen) {
                Text(
                    text = "🔔 কোনো এলার্ট বা সময় সেট করা নেই। সেট করতে ঘড়ির আইকন প্রেস করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val isWhite = note.color == 0xFFFFFFFF.toInt() || note.color == 0
    
    // Dim normal notes if in dark mode
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
                color = if (isWhite) Color(0xFFEAEAEA).copy(alpha = if(isDark) 0.1f else 1f) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
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
                    Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = PrimaryPurple)
                }
            }
        }
    }
}

@Composable
fun BottomAppNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
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
            BottomNavItem(Icons.Default.Description, "NOTES", selectedTab == "NOTES") { onTabSelected("NOTES") }
            BottomNavItem(Icons.Default.Search, "SEARCH", selectedTab == "SEARCH") { onTabSelected("SEARCH") }
            Spacer(modifier = Modifier.width(48.dp)) // Ideal Space for centered Action Button
            BottomNavItem(Icons.Default.Notifications, "REMIND", selectedTab == "REMIND") { onTabSelected("REMIND") }
            BottomNavItem(Icons.Default.Menu, "MENU", selectedTab == "MENU") { onTabSelected("MENU") }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
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

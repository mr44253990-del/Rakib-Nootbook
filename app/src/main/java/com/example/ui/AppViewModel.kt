package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.asStateFlow
import com.example.BuildConfig
import com.example.data.api.RetrofitClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Content
import com.example.data.api.Part
import kotlinx.coroutines.flow.MutableStateFlow

class AppViewModel(private val repository: NoteRepository) : ViewModel() {
    val allNotes: StateFlow<List<Note>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    fun selectModel(modelName: String) {
        _selectedModel.value = modelName
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            repository.insert(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun generateSummary(content: String, onResult: (String) -> Unit) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Please summarize the following text:\n\n$content")))),
                    systemInstruction = Content(parts = listOf(Part(text = "You are a helpful assistant that summarizes text concisely in the original language.")))
                )
                val response = RetrofitClient.service.generateContent(_selectedModel.value, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) onResult(text)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun generateTitle(content: String, onResult: (String) -> Unit) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Generate a short, catchy title (maximum 6 words) for the following text:\n\n$content")))),
                    systemInstruction = Content(parts = listOf(Part(text = "You are a helpful assistant that generates extremely short titles. Do not use quotes in the final title.")))
                )
                val response = RetrofitClient.service.generateContent(_selectedModel.value, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) onResult(text.replace("\"", "").trim())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun checkGrammar(content: String, onResult: (String) -> Unit) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Fix grammar and rewrite the following text professionally:\n\n$content")))),
                    systemInstruction = Content(parts = listOf(Part(text = "You are a professional editor. Only output the rewritten text. Keep the same language.")))
                )
                val response = RetrofitClient.service.generateContent(_selectedModel.value, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) onResult(text)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _aiLoading.value = false
            }
        }
    }
}

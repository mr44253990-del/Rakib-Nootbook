package com.example.ui

object Routes {
    const val SPLASH = "splash"
    const val LOCK = "lock"
    const val HOME = "home"
    const val EDITOR = "editor/{noteId}"
    
    fun editorRoute(noteId: Int = -1) = "editor/$noteId"
}

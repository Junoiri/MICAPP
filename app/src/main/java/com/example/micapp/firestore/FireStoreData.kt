package com.example.micapp.firestore

data class FireStoreData(
    val userid: String = "",
    val email: String = "",
    val username: String = ""
)
data class Event(
    val name: String = "",
    val date: String = "",
    val username: String = "",
    val userEmail: String = "",
    val groups: List<String> = emptyList(),
    val category: String = "",
    val description: String = "",
    val attachments: List<String>? = null
)

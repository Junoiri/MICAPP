package com.example.micapp.authManagers

import com.google.firebase.auth.FirebaseAuth

class EmailAuthManager(private val auth: FirebaseAuth) {

    fun signInWithEmailAndPassword(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

}

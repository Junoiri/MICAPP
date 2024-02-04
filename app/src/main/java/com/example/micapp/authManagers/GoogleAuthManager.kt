package com.example.micapp.authManagers

import android.app.Activity
import android.content.Intent
import com.example.micapp.CalendarMainScreenActivity
import com.example.micapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

// TODO: My notes about it are in word document under number 1
class GoogleAuthManager(private val activity: Activity) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun getGoogleSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        return googleSignInClient.signInIntent
    }


    fun handleSignInResult(
        completedTask: Task<GoogleSignInAccount>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Successfully signed in
            firebaseAuthWithGoogle(account.idToken, onSuccess, onFailure)
        } catch (e: ApiException) {
            // Google Sign-In failed
            onFailure("Google Sign-In failed. Status code: ${e.statusCode}")
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (idToken == null) {
            onFailure("Google Sign-In failed - ID Token is null.")
            return
        }

        // Proceed with Firebase authentication using the ID token
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onFailure("Authentication failed: ${task.exception?.message}")
            }
        }
    }

    // all the onSuccess methods were supposed to be handled in login/register activity
    private fun navigateToNextActivity() {
            val nextActivityIntent = Intent(activity, CalendarMainScreenActivity::class.java)
            activity.startActivity(nextActivityIntent)
            activity.finish()
    }
}

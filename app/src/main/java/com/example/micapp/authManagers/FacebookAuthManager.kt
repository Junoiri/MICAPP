package com.example.micapp.authManagers

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.micapp.CalendarMainScreenActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FacebookAuthProvider

class FacebookAuthManager(private val activity: Activity) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val callbackManager: CallbackManager = CallbackManager.Factory.create()

    init {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken.token)
            }

            override fun onCancel() {
                Toast.makeText(activity, "Facebook login was cancelled.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(activity, "Error during Facebook login: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getLoginIntent(): Intent {
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
        // Placeholder: "The actual intent is managed internally by the Facebook SDK"
        return Intent()
    }
    fun logInWithReadPermissions() {
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
    }

    private fun handleFacebookAccessToken(token: String) {
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                navigateToNextActivity()
            } else {
                Toast.makeText(activity, "Firebase authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToNextActivity() {
        val nextActivityIntent = Intent(activity, CalendarMainScreenActivity::class.java)
        activity.startActivity(nextActivityIntent)
        activity.finish()
    }
}

package com.example.micapp

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.micapp.authManagers.FacebookAuthManager
import com.example.micapp.authManagers.GoogleAuthManager
import com.example.micapp.authManagers.MicrosoftAuthManager
import com.example.micapp.authManagers.EmailAuthManager
import com.example.micapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailAuthManager: EmailAuthManager
    private lateinit var facebookAuthManager: FacebookAuthManager
    private lateinit var googleAuthManager: GoogleAuthManager
    private lateinit var microsoftAuthManager: MicrosoftAuthManager
    private lateinit var facebookLoginLauncher: ActivityResultLauncher<Intent>
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeAuthManagers()

        setupEmailLogin()
        setupFacebookLogin()
        setupGoogleLogin()
        setupMicrosoftLogin()
        setupForgotPassword()

        val signUpTextView: TextView = findViewById(R.id.textViewSignUp)
        signUpTextView.setOnClickListener {
            navigateToRegisterActivity()
        }
    }

    private fun initializeAuthManagers() {
        auth = FirebaseAuth.getInstance()
        emailAuthManager = EmailAuthManager(auth)
        facebookAuthManager = FacebookAuthManager(this)
        googleAuthManager = GoogleAuthManager(this)
        microsoftAuthManager = MicrosoftAuthManager(this)
    }

    private fun setupEmailLogin() {
        val emailLoginButton: Button = findViewById(R.id.buttonLogin)
        emailLoginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmailLogin).text.toString().trim()
            val password = findViewById<EditText>(R.id.editTextPasswordLogin).text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                emailAuthManager.signInWithEmailAndPassword(email, password, onSuccess = {
                    // Confirm the login is successful and the currentUser is not null
                    if (auth.currentUser != null) {
                        navigateToCalendarMainScreen()
                    } else {
                        showToast("Login failed. Please try again.")
                    }
                }, onFailure = { errorMsg ->
                    showToast(errorMsg)
                })
            } else {
                showToast("Please enter both email and password.")
            }
        }
    }


    private fun setupFacebookLogin() {
        facebookLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                facebookAuthManager.callbackManager.onActivityResult(
                    0, // requestCode is not used by the Facebook CallbackManager (for now?)
                    result.resultCode, // resultCode from the ActivityResult
                    data
                )
            }
        }

        val facebookLoginButton: ImageButton = findViewById(R.id.buttonFacebookLogin)
        facebookLoginButton.setOnClickListener {
            facebookAuthManager.logInWithReadPermissions()
        }
    }


    private fun setupGoogleLogin() {
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleGoogleSignInResult(result)
        }

        val googleLoginButton: ImageButton = findViewById(R.id.buttonGoogleLogin)
        googleLoginButton.setOnClickListener {
            googleSignInLauncher.launch(googleAuthManager.getGoogleSignInIntent())
        }
    }

    private fun setupMicrosoftLogin() {
        val microsoftLoginButton: ImageButton = findViewById(R.id.buttonMicrosoftLogin)
        microsoftLoginButton.setOnClickListener {
            microsoftAuthManager.signIn()
        }
    }

    private fun setupForgotPassword() {
        val forgotPasswordText: TextView = findViewById(R.id.textViewForgotPassword)
        forgotPasswordText.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun navigateToCalendarMainScreen() {
        val intent = Intent(this, CalendarMainScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleGoogleSignInResult(result: ActivityResult) {
        Log.d(TAG, "Google Sign-In result received: resultCode=${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            googleAuthManager.handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data), onSuccess = {
                // Confirm the login is successful and the currentUser is not null
                if (auth.currentUser != null) {
                    navigateToCalendarMainScreen()
                } else {
                    showToast("Login failed. Please try again.")
                }
            }, onFailure = { errorMsg ->
                showToast("Google Sign-In failed: $errorMsg")
            })
        } else {
            Log.w(TAG, "Google Sign-In was canceled or encountered an error")
            showToast("Google Sign-In failed or was canceled")
        }
    }
    private fun navigateToRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}

package com.example.micapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.micapp.firestore.FireStoreData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var signInTextView: TextView
    private lateinit var passwordVisibilityToggle: ImageButton
    private lateinit var imageViewMinChars: ImageView
    private lateinit var imageViewUppercase: ImageView
    private lateinit var imageViewNumber: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeFirebase()
        initializeViews()
        setupListeners()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initializeViews() {
        usernameEditText = findViewById(R.id.editTextUsernameRegister)
        emailEditText = findViewById(R.id.editTextEmailRegister)
        passwordEditText = findViewById(R.id.editTextPasswordRegister)
        registerButton = findViewById(R.id.buttonRegister)
        signInTextView = findViewById(R.id.textViewSignIn)
        passwordVisibilityToggle = findViewById(R.id.togglePasswordVisibility)
        imageViewMinChars = findViewById(R.id.imageViewMinChars)
        imageViewUppercase = findViewById(R.id.imageViewUppercase)
        imageViewNumber = findViewById(R.id.imageViewNumber)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            handleRegistration()
        }

        signInTextView.setOnClickListener {
            navigateToLoginActivity()
        }

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let { checkPasswordRequirements(it.toString()) }
            }
        })

        passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun handleRegistration() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()

        if (validateForm(email, password, username)) {
            createAccount(email, password, username)
        } else {
            Toast.makeText(this, "Please fill out all fields correctly.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(email: String, password: String, username: String): Boolean {
        val isValidEmail = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isValidPassword = password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() }
        val isValidUsername = username.isNotBlank()

        return isValidEmail && isValidPassword && isValidUsername
    }

    private fun createAccount(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userData = FireStoreData(userId, email, username)
                    saveUserToFirestore(userData)
                } else {
                    task.exception?.let {
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(baseContext, "Authentication failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }


    private fun saveUserToFirestore(userData: FireStoreData) {
        db.collection("Users").document(userData.userid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                navigateToLoginActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error writing document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun navigateToLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }

    private fun togglePasswordVisibility() {
        if (passwordEditText.inputType == 129) {
            passwordEditText.inputType = 144 // Visible password
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_on)
        } else {
            passwordEditText.inputType = 129 // Hidden password
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off)
        }
    }

    private fun checkPasswordRequirements(password: String) {
        imageViewMinChars.setImageResource(if (password.length >= 8) R.drawable.ic_check else R.drawable.ic_cross)
        imageViewUppercase.setImageResource(if (password.any { it.isUpperCase() }) R.drawable.ic_check else R.drawable.ic_cross)
        imageViewNumber.setImageResource(if (password.any { it.isDigit() }) R.drawable.ic_check else R.drawable.ic_cross)
    }
}


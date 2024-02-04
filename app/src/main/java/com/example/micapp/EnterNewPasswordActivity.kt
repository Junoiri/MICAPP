package com.example.micapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class EnterNewPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var passwordVisibilityToggle: ImageButton
    private lateinit var imageViewMinChars: ImageView
    private lateinit var imageViewUppercase: ImageView
    private lateinit var imageViewNumber: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_new_password)

        initializeViews()
        setListeners()
    }

    private fun initializeViews() {
        newPasswordEditText = findViewById(R.id.editTextNewPassword)
        passwordVisibilityToggle = findViewById(R.id.togglePasswordVisibility)
        imageViewMinChars = findViewById(R.id.imageViewMinChars)
        imageViewUppercase = findViewById(R.id.imageViewUppercase)
        imageViewNumber = findViewById(R.id.imageViewNumber)
        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        val buttonContinueResetPassword: Button = findViewById(R.id.buttonContinueResetPassword)

        buttonBack.setOnClickListener { navigateToEmailVerificationActivity() }
        buttonContinueResetPassword.setOnClickListener { handleContinueResetPassword() }
    }

    private fun setListeners() {
        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let { checkPasswordRequirements(it.toString()) }
            }
        })

        passwordVisibilityToggle.setOnClickListener { togglePasswordVisibility() }
    }

    private fun navigateToEmailVerificationActivity() {
        val intent = Intent(this, EmailVerificationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleContinueResetPassword() {
        // Placeholder for logic for handling password reset
    }

    private fun togglePasswordVisibility() {
        if (newPasswordEditText.inputType == 129) { // Hidden password
            newPasswordEditText.inputType = 144 // Visible password
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_on)
        } else {
            newPasswordEditText.inputType = 129 // Hidden password
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off)
        }
    }

    private fun checkPasswordRequirements(password: String) {
        imageViewMinChars.setImageResource(if (password.length >= 8) R.drawable.ic_check else R.drawable.ic_cross)
        imageViewUppercase.setImageResource(if (password.any { it.isUpperCase() }) R.drawable.ic_check else R.drawable.ic_cross)
        imageViewNumber.setImageResource(if (password.any { it.isDigit() }) R.drawable.ic_check else R.drawable.ic_cross)
    }
}

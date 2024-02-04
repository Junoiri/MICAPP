package com.example.micapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val info = packageManager.getPackageInfo(
                "com.example.micapp",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.d("KeyHash", keyHash)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Placeholder for exception
        } catch (e: NoSuchAlgorithmException) {
            // Another placeholder
        }


        val buttonLoginMain = findViewById<Button>(R.id.buttonLoginMain)
        buttonLoginMain.setOnClickListener {
            // Start LoginActivity
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        val buttonSignupMain = findViewById<Button>(R.id.buttonSignupMain)
        buttonSignupMain.setOnClickListener {
            // Start RegisterActivity
            val signupIntent = Intent(this, RegisterActivity::class.java)
            startActivity(signupIntent)
        }

    }


}

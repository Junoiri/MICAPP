package com.example.micapp.authManagers

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.micapp.CalendarMainScreenActivity
import com.example.micapp.R
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException

class MicrosoftAuthManager(private val activity: Activity) {
    private var singleAccountApp: ISingleAccountPublicClientApplication? = null

    init {
        // Initialize the MSAL single account app with the configuration file
        PublicClientApplication.createSingleAccountPublicClientApplication(
            activity.applicationContext,
            R.raw.msal_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    singleAccountApp = application
                }

                override fun onError(exception: MsalException) {
                    Toast.makeText(activity, "MSAL Exception: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun signIn() {
        singleAccountApp?.signIn(activity, null, arrayOf("user.read"), object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                val intent = Intent(activity, CalendarMainScreenActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }

            override fun onError(exception: MsalException) {
                Toast.makeText(activity, "Authentication failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCancel() {
                Toast.makeText(activity, "Authentication cancelled.", Toast.LENGTH_LONG).show()
            }
        })
    }
}

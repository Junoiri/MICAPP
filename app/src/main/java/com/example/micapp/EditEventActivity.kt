package com.example.micapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


// to be implemented in a future version - this class will have very similar functionality to the AddEventActivity
// however, it will be adapted to not allow duplicates of event's properties
class EditEventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)
    }
}
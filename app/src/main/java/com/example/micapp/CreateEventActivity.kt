package com.example.micapp

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.micapp.firestore.SaveEventToFirestore
import com.google.firebase.firestore.FirebaseFirestore
import com.example.micapp.firestore.Event

class CreateEventActivity : AppCompatActivity() {

    private lateinit var eventNameEditText: EditText
    private lateinit var closeButton: ImageView
    private lateinit var saveButton: Button
    private lateinit var userEmailTextView: TextView
    private lateinit var dayDateDisplay: TextView
    private lateinit var attachmentLauncher: ActivityResultLauncher<String>
    private lateinit var categorySpinner: Spinner
    private var selectedCategory: String? = null
    private lateinit var addAttachmentsText: TextView
    private var selectedPosition = 0
    private val selectedAttachmentUris = mutableListOf<Uri>()
    private lateinit var removeAttachmentImage: ImageView
    private lateinit var attachmentsRecyclerView: RecyclerView
    private lateinit var attachmentsAdapter: AttachmentsAdapter
    private lateinit var eventDescriptionEditText: EditText
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        initializeViews()
        setupClickListeners()
        fetchCategories()

        removeAttachmentImage = findViewById(R.id.removeAttachmentImage)



        // Retrieve the passed values from previous activity
        val selectedDate = intent.getStringExtra("SELECTED_DATE")
        userName = intent.getStringExtra("USER_NAME") ?: ""
        val userEmail = intent.getStringExtra("USER_EMAIL")

        dayDateDisplay.text = selectedDate ?: "Date not set"
        userEmailTextView.text = getString(R.string.user_display_format, userName, userEmail)
        addAttachmentsText = findViewById(R.id.addAttachmentsText)
        attachmentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            handleAttachmentUri(uri)
        }

        attachmentsRecyclerView = findViewById(R.id.attachmentsRecyclerView)
        setupAttachmentsRecyclerView()
    }

    private fun initializeViews() {
        eventNameEditText = findViewById(R.id.eventName)
        closeButton = findViewById(R.id.closeButton)
        saveButton = findViewById(R.id.saveButton)
        userEmailTextView = findViewById(R.id.userEmail)
        dayDateDisplay = findViewById(R.id.dayDateDisplay)
        categorySpinner = findViewById(R.id.categorySpinner)
        addAttachmentsText = findViewById(R.id.addAttachmentsText)
        eventDescriptionEditText = findViewById(R.id.eventDescription)
    }

    private fun setupClickListeners() {
        closeButton.setOnClickListener {
            closeActivity()
        }
        saveButton.setOnClickListener {
            saveEventToFirebase()
        }
        val addAttachmentsText: TextView = findViewById(R.id.addAttachmentsText)
        addAttachmentsText.setOnClickListener {
            openFilePicker()
        }
    }

    private fun closeActivity() {
        val intent = Intent(this, CalendarMainScreenActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchCategories() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Categories").get()
            .addOnSuccessListener { documents ->
                val categories = mutableListOf(getString(R.string.select_category))
                categories.addAll(documents.mapNotNull { it.getString("name") })

                val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
                    override fun isEnabled(position: Int): Boolean {
                        // Make the first item (default category) non-selectable
                        return position != 0
                    }

                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent) as TextView
                        view.setTextColor(Color.WHITE)
                        view.textSize = if (position == 0) 14f else 16f
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent) as TextView
                        if (position == 0) {
                            // Set the hint text color (shown only in the dropdown)
                            view.setTextColor(ContextCompat.getColor(context, R.color.category_disabled))
                        } else {
                            // Set the normal text color (for selected items in the dropdown)
                            view.setTextColor(ContextCompat.getColor(context, R.color.category_enabled))
                        }
                        view.textSize = 14f
                        return view
                    }
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter

                setupSpinnerItemSelectedListener()

//                setup ItemSelectedListener() // Call without passing the adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching categories: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSpinnerItemSelectedListener() {
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position > 0) {
                    selectedCategory = parent.getItemAtPosition(position) as String
                    selectedPosition = position
                    // Cast to ArrayAdapter<String>
                    (categorySpinner.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                } else {
                    selectedCategory = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
                selectedPosition = 0
                // Cast to ArrayAdapter<String>
                (categorySpinner.adapter as ArrayAdapter<String>).notifyDataSetChanged()
            }
        }
    }



    private fun saveEventToFirebase() {
        val eventName = eventNameEditText.text.toString().trim()
        val eventDescription = eventDescriptionEditText.text.toString().trim()
        val userEmail = userEmailTextView.text.toString()

        if (eventName.isNotEmpty() && selectedCategory != null && userName.isNotEmpty() && eventDescription.isNotEmpty()) {
            // Create the event object
            val event = createEventObject(eventName, selectedCategory!!, userName, userEmail, eventDescription)

            // Create an instance of SaveEventToFirestore
            val saveEventToFirestore = SaveEventToFirestore()

            // Call saveEventWithAttachments to save the event and upload attachments
            saveEventToFirestore.saveEventWithAttachments(
                event,
                selectedAttachmentUris,
                onSuccess = {
                    Toast.makeText(this, "Event with attachments saved successfully", Toast.LENGTH_SHORT).show()
                    navigateToMainScreen()

                    // Send a broadcast to trigger notification about a new event
                    val notificationIntent = Intent(this, ReminderBroadcast::class.java).apply {
                        putExtra("NOTIFICATION_TITLE", "\uD83D\uDCD8 New Academic Event!")
                        putExtra("NOTIFICATION_MESSAGE", "A new academic event has been scheduled. Check it out! \uD83D\uDCC5✨")
                    }
                    sendBroadcast(notificationIntent)
                },
                onFailure = { e ->
                    Toast.makeText(this, "Error saving event with attachments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(this, "Please enter all details for the event.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createEventObject(eventName: String, category: String, userName: String, userEmail: String, eventDescription: String): com.example.micapp.firestore.Event {
        val date = dayDateDisplay.text.toString()
        val attachmentPaths = selectedAttachmentUris.map { it.toString() }

        return com.example.micapp.firestore.Event(
            name = eventName,
            date = date,
            username = userName,
            userEmail = userEmail,
            category = category,
            description = eventDescription,
            attachments = attachmentPaths,
            groups = emptyList()
        )
    }



    private fun openFilePicker() {
        attachmentLauncher.launch("*/*") // Allows any file type.
    }


    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
    private fun handleAttachmentUri(uri: Uri?) {
        uri?.let {
            selectedAttachmentUris.add(it)
            if (attachmentsRecyclerView.visibility == View.GONE) {
                attachmentsRecyclerView.visibility = View.VISIBLE
            }
            attachmentsAdapter.notifyDataSetChanged()
        }
    }

    // Was needed when attachments jus didn't want to work :(
    private fun updateAttachmentsText() {
        val attachmentNames = selectedAttachmentUris.joinToString(", ") { uri ->
            getFileName(uri) ?: getString(R.string.unknown_file)
        }

        if (attachmentNames.isNotEmpty()) {
            addAttachmentsText.text = attachmentNames
            Log.d("CreateEventActivity", "Attachments updated: $attachmentNames")
        } else {
            addAttachmentsText.text = getString(R.string.add_attachments)
            Log.d("CreateEventActivity", "No attachments to display")
        }
    }

    private fun removeAttachmentAtPosition(position: Int) {
        if (position >= 0 && position < selectedAttachmentUris.size) {
            val removedUri = selectedAttachmentUris.removeAt(position)
            Log.d("CreateEventActivity", "Attachment removed: ${removedUri.path}")
            attachmentsAdapter.notifyItemRemoved(position)
            attachmentsAdapter.notifyItemRangeChanged(position, selectedAttachmentUris.size)
            if (selectedAttachmentUris.isEmpty()) {
                addAttachmentsText.text = getString(R.string.add_attachments)
                Log.d("CreateEventActivity", "All attachments removed")
            }
        } else {
            Log.d("CreateEventActivity", "Invalid position: $position")
        }
    }

    private fun setupAttachmentsRecyclerView() {
        attachmentsAdapter = AttachmentsAdapter(this, selectedAttachmentUris) { uri ->
            // Find the position of the uri to remove
            val position = selectedAttachmentUris.indexOf(uri)
            if (position != -1) {
                removeAttachmentAtPosition(position)
            }
        }
        attachmentsRecyclerView.adapter = attachmentsAdapter
        attachmentsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, CalendarMainScreenActivity::class.java)
        startActivity(intent)
        finish()
    }
}










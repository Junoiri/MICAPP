package com.example.micapp

import com.example.micapp.firestore.Event
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.google.android.material.navigation.NavigationView
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.addCallback
import com.example.micapp.firestore.FireStoreData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CalendarMainScreenActivity : AppCompatActivity() {

    private var originalLocale: Locale? = null
    private lateinit var slidingPanel: SlidingUpPanelLayout
    private lateinit var panelIcon: ImageView
    private lateinit var panelText: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var calendarView: CalendarView
    private lateinit var dayOfWeekText: TextView
    private lateinit var dateText: TextView
    private lateinit var addEventButton: ImageView
    private var lastSelectedDate: String? = null
    private lateinit var selectedDayEventsLabel: TextView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var eventsAdapter: EventListAdapter
    private lateinit var allEventsRecyclerView: RecyclerView
    private lateinit var allEventsAdapter: AllEventsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Save the original locale - for calendar language
        originalLocale = resources.configuration.locale
        setLocale(Locale.ENGLISH)

        setContentView(R.layout.activity_calendar_main_screen_layout)

        // Initialize the views
        slidingPanel = findViewById(R.id.sliding_layout)
        panelIcon = findViewById(R.id.panel_icon)
        panelText = findViewById(R.id.panel_text)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        calendarView = findViewById(R.id.calendarView)
        dayOfWeekText = findViewById(R.id.dayOfWeekText)
        dateText = findViewById(R.id.dateText)
        selectedDayEventsLabel = findViewById(R.id.selectedDayEventsLabel)
        selectedDayEventsLabel.visibility = View.GONE
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView)
        eventsAdapter = EventListAdapter(mutableListOf())
        eventsRecyclerView.adapter = eventsAdapter
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        allEventsRecyclerView = findViewById(R.id.slidingPanelRecyclerView) // Adjust ID as necessary
        allEventsAdapter = AllEventsAdapter(mutableListOf())
        allEventsRecyclerView.adapter = allEventsAdapter
        allEventsRecyclerView.layoutManager = LinearLayoutManager(this)


        setupAddEventButton()
        setupToolbar()
        setupMenuDrawer()
        setupSlidingPanel()
        setupCalendarView()
        setupCalendarView()
        fetchAndDisplayAllEvents()



        // Get the current user's ID from FirebaseAuth for fetching user data
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            // Not logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        fetchUserData(userId)

    }
    private fun fetchUserData(userId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.d("CalendarMainScreenActivity", "Attempting to fetch user data for ID: $userId")
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(FireStoreData::class.java)
                    if (userData != null) {
                        Log.d("CalendarMainScreenActivity", "Fetched user data: $userData")
                        updateUIWithUserData(userData)
                    } else {
                        Log.d("CalendarMainScreenActivity", "Document exists but unable to convert to FireStoreData object.")
                        Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("CalendarMainScreenActivity", "No document found for user ID: $userId")
                    Toast.makeText(this, "No user data found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarMainScreenActivity", "Error fetching user data", exception)
                Toast.makeText(this, "Error fetching user data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Update the UI with the user's data - needed for the CreateEventActivity
    private fun updateUIWithUserData(userData: FireStoreData) {
        addEventButton.setOnClickListener {
            val intent = Intent(this, CreateEventActivity::class.java).apply {
                putExtra("SELECTED_DATE", lastSelectedDate ?: "Date not set")
                putExtra("USER_NAME", userData.username)
                putExtra("USER_EMAIL", userData.email)
            }
            startActivity(intent)
        }
    }


    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun setupMenuDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    // Set up the sliding panel with a listener for state changes
    private fun setupSlidingPanel() {
        slidingPanel = findViewById(R.id.sliding_layout)
        // Set the initial state icon and text
        panelIcon.setImageResource(R.drawable.ic_swipe_up)
        panelText.setText(R.string.swipe_up)

        slidingPanel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                // Not needed - the panel reacts to pressing - state changed
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: SlidingUpPanelLayout.PanelState,
                newState: SlidingUpPanelLayout.PanelState
            ) {
                // Change the icon and text when the panel state changes
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    panelIcon.setImageResource(R.drawable.ic_swipe_up)
                    panelText.setText(R.string.swipe_up)
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED || newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    panelIcon.setImageResource(R.drawable.ic_swipe_down)
                    panelText.setText(R.string.swipe_down)
                }
            }
        })
        // Log user out when the back button is pressed - only logged in users are allowed in this activity
        onBackPressedDispatcher.addCallback(this) {
            FirebaseAuth.getInstance().signOut()
            val loginIntent = Intent(this@CalendarMainScreenActivity, LoginActivity::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    private fun fetchAndDisplayEvents(date: Date) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
        val dateString = dateFormat.format(date)

        FirebaseFirestore.getInstance().collection("Events")
            .whereEqualTo("date", dateString)
            .get()
            .addOnSuccessListener { documents ->
                val events = documents.mapNotNull { it.toObject(Event::class.java) }
                eventsAdapter.updateEvents(events)
                checkForEventsAndUpdateLabel(date)
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarMainScreenActivity", "Error getting documents: ", exception)
                // Update label even if there is an error
                checkForEventsAndUpdateLabel(date)
            }
    }

    private fun setupCalendarView() {
        val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)

        val today = Date(calendarView.date)
        dayOfWeekText.text = dayFormat.format(today)
        dateText.text = dateFormat.format(today)
        lastSelectedDate = dateFormat.format(today)

        // Fetch events for today - default
        fetchAndDisplayEvents(today)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)

            dayOfWeekText.text = dayFormat.format(cal.time)
            dateText.text = dateFormat.format(cal.time)
            lastSelectedDate = dateFormat.format(cal.time)

            // Fetch events for the selected day
            fetchAndDisplayEvents(cal.time)
        }
    }

    private fun checkForEventsAndUpdateLabel(date: Date) {
        hasEventsOnDate(date) { hasEvents ->
            runOnUiThread {
                if (hasEvents) {
                    selectedDayEventsLabel.text = getString(R.string.events_for_selected_day)
                    selectedDayEventsLabel.visibility = View.VISIBLE
                } else {
                    selectedDayEventsLabel.text = getString(R.string.no_events_planned)
                    selectedDayEventsLabel.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun hasEventsOnDate(date: Date, completion: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
        val dateString = dateFormat.format(date)

        db.collection("Events")
            .whereEqualTo("date", dateString)
            .get()
            .addOnSuccessListener { documents ->
                completion(documents.size() > 0)
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarMainScreenActivity", "Error getting documents: ", exception)
                completion(false)
            }
    }

    private fun fetchAndDisplayAllEvents() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Events")
            .orderBy("date")
            .get()
            .addOnSuccessListener { documents ->
                val events = documents.toObjects(Event::class.java).sortedBy { SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).parse(it.date) }
                allEventsAdapter.updateEvents(events)
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarMainScreenActivity", "Error fetching events: ", exception)
            }
    }

    override fun onDestroy() {
        originalLocale?.let { setLocale(it) }
        super.onDestroy()
    }

    private fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun setupAddEventButton() {
        addEventButton = findViewById(R.id.addEventButton)
        addEventButton.setOnClickListener {
            // Intent for CreateEventActivity with placeholders
            val intent = Intent(this, CreateEventActivity::class.java).apply {
                putExtra("SELECTED_DATE", lastSelectedDate ?: "Date not set")
                putExtra("USER_NAME", "Name not set")
                putExtra("USER_EMAIL", "Email not set")
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Log the user out
        FirebaseAuth.getInstance().signOut()
        // Navigate back to LoginActivity
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish() // Finish this activity so the user can't navigate back to it
    }

    override fun onResume() {
        super.onResume()
        // Check if the user is logged in
        if (FirebaseAuth.getInstance().currentUser == null) {
            // If not, navigate back to LoginActivity
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            finish() // Finish this activity so the user can't navigate back to it
        }
    }
}

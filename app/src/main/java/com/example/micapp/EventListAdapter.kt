package com.example.micapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micapp.firestore.Event
import java.util.Locale

class EventListAdapter(private val eventsList: MutableList<Event>) : RecyclerView.Adapter<EventListAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryInitial: TextView = view.findViewById(R.id.categoryInitial)
        val eventName: TextView = view.findViewById(R.id.eventName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        val initial = event.category.take(1).uppercase(Locale.getDefault())
        holder.categoryInitial.text = initial

        // Set the full category name on long press.
        holder.categoryInitial.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    holder.categoryInitial.text = event.category.uppercase(Locale.getDefault())
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    holder.categoryInitial.text = initial
                    holder.categoryInitial.performClick()
                }
            }
            true
        }

        holder.eventName.text = event.name
    }



    fun updateEvents(newEvents: List<Event>) {
        eventsList.clear()
        eventsList.addAll(newEvents)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }
}

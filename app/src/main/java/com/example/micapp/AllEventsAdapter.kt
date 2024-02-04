package com.example.micapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.micapp.firestore.Event
import java.text.SimpleDateFormat
import java.util.*

class AllEventsAdapter(private val eventsList: MutableList<Event>) : RecyclerView.Adapter<AllEventsAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventName: TextView = view.findViewById(R.id.eventName)
        val eventDate: TextView = view.findViewById(R.id.eventDate)
        val categoryInitial: TextView = view.findViewById(R.id.categoryInitial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_upcoming_events, parent, false)
        return EventViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility", "SimpleDateFormat")
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.eventName.text = event.name
        holder.eventDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(event.date)!!)
        val initial = event.category.take(1).uppercase(Locale.getDefault())
        holder.categoryInitial.text = initial

        holder.categoryInitial.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    holder.categoryInitial.apply {
                        text = event.category.uppercase(Locale.getDefault())
                        setBackgroundResource(R.drawable.event_circle_expanded_shape)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    holder.categoryInitial.apply {
                        text = initial
                        setBackgroundResource(R.drawable.event_circle_shape)
                    }
                }
            }
            true
        }
    }

    override fun getItemCount() = eventsList.size

    fun updateEvents(newEvents: List<Event>) {
        eventsList.clear()
        eventsList.addAll(newEvents.sortedBy { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it.date) })
        notifyDataSetChanged()
    }
}

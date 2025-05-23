package com.example.greenaura

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(private val reminders: List<ReminderModel>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.textViewTime)
        val amPmText: TextView = itemView.findViewById(R.id.textViewAmPm)
        val dateDaysText: TextView = itemView.findViewById(R.id.textViewDateDays)
        val selectedDaysText: TextView = itemView.findViewById(R.id.selectedDaysText)
        val switchActive: Switch = itemView.findViewById(R.id.switchActive)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun getItemCount(): Int = reminders.size

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        // Setting up the time (e.g., "08:21")
        holder.timeText.text = reminder.time.substringBefore(" ")
        holder.amPmText.text = reminder.time.substringAfter(" ")

        // Setting up frequency (e.g., "Every Day", "Weekly", etc.)
        holder.dateDaysText.text = reminder.frequency

        // Handling the switch (active or inactive state)
        holder.switchActive.isChecked = reminder.isActive

        // Converting selectedDays (list of integers) to day names
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val selectedDaysString = reminder.selectedDays
            .map { dayNames[it - 1] }  // Adjust to 0-based index
            .joinToString(", ")

        // Setting the text for selected days (e.g., "Repeat: Sun, Mon, Wed")
        holder.selectedDaysText.text = "Repeat: $selectedDaysString"
    }
}

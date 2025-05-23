package com.example.greenaura


import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread
import androidx.recyclerview.widget.LinearLayoutManager


class AddPlantActivity : AppCompatActivity() {


    private lateinit var plantName: EditText
    private lateinit var taskSpinner: Spinner
    private lateinit var frequencySpinner: Spinner
    private lateinit var specificDayLayout: LinearLayout
    private lateinit var timeEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var searchButton: Button
    private lateinit var suggestionTextView: TextView
    private lateinit var suggestionCard: CardView
    private lateinit var timeSuggestionCard: CardView
    private lateinit var timeSuggestionTextView: TextView
    // reminder model
    private lateinit var reminderRecyclerView: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter
    private val reminderList = mutableListOf<ReminderModel>()


    private lateinit var sunday: CheckBox
    private lateinit var monday: CheckBox
    private lateinit var tuesday: CheckBox
    private lateinit var wednesday: CheckBox
    private lateinit var thursday: CheckBox
    private lateinit var friday: CheckBox
    private lateinit var saturday: CheckBox


    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1


    private val weatherApiKey = "6a09ada111d64fe490e84818252204"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)


        plantName = findViewById(R.id.editPlantName)
        taskSpinner = findViewById(R.id.taskSpinner)
        frequencySpinner = findViewById(R.id.frequencySpinner)
        specificDayLayout = findViewById(R.id.specificDayLayout)
        timeEditText = findViewById(R.id.timeEditText)
        saveButton = findViewById(R.id.saveButton)
        searchButton = findViewById(R.id.searchButton)
        suggestionTextView = findViewById(R.id.suggestionTextView)
        suggestionCard = findViewById(R.id.suggestionCard)
        timeSuggestionCard = findViewById(R.id.timeSuggestionCard)
        timeSuggestionTextView = findViewById(R.id.timeSuggestionTextView)


        sunday = findViewById(R.id.sunday)
        monday = findViewById(R.id.monday)
        tuesday = findViewById(R.id.tuesday)
        wednesday = findViewById(R.id.wednesday)
        thursday = findViewById(R.id.thursday)
        friday = findViewById(R.id.friday)
        saturday = findViewById(R.id.saturday)
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView)
        reminderRecyclerView.layoutManager = LinearLayoutManager(this)
        reminderAdapter = ReminderAdapter(reminderList)
        reminderRecyclerView.adapter = reminderAdapter


        val tasks = arrayOf("Watering", "Fertilizer")
        taskSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tasks)


        val frequencies = arrayOf("Every Day", "Weekly", "Specific Day")
        frequencySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)


        frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                specificDayLayout.visibility = if (position == 2) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        timeEditText.setOnClickListener {
            val c = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hourOfDay, min ->
                selectedHour = hourOfDay
                selectedMinute = min
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hourFormatted = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                timeEditText.setText(String.format("%02d:%02d %s", hourFormatted, min, amPm))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false)
            timePicker.show()
        }


        // Call the new method to fetch plant care suggestion when search button is clicked
        searchButton.setOnClickListener {
            val name = plantName.text.toString().trim()


            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a plant name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Call the new method to get the plant care suggestion
            getPlantCareSuggestion(name)
        }


        saveButton.setOnClickListener {
            val name = plantName.text.toString().trim()
            val task = taskSpinner.selectedItem.toString()
            val freq = frequencySpinner.selectedItem.toString()
            val time = timeEditText.text.toString()


            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a plant name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedHour == -1 || selectedMinute == -1) {
                Toast.makeText(this, "Please set a time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val selectedDays = mutableListOf<Int>()
            if (sunday.isChecked) selectedDays.add(Calendar.SUNDAY)
            if (monday.isChecked) selectedDays.add(Calendar.MONDAY)
            if (tuesday.isChecked) selectedDays.add(Calendar.TUESDAY)
            if (wednesday.isChecked) selectedDays.add(Calendar.WEDNESDAY)
            if (thursday.isChecked) selectedDays.add(Calendar.THURSDAY)
            if (friday.isChecked) selectedDays.add(Calendar.FRIDAY)
            if (saturday.isChecked) selectedDays.add(Calendar.SATURDAY)


            if (freq == "Specific Day" && selectedDays.isEmpty()) {
                Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener


            // Set isActive to true for the reminder
            val isActive = true


            // Save reminder with isActive field to Firestore
            val reminder = hashMapOf(
                "plantName" to name,
                "task" to task,
                "frequency" to freq,
                "time" to time,
                "selectedHour" to selectedHour,
                "selectedMinute" to selectedMinute,
                "selectedDays" to selectedDays,
                "userId" to userId,
                "isActive" to isActive // Add isActive field
            )


            Firebase.firestore.collection("reminders")
                .add(reminder)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reminder saved to Firestore!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }


            // Schedule alarm based on the selected reminder time and days
            scheduleAlarm(name, task, selectedDays)


            // Create ReminderModel with isActive set to true
            val reminderModel = ReminderModel(name, task, freq, time, selectedDays, isActive)


            // Add the reminder model to the list and update the adapter
            reminderList.add(reminderModel)
            reminderAdapter.notifyItemInserted(reminderList.size - 1)

            resetFields()
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }


    // New method for getting plant care suggestions
    private fun getPlantCareSuggestion(plantName: String) {
        fetchTemperatureFromFirestore(plantName) { plantTemps ->
            getCurrentLocationAndWeather { currentTemp ->
                if (plantTemps.isNotEmpty() && currentTemp != null) {
                    val averageTemp = plantTemps.average()


                    val suggestion = when {
                        currentTemp > averageTemp + 2 -> "It's hot and dry. Water your plant more frequently."
                        currentTemp < averageTemp - 2 -> "It's colder than usual. Reduce watering to prevent root rot."
                        else -> "Weather is just right! Maintain your regular care routine."
                    }


                    val interval = when {
                        currentTemp > averageTemp + 2 -> "Water every 12 hours"
                        currentTemp < averageTemp - 2 -> "Water every 3–4 days"
                        else -> "Water every 1–2 days"
                    }


                    runOnUiThread {
                        suggestionTextView.text = suggestion
                        suggestionCard.visibility = View.VISIBLE


                        timeSuggestionTextView.text = "Recommended reminder interval: $interval"
                        timeSuggestionCard.visibility = View.VISIBLE
                    }
                } else {
                    runOnUiThread {
                        suggestionTextView.text = "Could not fetch weather or plant temperature."
                        suggestionCard.visibility = View.VISIBLE
                        timeSuggestionCard.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun fetchTemperatureFromFirestore(plantName: String, callback: (List<Double>) -> Unit) {
        Firebase.firestore.collection("plants")
            .whereEqualTo("common name", plantName)
            .get()
            .addOnSuccessListener { docs ->
                val temps = docs.firstOrNull()?.getString("Temperature")
                    ?.split(",")
                    ?.mapNotNull { it.toDoubleOrNull() }
                    ?: emptyList()
                Log.d("Firestore", "Plant temps: $temps")
                callback(temps)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error fetching temperature from Firestore", it)
                callback(emptyList())
            }
    }


    private fun getCurrentLocationAndWeather(callback: (Double?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2000)
            callback(null)
            return
        }


        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)


        if (location != null) {
            val url = "https://api.weatherapi.com/v1/current.json?key=$weatherApiKey&q=${location.latitude},${location.longitude}"
            Log.d("Weather", "Lat: ${location.latitude}, Lon: ${location.longitude}")
            Log.d("Weather", "WeatherAPI URL: $url")
            thread {
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    val data = connection.inputStream.bufferedReader().readText()
                    Log.d("Weather", "Weather JSON: $data")
                    val json = JSONObject(data)
                    val temp = json.getJSONObject("current").getDouble("temp_c")
                    callback(temp)
                } catch (e: Exception) {
                    Log.e("Weather", "Error fetching weather", e)
                    callback(null)
                }
            }
        } else {
            callback(null)
        }
    }


    private fun scheduleAlarm(plantName: String, task: String, selectedDays: List<Int>) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        selectedDays.forEach { dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                val today = get(Calendar.DAY_OF_WEEK)
                var daysUntilNext = (dayOfWeek - today + 7) % 7
                if (daysUntilNext == 0 && before(Calendar.getInstance())) {
                    daysUntilNext = 7
                }
                add(Calendar.DAY_OF_YEAR, daysUntilNext)
            }


            val intent = Intent(this, ReminderReceiver::class.java).apply {
                putExtra("plantName", plantName)
                putExtra("task", task)
            }


            val requestCode = System.currentTimeMillis().toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d("Alarm", "Alarm set for ${calendar.time}")
        }
    }

    private fun resetFields() {
        // Clear the plant name and reset the spinners to default values
        plantName.text.clear()
        taskSpinner.setSelection(0)  // Reset task spinner to first item
        frequencySpinner.setSelection(0)  // Reset frequency spinner to first item
        specificDayLayout.visibility = View.GONE  // Hide the specific day layout by default
        timeEditText.text.clear()  // Clear time input
        sunday.isChecked = false
        monday.isChecked = false
        tuesday.isChecked = false
        wednesday.isChecked = false
        thursday.isChecked = false
        friday.isChecked = false
        saturday.isChecked = false
    }

}

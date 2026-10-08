package com.example.swc3403assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import java.util.Calendar
import java.util.Locale

class DonorActivity : ComponentActivity() {

    private lateinit var dbHelper: databasehelper
    private lateinit var etFoodTitle: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvQuantityDisplay: TextView
    private lateinit var seekBarQuantity: SeekBar
    private lateinit var etExpiryTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSubmitDonation: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor)

        // Initialize Database
        dbHelper = databasehelper(this)

        // Bind UI Elements
        etFoodTitle = findViewById(R.id.etFoodTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvQuantityDisplay = findViewById(R.id.tvQuantityDisplay)
        seekBarQuantity = findViewById(R.id.seekBarQuantity)
        etExpiryTime = findViewById(R.id.etExpiryTime)
        etLocation = findViewById(R.id.etLocation)
        btnSubmitDonation = findViewById(R.id.btnSubmitDonation)

        // Setup Spinner (Dropdown for categories)
        val categories = arrayOf("Cooked", "Bakery", "Produce")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter

        // Setup Slider/SeekBar Logic
        seekBarQuantity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvQuantityDisplay.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Setup Date & Time Picker for Expiry Field
        etExpiryTime.isFocusable = false
        etExpiryTime.isClickable = true
        etExpiryTime.setOnClickListener {
            showDateTimePicker()
        }

        // Handle Submit Button
        btnSubmitDonation.setOnClickListener {
            processDonation()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // 1. Launch Date Picker
        val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            // 2. Immediately launch Time Picker after selecting the date
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val formattedDateTime = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d %02d:%02d",
                    year,
                    monthOfYear + 1,
                    dayOfMonth,
                    hourOfDay,
                    minute
                )
                etExpiryTime.setText(formattedDateTime)
            }, currentHour, currentMinute, false)

            timePickerDialog.show()
        }, currentYear, currentMonth, currentDay)

        // Restrict selection to current time onward (no past dates)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun processDonation() {
        val title = etFoodTitle.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val quantity = seekBarQuantity.progress
        val expiry = etExpiryTime.text.toString().trim()
        val location = etLocation.text.toString().trim()

        // Input Validation
        if (title.isEmpty() || expiry.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all text fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantity == 0) {
            Toast.makeText(this, "Quantity cannot be zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Save to SQLite
        val isSaved = dbHelper.insertFoodListing(title, category, quantity, expiry, location)

        if (isSaved) {
            AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Your surplus food has been posted for recipients to claim.")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            Toast.makeText(this, "Error saving to database", Toast.LENGTH_LONG).show()
        }
    }
}

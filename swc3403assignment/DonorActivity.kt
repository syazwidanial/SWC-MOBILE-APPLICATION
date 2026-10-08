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
    private lateinit var spinnerLocation: Spinner
    private lateinit var btnSubmitDonation: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor)

        dbHelper = databasehelper(this)

        etFoodTitle = findViewById(R.id.etFoodTitle)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvQuantityDisplay = findViewById(R.id.tvQuantityDisplay)
        seekBarQuantity = findViewById(R.id.seekBarQuantity)
        etExpiryTime = findViewById(R.id.etExpiryTime)
        spinnerLocation = findViewById(R.id.spinnerLocation)
        btnSubmitDonation = findViewById(R.id.btnSubmitDonation)

        // Setup Category Spinner
        val categories = arrayOf("Cooked", "Bakery", "Produce")
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        // Setup Location Spinner (Matching RecipientActivity filter options)
        val locations = arrayOf("Wangsa Maju", "Cheras", "Setapak", "Ampang")
        spinnerLocation.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        // Setup Quantity Slider
        seekBarQuantity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvQuantityDisplay.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Setup Calendar & Clock Picker
        etExpiryTime.isFocusable = false
        etExpiryTime.isClickable = true
        etExpiryTime.setOnClickListener { showDateTimePicker() }

        btnSubmitDonation.setOnClickListener { processDonation() }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val formatted = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d %02d:%02d",
                    year, monthOfYear + 1, dayOfMonth, hourOfDay, minute
                )
                etExpiryTime.setText(formatted)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
            timePickerDialog.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun processDonation() {
        val title = etFoodTitle.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val quantity = seekBarQuantity.progress
        val expiry = etExpiryTime.text.toString().trim()
        val location = spinnerLocation.selectedItem.toString()

        if (title.isEmpty() || expiry.isEmpty()) {
            Toast.makeText(this, "Please fill in all text fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (quantity == 0) {
            Toast.makeText(this, "Quantity cannot be zero", Toast.LENGTH_SHORT).show()
            return
        }

        val isSaved = dbHelper.insertFoodListing(title, category, quantity, expiry, location)

        if (isSaved) {
            AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Your surplus food has been posted for recipients to claim.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        } else {
            Toast.makeText(this, "Error saving to database", Toast.LENGTH_LONG).show()
        }
    }
}

package com.example.swc3403assignment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDonor = findViewById<Button>(R.id.btnRoleDonor)
        val btnRecipient = findViewById<Button>(R.id.btnRoleRecipient)
        val btnVolunteer = findViewById<Button>(R.id.btnRoleVolunteer)

        btnDonor.setOnClickListener { navigateToDashboard("Donor") }
        btnRecipient.setOnClickListener { navigateToDashboard("Recipient") }
        btnVolunteer.setOnClickListener { navigateToDashboard("Volunteer") }
    }

    private fun navigateToDashboard(role: String) {
        // Save the user's role
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("USER_ROLE", role).apply()

        // Only launch the Donor screen. Bypass the others to prevent compiler errors.
        if (role == "Donor") {
            val intent = Intent(this, DonorActivity::class.java)
            startActivity(intent)
        } else {
            // Show a temporary message for Recipient and Volunteer
            Toast.makeText(this, "$role screen coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}

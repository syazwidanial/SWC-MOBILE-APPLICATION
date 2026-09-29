package com.example.swc3403assignment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("USER_ROLE", role).apply()

        when (role) {
            "Donor" -> startActivity(Intent(this, DonorActivity::class.java))
            "Recipient" -> startActivity(Intent(this, RecipientActivity::class.java))
            "Volunteer" -> startActivity(Intent(this, VolunteerActivity::class.java))
        }
    }
}
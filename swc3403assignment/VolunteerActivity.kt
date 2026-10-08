package com.example.swc3403assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class VolunteerActivity : AppCompatActivity() {

    private lateinit var dbHelper: databasehelper
    private lateinit var rvTasks: RecyclerView
    private lateinit var adapter: VolunteerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)

        dbHelper = databasehelper(this)
        rvTasks = findViewById(R.id.rvVolunteerTasks)

        findViewById<Button>(R.id.btnVolunteerToHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        setupRecyclerView()
        loadVolunteerTasks()
    }

    override fun onResume() {
        super.onResume()
        loadVolunteerTasks()
    }

    private fun setupRecyclerView() {
        adapter = VolunteerAdapter(
            items = listOf(),
            onScheduleClick = { item -> pickDateTimeForPickup(item) },
            onStatusClick = { item -> advanceTaskStatus(item) }
        )
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter
    }

    private fun loadVolunteerTasks() {
        val cursor = dbHelper.getVolunteerTasks()
        val tasks = mutableListOf<FoodListingModel>()

        if (cursor.moveToFirst()) {
            do {
                tasks.add(
                    FoodListingModel(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        expiry = cursor.getString(cursor.getColumnIndexOrThrow("expiry_time")),
                        location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                        status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        claimedBy = cursor.getString(cursor.getColumnIndexOrThrow("claimed_by")),
                        pickupDateTime = cursor.getString(cursor.getColumnIndexOrThrow("pickup_datetime"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        adapter.updateData(tasks)
    }

    private fun pickDateTimeForPickup(item: FoodListingModel) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val formattedTime = String.format("%02d:%02d, %02d/%02d/%04d", hour, minute, day, month + 1, year)
                val updated = dbHelper.updatePickupTime(item.id, formattedTime)
                if (updated) {
                    Toast.makeText(this, "Pickup scheduled for $formattedTime", Toast.LENGTH_SHORT).show()
                    loadVolunteerTasks()
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun advanceTaskStatus(item: FoodListingModel) {
        when (item.status) {
            "RESERVED" -> {
                // FIXED: Changed "IN_TRANSIT" to "IN TRANSIT" (space) to match the database query
                val updated = dbHelper.updateFoodStatus(item.id, "IN TRANSIT")
                if (updated) {
                    Toast.makeText(this, "Status updated to IN TRANSIT", Toast.LENGTH_SHORT).show()
                    loadVolunteerTasks()
                }
            }
            "IN TRANSIT", "IN_TRANSIT" -> {
                // Handles both space and underscore variants safely
                val updated = dbHelper.updateFoodStatus(item.id, "DELIVERED")
                if (updated) {
                    Toast.makeText(this, "DELIVERED! Task moved to History Log.", Toast.LENGTH_SHORT).show()
                    loadVolunteerTasks()
                }
            }
        }
    }
}

class VolunteerAdapter(
    private var items: List<FoodListingModel>,
    private val onScheduleClick: (FoodListingModel) -> Unit,
    private val onStatusClick: (FoodListingModel) -> Unit
) : RecyclerView.Adapter<VolunteerAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvTaskTitle)
        val tvStatus: TextView = v.findViewById(R.id.tvTaskStatus)
        val tvClaimedBy: TextView = v.findViewById(R.id.tvTaskClaimedBy)
        val tvPickupLoc: TextView = v.findViewById(R.id.tvTaskPickupLoc)
        val tvScheduleTime: TextView = v.findViewById(R.id.tvTaskScheduleTime)
        val btnSchedule: Button = v.findViewById(R.id.btnSchedulePickup)
        val btnAdvance: Button = v.findViewById(R.id.btnAdvanceStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_volunteer_task, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvStatus.text = item.status
        holder.tvClaimedBy.text = "Reserved for: ${item.claimedBy ?: "Shelter"}"
        holder.tvPickupLoc.text = "Pickup at: ${item.location}"
        holder.tvScheduleTime.text = if (!item.pickupDateTime.isNullOrEmpty()) {
            "Pickup Window: ${item.pickupDateTime}"
        } else {
            "Pickup Window: Not scheduled yet"
        }

        if (item.status == "RESERVED") {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
            holder.tvStatus.setTextColor(Color.parseColor("#E65100"))
            holder.btnAdvance.text = "Mark Picked Up"
            holder.btnAdvance.setBackgroundColor(Color.parseColor("#EF6C00"))
            holder.btnSchedule.visibility = View.VISIBLE
        } else {
            // Displays for "IN TRANSIT"
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
            holder.tvStatus.setTextColor(Color.parseColor("#1565C0"))
            holder.btnAdvance.text = "Confirm Delivered"
            holder.btnAdvance.setBackgroundColor(Color.parseColor("#2E7D32"))
            holder.btnSchedule.visibility = View.GONE
        }

        holder.btnSchedule.setOnClickListener { onScheduleClick(item) }
        holder.btnAdvance.setOnClickListener { onStatusClick(item) }
    }

    fun updateData(newList: List<FoodListingModel>) {
        items = newList
        notifyDataSetChanged()
    }
}

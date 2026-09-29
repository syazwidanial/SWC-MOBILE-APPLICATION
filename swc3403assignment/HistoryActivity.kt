package com.example.swc3403assignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: databasehelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = databasehelper(this)

        val tvTotalCompleted: TextView = findViewById(R.id.tvTotalCompleted)
        val tvTotalServings: TextView = findViewById(R.id.tvTotalServings)
        val rvHistory: RecyclerView = findViewById(R.id.rvHistoryLog)

        val cursor = dbHelper.getCompletedHistory()
        val historyList = mutableListOf<FoodListingModel>()
        var totalServings = 0

        if (cursor.moveToFirst()) {
            do {
                val qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                totalServings += qty

                historyList.add(
                    FoodListingModel(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        quantity = qty,
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

        tvTotalCompleted.text = historyList.size.toString()
        tvTotalServings.text = totalServings.toString()

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = HistoryAdapter(historyList)
    }
}

class HistoryAdapter(private val list: List<FoodListingModel>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvHistTitle)
        val tvRecipient: TextView = v.findViewById(R.id.tvHistRecipient)
        val tvLoc: TextView = v.findViewById(R.id.tvHistLoc)
        val tvTime: TextView = v.findViewById(R.id.tvHistTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_food, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTitle.text = "${item.quantity}x ${item.title} (${item.category})"
        holder.tvRecipient.text = "Delivered to: ${item.claimedBy ?: "Shelter"}"
        holder.tvLoc.text = "Origin: ${item.location}"
        holder.tvTime.text = "Pickup window: ${item.pickupDateTime ?: "Completed"}"
    }
}
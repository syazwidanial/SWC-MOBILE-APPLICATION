package com.example.swc3403assignment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class FoodListingModel(
    val id: Int,
    val title: String,
    val category: String,
    val quantity: Int,
    val expiry: String,
    val location: String,
    val status: String,
    val claimedBy: String?,
    val pickupDateTime: String?
)

class RecipientActivity : AppCompatActivity() {

    private lateinit var dbHelper: databasehelper
    private lateinit var etSearch: EditText
    private lateinit var spinnerCat: Spinner
    private lateinit var spinnerLoc: Spinner
    private lateinit var rvListings: RecyclerView
    private lateinit var adapter: RecipientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipient)

        dbHelper = databasehelper(this)

        etSearch = findViewById(R.id.etRecipientSearch)
        spinnerCat = findViewById(R.id.spinnerRecipientCat)
        spinnerLoc = findViewById(R.id.spinnerRecipientLoc)
        rvListings = findViewById(R.id.rvRecipientListings)

        findViewById<Button>(R.id.btnRecipientToHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        setupFilters()
        setupRecyclerView()
        loadAvailableFood()
    }

    override fun onResume() {
        super.onResume()
        loadAvailableFood()
    }

    private fun setupFilters() {
        val categories = arrayOf("All", "Cooked", "Bakery", "Produce")
        val locations = arrayOf("All", "Wangsa Maju Block B", "Wangsa Maju", "Cheras", "Setapak", "Ampang")

        spinnerCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerLoc.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) = loadAvailableFood()
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spinnerCat.onItemSelectedListener = filterListener
        spinnerLoc.onItemSelectedListener = filterListener

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = loadAvailableFood()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = RecipientAdapter(listOf()) { item ->
            promptReservationDialog(item)
        }
        rvListings.layoutManager = LinearLayoutManager(this)
        rvListings.adapter = adapter
    }

    private fun loadAvailableFood() {
        val keyword = etSearch.text.toString().trim()
        val cat = spinnerCat.selectedItem?.toString() ?: "All"
        val loc = spinnerLoc.selectedItem?.toString() ?: "All"

        val cursor = dbHelper.searchAvailableFood(keyword, cat, loc)
        val list = mutableListOf<FoodListingModel>()

        if (cursor.moveToFirst()) {
            do {
                list.add(
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
        adapter.updateData(list)
    }

    private fun promptReservationDialog(item: FoodListingModel) {
        val input = EditText(this).apply {
            hint = "Organization Name (e.g. Rumah Kasih)"
        }

        AlertDialog.Builder(this)
            .setTitle("Reserve ${item.title}")
            .setMessage("Quantity: ${item.quantity} Servings\nLocation: ${item.location}")
            .setView(input)
            .setPositiveButton("Confirm Reservation") { _, _ ->
                val orgName = input.text.toString().trim()
                val recipientName = if (orgName.isNotEmpty()) orgName else "Welfare Recipient"

                // Updates status to RESERVED and attaches recipient name
                val updated = dbHelper.updateFoodStatus(item.id, "RESERVED", recipientName)
                if (updated) {
                    Toast.makeText(this, "Reserved successfully for $recipientName!", Toast.LENGTH_SHORT).show()
                    loadAvailableFood()
                } else {
                    Toast.makeText(this, "Failed to reserve item", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class RecipientAdapter(
    private var items: List<FoodListingModel>,
    private val onReserveClick: (FoodListingModel) -> Unit
) : RecyclerView.Adapter<RecipientAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tvCardTitle)
        val tvMeta: TextView = v.findViewById(R.id.tvCardMeta)
        val tvExpiry: TextView = v.findViewById(R.id.tvCardExpiry)
        val btnReserve: Button = v.findViewById(R.id.btnCardReserve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipient_food, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvMeta.text = "${item.category} • ${item.quantity} Servings • ${item.location}"
        holder.tvExpiry.text = "Expiry: ${item.expiry}"
        holder.btnReserve.setOnClickListener { onReserveClick(item) }
    }

    fun updateData(newList: List<FoodListingModel>) {
        items = newList
        notifyDataSetChanged()
    }
}
package com.example.swc3403assignment

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Class name updated to lowercase to match your other files
class databasehelper(context: Context) : SQLiteOpenHelper(context, "SurplusFoodDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE FoodListings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                category TEXT,
                quantity INTEGER,
                expiry_time TEXT,
                location TEXT,
                status TEXT DEFAULT 'AVAILABLE',
                claimed_by TEXT,
                pickup_datetime TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS FoodListings")
        onCreate(db)
    }

    fun insertFoodListing(title: String, category: String, quantity: Int, expiry: String, location: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("category", category)
            put("quantity", quantity)
            put("expiry_time", expiry)
            put("location", location)
            put("status", "AVAILABLE")
        }
        val result = db.insert("FoodListings", null, values)
        return result != -1L
    }

    fun searchAvailableFood(keyword: String, categoryFilter: String, locationFilter: String): Cursor {
        val db = this.readableDatabase
        var query = "SELECT * FROM FoodListings WHERE status = 'AVAILABLE'"
        val args = mutableListOf<String>()

        if (keyword.isNotEmpty()) {
            query += " AND title LIKE ?"
            args.add("%$keyword%")
        }
        if (categoryFilter != "All") {
            query += " AND category = ?"
            args.add(categoryFilter)
        }
        if (locationFilter != "All") {
            query += " AND location = ?"
            args.add(locationFilter)
        }
        return db.rawQuery(query, args.toTypedArray())
    }

    fun updateFoodStatus(id: Int, newStatus: String, claimedBy: String = ""): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("status", newStatus)
            if (claimedBy.isNotEmpty()) {
                put("claimed_by", claimedBy)
            }
        }
        val result = db.update("FoodListings", values, "id = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun updatePickupTime(id: Int, pickupDateTime: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("pickup_datetime", pickupDateTime)
        }
        val result = db.update("FoodListings", values, "id = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun getCompletedHistory(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM FoodListings WHERE status = 'DELIVERED'", null)
    }
}

package com.example.swc3403assignment

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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

    fun getVolunteerTasks(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery(
            "SELECT * FROM FoodListings WHERE status = 'RESERVED' OR status = 'IN TRANSIT' OR status = 'IN_TRANSIT'",
            null
        )
    }

    // Force seeds the data so you can test immediately
    fun seedSampleData(forceReset: Boolean = true) {
        val db = this.writableDatabase

        if (forceReset) {
            db.delete("FoodListings", null, null) // Clears leftover mock items
        } else {
            val cursor = db.rawQuery("SELECT COUNT(*) FROM FoodListings", null)
            var count = 0
            if (cursor.moveToFirst()) count = cursor.getInt(0)
            cursor.close()
            if (count > 0) return
        }

        val sampleItems = listOf(
            // 1. AVAILABLE - For Recipient Search & Filter test (Bakery)
            ContentValues().apply {
                put("title", "20 Croissants & Danish Pastries")
                put("category", "Bakery")
                put("quantity", 20)
                put("expiry_time", "2026-10-09 22:00")
                put("location", "Wangsa Maju Block B")
                put("status", "AVAILABLE")
            },
            // 2. AVAILABLE - For Recipient Search & Filter test (Cooked)
            ContentValues().apply {
                put("title", "15 Bento Fried Rice Boxes")
                put("category", "Cooked")
                put("quantity", 15)
                put("expiry_time", "2026-10-09 20:30")
                put("location", "Cheras")
                put("status", "AVAILABLE")
            },
            // 3. AVAILABLE - For Recipient Reservation test (Produce)
            ContentValues().apply {
                put("title", "Fresh Apples & Bananas (10kg)")
                put("category", "Produce")
                put("quantity", 10)
                put("expiry_time", "2026-10-10 18:00")
                put("location", "Setapak")
                put("status", "AVAILABLE")
            },
            // 4. RESERVED - For Volunteer status test (RESERVED -> IN TRANSIT)
            ContentValues().apply {
                put("title", "25 Packets Mee Goreng")
                put("category", "Cooked")
                put("quantity", 25)
                put("expiry_time", "2026-10-09 14:00")
                put("location", "Wangsa Maju")
                put("status", "RESERVED")
                put("claimed_by", "Rumah Kasih Orphanage")
                put("pickup_datetime", "2026-10-09 10:30")
            },
            // 5. IN TRANSIT - For Volunteer delivery completion test (IN TRANSIT -> DELIVERED)
            ContentValues().apply {
                put("title", "12 Loaves Wholemeal Bread")
                put("category", "Bakery")
                put("quantity", 12)
                put("expiry_time", "2026-10-10 12:00")
                put("location", "Ampang")
                put("status", "IN TRANSIT")
                put("claimed_by", "Hope Community Center")
                put("pickup_datetime", "2026-10-09 09:00")
            },
            // 6. DELIVERED - For History audit log & metric calculation
            ContentValues().apply {
                put("title", "30 Packets Nasi Lemak")
                put("category", "Cooked")
                put("quantity", 30)
                put("expiry_time", "2026-10-08 12:00")
                put("location", "Cheras")
                put("status", "DELIVERED")
                put("claimed_by", "Pertiwi Soup Kitchen")
                put("pickup_datetime", "2026-10-08 08:30")
            },
            // 7. DELIVERED - For History audit log & metric calculation
            ContentValues().apply {
                put("title", "18kg Assorted Organic Vegetables")
                put("category", "Produce")
                put("quantity", 18)
                put("expiry_time", "2026-10-07 18:00")
                put("location", "Wangsa Maju")
                put("status", "DELIVERED")
                put("claimed_by", "An-Nur Welfare Home")
                put("pickup_datetime", "2026-10-07 16:00")
            }
        )

        for (item in sampleItems) {
            db.insert("FoodListings", null, item)
        }
    }
}

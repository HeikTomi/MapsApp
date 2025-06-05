package com.example.mapsapp

import android.content.ContentValues
import android.database.Cursor

class DatabaseManager(private val dbHelper: DatabaseHelper) {

    fun insertData(title: String, lat: Double, lng: Double): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_TITLE, title)
            put(DatabaseHelper.COLUMN_LATITUDE, lat)
            put(DatabaseHelper.COLUMN_LONGITUDE, lng)
        }
        return db.insert(DatabaseHelper.TABLE_NAME, null, values)
    }

    fun getData(): MutableList<Place> {
        val db = dbHelper.readableDatabase
        // Sorting by timestamp in descending order
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME,
            arrayOf(DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_LATITUDE, DatabaseHelper.COLUMN_LONGITUDE),
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_TIMESTAMP} DESC"
        )
        val dataList = mutableListOf<Place>()
        with(cursor) {
            while (moveToNext()) {
                val title = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE))
                val lat = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE))
                val lng = getDouble(getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE))

                dataList.add(Place(title, lat, lng)) // Assuming Place is a data class
            }
        }
        cursor.close()
        return dataList
    }

    fun deleteData(title: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(DatabaseHelper.TABLE_NAME, "${DatabaseHelper.COLUMN_TITLE} = ?", arrayOf(title))
    }
}
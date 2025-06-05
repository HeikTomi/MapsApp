package com.example.mapsapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "places.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "places_table"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_LATITUDE = "lat"
        const val COLUMN_LONGITUDE = "lng"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE TEXT UNIQUE NOT NULL,"+
                "$COLUMN_LATITUDE REAL NOT NULL, " +
                "$COLUMN_LONGITUDE REAL NOT NULL, " +
                "$COLUMN_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"
        db.execSQL(createTable)

        // Insert initial data (Default location)
        val insertInitialData = "INSERT INTO $TABLE_NAME ($COLUMN_TITLE, $COLUMN_LATITUDE, $COLUMN_LONGITUDE) VALUES " +
                "('Lahti, Keskusta', 60.9827, 25.6615);"
        db.execSQL(insertInitialData)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}
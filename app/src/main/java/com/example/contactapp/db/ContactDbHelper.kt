package com.example.contactapp.db

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.contactapp.util.PhoneUtils

class ContactDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{
    override fun onCreate(db: SQLiteDatabase)
    {
        db.execSQL(SQL_CREATE_CONTACTS)
        try {
            db.execSQL(SQL_CREATE_INDEX_PHONE_NORM)
        } catch (e: Exception) {
            // ignore
        }
        db.execSQL(SQL_CREATE_MESSAGES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
    {
        // Non-destructive migration strategy: add new columns when upgrading from v1 to v2
        if (oldVersion < 2)
        {
            try {
                db.execSQL("ALTER TABLE $TABLE_CONTACTS ADD COLUMN $COLUMN_PHONE_NORM TEXT")
            } catch (e: Exception) {
                // column may already exist, ignore
            }
            try {
                db.execSQL("ALTER TABLE $TABLE_CONTACTS ADD COLUMN $COLUMN_PHOTO TEXT")
            } catch (e: Exception) {
                // ignore if exists
            }

            // Backfill phone_norm for existing rows using PhoneUtils.normalize
            try {
                val cursor = db.query(TABLE_CONTACTS, arrayOf(COLUMN_ID, COLUMN_PHONE), null, null, null, null, null)
                cursor.use {
                    while (it.moveToNext()) {
                        val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                        val phone = it.getString(it.getColumnIndexOrThrow(COLUMN_PHONE))
                        val norm = PhoneUtils.normalize(phone)
                        val cv = ContentValues().apply { put(COLUMN_PHONE_NORM, norm) }
                        db.update(TABLE_CONTACTS, cv, "$COLUMN_ID = ?", arrayOf(id.toString()))
                    }
                }
            } catch (e: Exception) {
                Log.e("ContactDbHelper", "Error backfilling phone_norm: ${e.message}")
            }
        }
        if (oldVersion < 3)
        {
            try {
                db.execSQL("ALTER TABLE $TABLE_CONTACTS ADD COLUMN $COLUMN_FAVORITE INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // ignore
            }
        }
        if (oldVersion < 4)
        {
            try {
                // Clear all problematic photo URIs that might cause SecurityException
                db.execSQL("UPDATE $TABLE_CONTACTS SET $COLUMN_PHOTO = NULL")
            } catch (e: Exception) {
                // ignore
            }
        }
        // Future migrations can be handled here
    }

    companion object
    {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 4

        const val TABLE_CONTACTS = "contacts"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_PHONE_NORM = "phone_norm"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_ADDRESS = "address"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_PHOTO = "photo_uri"
        const val COLUMN_FAVORITE = "is_favorite"

        const val TABLE_MESSAGES = "messages"
        const val MSG_ID = "id"
        const val MSG_CONTACT_ID = "contact_id"
        const val MSG_SENDER = "sender_number"
        const val MSG_BODY = "body"
        const val MSG_TIMESTAMP = "timestamp"
        const val MSG_INCOMING = "incoming"

        private val SQL_CREATE_CONTACTS = """
            CREATE TABLE $TABLE_CONTACTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_PHONE_NORM TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_NOTES TEXT,
                $COLUMN_PHOTO TEXT,
                $COLUMN_FAVORITE INTEGER DEFAULT 0
            )
        """.trimIndent()

        private const val SQL_CREATE_INDEX_PHONE_NORM = "CREATE INDEX IF NOT EXISTS idx_phone_norm ON $TABLE_CONTACTS($COLUMN_PHONE_NORM)"

        private val SQL_CREATE_MESSAGES = """
            CREATE TABLE $TABLE_MESSAGES (
                $MSG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $MSG_CONTACT_ID INTEGER,
                $MSG_SENDER TEXT,
                $MSG_BODY TEXT,
                $MSG_TIMESTAMP INTEGER,
                $MSG_INCOMING INTEGER
            )
        """.trimIndent()
    }
}

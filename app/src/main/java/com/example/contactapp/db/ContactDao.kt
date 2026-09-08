package com.example.contactapp.db

import android.content.ContentValues
import android.database.Cursor
import com.example.contactapp.Contact
import com.example.contactapp.util.PhoneUtils

class ContactDao(private val helper: ContactDbHelper)
{
    fun insertContact(contact: Contact): Long
    {
        val db = helper.writableDatabase
        // Avoid inserting duplicates: if a contact with same normalized phone exists, update instead
        val existing = findByPhone(contact.phone)
        if (existing != null)
        {
            // Update existing record and return its id
            val id = existing.id
            val rows = updateContact(contact.copy(id = id))
            return if (rows > 0) id else -1L
        }
        val values = ContentValues().apply {
            put(ContactDbHelper.COLUMN_NAME, contact.name)
            put(ContactDbHelper.COLUMN_PHONE, contact.phone)
            put(ContactDbHelper.COLUMN_PHONE_NORM, PhoneUtils.normalize(contact.phone))
            put(ContactDbHelper.COLUMN_EMAIL, contact.email)
            put(ContactDbHelper.COLUMN_ADDRESS, contact.address)
            put(ContactDbHelper.COLUMN_NOTES, contact.notes)
            put(ContactDbHelper.COLUMN_PHOTO, contact.photoUri)
        }
        return db.insert(ContactDbHelper.TABLE_CONTACTS, null, values)
    }

    fun updateContact(contact: Contact): Int
    {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(ContactDbHelper.COLUMN_NAME, contact.name)
            put(ContactDbHelper.COLUMN_PHONE, contact.phone)
            put(ContactDbHelper.COLUMN_PHONE_NORM, PhoneUtils.normalize(contact.phone))
            put(ContactDbHelper.COLUMN_EMAIL, contact.email)
            put(ContactDbHelper.COLUMN_ADDRESS, contact.address)
            put(ContactDbHelper.COLUMN_NOTES, contact.notes)
            put(ContactDbHelper.COLUMN_PHOTO, contact.photoUri)
        }
        val where = "${ContactDbHelper.COLUMN_ID} = ?"
        return db.update(ContactDbHelper.TABLE_CONTACTS, values, where, arrayOf(contact.id.toString()))
    }

    fun deleteContact(id: Long): Int
    {
        val db = helper.writableDatabase
        val where = "${ContactDbHelper.COLUMN_ID} = ?"
        return db.delete(ContactDbHelper.TABLE_CONTACTS, where, arrayOf(id.toString()))
    }

    fun getContact(id: Long): Contact?
    {
        val db = helper.readableDatabase
        val cursor = db.query(ContactDbHelper.TABLE_CONTACTS, null, "${ContactDbHelper.COLUMN_ID} = ?", arrayOf(id.toString()), null, null, null)
        cursor.use {
            if (it.moveToFirst())
            {
                return cursorToContact(it)
            }
        }
        return null
    }

    fun getAllContacts(): List<Contact>
    {
        val list = mutableListOf<Contact>()
        val db = helper.readableDatabase
        val cursor = db.query(ContactDbHelper.TABLE_CONTACTS, null, null, null, null, null, "${ContactDbHelper.COLUMN_NAME} ASC")
        cursor.use {
            while (it.moveToNext())
            {
                list.add(cursorToContact(it))
            }
        }
        return list
    }

    fun findByPhone(phone: String): Contact?
    {
        val db = helper.readableDatabase
        val norm = PhoneUtils.normalize(phone)
        // Try exact normalized match first
        val cursor = db.query(ContactDbHelper.TABLE_CONTACTS, null, "${ContactDbHelper.COLUMN_PHONE_NORM} = ?", arrayOf(norm), null, null, null)
        cursor.use {
            if (it.moveToFirst()) return cursorToContact(it)
        }
        // Fallback: search by suffix match (last digits)
        if (norm.length > 6)
        {
            val suffix = norm.takeLast(9)
            val c2 = db.query(ContactDbHelper.TABLE_CONTACTS, null, "${ContactDbHelper.COLUMN_PHONE_NORM} LIKE ?", arrayOf("%$suffix"), null, null, null)
            c2.use {
                if (it.moveToFirst()) return cursorToContact(it)
            }
        }
        return null
    }

    private fun cursorToContact(c: Cursor): Contact
    {
        val id = c.getLong(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_ID))
        val name = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_NAME))
        val phone = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_PHONE))
        val email = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_EMAIL))
        val address = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_ADDRESS))
        val notes = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_NOTES))
        val photo = if (c.getColumnIndex(ContactDbHelper.COLUMN_PHOTO) >= 0) c.getString(c.getColumnIndexOrThrow(ContactDbHelper.COLUMN_PHOTO)) else null
        return Contact(id = id, name = name ?: "", phone = phone ?: "", email = email ?: "", address = address ?: "", notes = notes ?: "", photoUri = photo)
    }
}

package com.example.contactapp.db

import android.content.ContentValues
import android.database.Cursor
import com.example.contactapp.Message

class MessageDao(private val helper: ContactDbHelper)
{
    fun insertMessage(message: Message): Long
    {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put(ContactDbHelper.MSG_CONTACT_ID, message.contactId)
            put(ContactDbHelper.MSG_SENDER, message.sender)
            put(ContactDbHelper.MSG_BODY, message.body)
            put(ContactDbHelper.MSG_TIMESTAMP, message.timestamp)
            put(ContactDbHelper.MSG_INCOMING, if (message.incoming) 1 else 0)
        }
        return db.insert(ContactDbHelper.TABLE_MESSAGES, null, values)
    }

    fun getConversationByContact(contactId: Long): List<Message>
    {
        val list = mutableListOf<Message>()
        val db = helper.readableDatabase
        val cursor = db.query(ContactDbHelper.TABLE_MESSAGES, null, "${ContactDbHelper.MSG_CONTACT_ID} = ?", arrayOf(contactId.toString()), null, null, "${ContactDbHelper.MSG_TIMESTAMP} ASC")
        cursor.use {
            while (it.moveToNext())
            {
                list.add(cursorToMessage(it))
            }
        }
        return list
    }

    fun getMessagesBySender(sender: String): List<Message>
    {
        val list = mutableListOf<Message>()
        val db = helper.readableDatabase
        val cursor = db.query(ContactDbHelper.TABLE_MESSAGES, null, "${ContactDbHelper.MSG_SENDER} = ?", arrayOf(sender), null, null, "${ContactDbHelper.MSG_TIMESTAMP} ASC")
        cursor.use {
            while (it.moveToNext()) list.add(cursorToMessage(it))
        }
        return list
    }

    private fun cursorToMessage(c: Cursor): Message
    {
        val id = c.getLong(c.getColumnIndexOrThrow(ContactDbHelper.MSG_ID))
        val contactId = if (c.isNull(c.getColumnIndexOrThrow(ContactDbHelper.MSG_CONTACT_ID))) 0L else c.getLong(c.getColumnIndexOrThrow(ContactDbHelper.MSG_CONTACT_ID))
        val sender = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.MSG_SENDER)) ?: ""
        val body = c.getString(c.getColumnIndexOrThrow(ContactDbHelper.MSG_BODY)) ?: ""
        val ts = c.getLong(c.getColumnIndexOrThrow(ContactDbHelper.MSG_TIMESTAMP))
        val incoming = c.getInt(c.getColumnIndexOrThrow(ContactDbHelper.MSG_INCOMING)) == 1
        return Message(id = id, contactId = contactId, sender = sender, body = body, timestamp = ts, incoming = incoming)
    }
}

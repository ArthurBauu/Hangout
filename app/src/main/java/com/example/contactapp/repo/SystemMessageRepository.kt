package com.example.contactapp.repo

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import android.provider.Telephony
import com.example.contactapp.Message
import com.example.contactapp.db.ContactDao
import com.example.contactapp.db.ContactDbHelper

class SystemMessageRepository(private val context: Context) : IMessageRepository {

    private val contactDao = ContactDao(ContactDbHelper(context))

    override fun getConversationByContact(contactId: Long): List<Message> {
        val contact = contactDao.getContact(contactId)
        val phone = contact?.phone ?: return emptyList()
        
        val messages = mutableListOf<Message>()
        val uri = "content://sms/".toUri()
        val projection = arrayOf("_id", "address", "body", "date", "type")
        
        // We filter by address. 
        // Improvement: use normalized number for better matching if needed.
        // System SMS database usually stores numbers as they appear.
        val cursor = context.contentResolver.query(
            uri, 
            projection, 
            "address LIKE ?", 
            arrayOf("%${phone.takeLast(9)}"), 
            "date ASC"
        )

        cursor?.use {
            val idIdx = it.getColumnIndex("_id")
            val bodyIdx = it.getColumnIndex("body")
            val dateIdx = it.getColumnIndex("date")
            val typeIdx = it.getColumnIndex("type")
            
            while (it.moveToNext()) {
                val id = it.getLong(idIdx)
                val body = it.getString(bodyIdx) ?: ""
                val date = it.getLong(dateIdx)
                val type = it.getInt(typeIdx)
                
                // type 1 = incoming (MESSAGE_TYPE_INBOX), type 2 = outgoing (MESSAGE_TYPE_SENT)
                val incoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX
                
                messages.add(Message(
                    id = id,
                    contactId = contactId,
                    sender = phone,
                    body = body,
                    timestamp = date,
                    incoming = incoming
                ))
            }
        }
        
        // Bonus: if the address in system DB is normalized differently, we might need a better query.
        // For now, this handles exact matches.
        
        return messages
    }

    override fun insert(message: Message): Long {
        // We manually insert the message into the system SENT box.
        // This ensures it appears in the system history and our app history.
        return try {
            val values = android.content.ContentValues().apply {
                put("address", message.sender)
                put("body", message.body)
                put("date", System.currentTimeMillis())
                put("read", 1)
                put("type", Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            val uri = context.contentResolver.insert(Uri.parse("content://sms/sent"), values)
            uri?.lastPathSegment?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}

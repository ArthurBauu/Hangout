package com.example.contactapp.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.ContactDao
import com.example.contactapp.Contact

class SmsReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        {
            val helper = ContactDbHelper(context)
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (msg in messages)
            {
                val from = msg.originatingAddress
                val body = msg.messageBody
    			Log.d("ContactApp", "SMS from=$from body=$body")
    			// Try to find contact by phone; if not found create minimal contact
                val dao = ContactDao(helper)
                var contact = dao.findByPhone(from ?: "")
    			var createdNew = false
    			if (contact == null)
    			{
                    val newContact = Contact(name = from ?: "", phone = from ?: "", photoUri = "")
    				val newId = dao.insertContact(newContact)
    				contact = dao.getContact(newId)
    				createdNew = true
    			}
    			// System will handle message insertion in its own DB.
    			// If we created a new contact, notify the user and offer to edit details
    			if (createdNew && contact != null)
    			{
    				notifyNewContact(context, contact)
    			}
            }
        }
    }

        @SuppressLint("NotificationPermission")
        private fun notifyNewContact(context: Context, contact: com.example.contactapp.Contact)
        {
            val channelId = "sms_channel"
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                val ch = NotificationChannel(channelId, "SMS Events", NotificationManager.IMPORTANCE_DEFAULT)
                nm.createNotificationChannel(ch)
            }

            val intent = Intent(context, com.example.contactapp.ContactEditActivity::class.java).apply {
                putExtra("contact_id", contact.id)
            }
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntentWithParentStack(intent)
            val pending = stackBuilder.getPendingIntent(contact.id.toInt(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notif = NotificationCompat.Builder(context, channelId)
                .setContentTitle("New contact created")
                .setContentText("From ${contact.phone}")
                .setSmallIcon(com.example.contactapp.R.drawable.ic_contact_placeholder)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()

            // Android 13+ requires POST_NOTIFICATIONS runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    nm.notify(contact.id.toInt(), notif)
                } else {
                    // No permission to post notifications; skip notifying
                    Log.d("ContactApp", "POST_NOTIFICATIONS not granted, skipping notification")
                }
            } else {
                nm.notify(contact.id.toInt(), notif)
            }
        }
}

package com.example.contactapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.contactapp.viewmodel.ViewModelFactory
import com.example.contactapp.util.PermissionHelper
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactapp.adapter.MessageAdapter
import com.example.contactapp.viewmodel.MessageViewModel
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.ContactDao

class ConversationActivity : AppCompatActivity()
{
    private lateinit var vm: MessageViewModel
    private var contactId: Long = 0L
    private var contactName: String = ""

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        contactId = intent.getLongExtra("contact_id", 0L)
        contactName = intent.getStringExtra("contact_name") ?: ""
        title = contactName

        val factory = ViewModelFactory(application)
        vm = ViewModelProvider(this, factory).get(MessageViewModel::class.java)
        val recycler = findViewById<RecyclerView>(R.id.recyclerMessages)
        val adapter = MessageAdapter(mutableListOf())
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val tvEmpty = findViewById<android.widget.TextView>(R.id.tvNoMessages)
        vm.messages.observe(this) { list ->
            adapter.setItems(list)
            recycler.scrollToPosition(list.size - 1)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.loadConversation(contactId)

        // Setup Search
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchMessages)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        val et = findViewById<EditText>(R.id.etMessage)
        val btn = findViewById<Button>(R.id.btnSend)
        btn.setOnClickListener {
            val text = et.text.toString().trim()
            if (text.isNotEmpty())
            {
                sendSms(text)
                et.setText("")
            }
        }
        et.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { btn.performClick(); true } else false
        }
    }

    private fun sendSms(text: String)
    {
        if (PermissionHelper.missingPermissions(this).contains(Manifest.permission.SEND_SMS))
        {
            PermissionHelper.requestMissing(this)
            Toast.makeText(this, "Requesting SMS permission", Toast.LENGTH_SHORT).show()
            return
        }

        // Look up contact to get phone
        val helper = ContactDbHelper(this)
        val dao = ContactDao(helper)
        val contact = dao.getContact(contactId)
        val number = contact?.phone ?: run { Toast.makeText(this, "No number for contact", Toast.LENGTH_SHORT).show(); return }

        val sms = SmsManager.getDefault()
        try {
            // handle long messages
            val parts = sms.divideMessage(text)
            if (parts.size > 1) {
                sms.sendMultipartTextMessage(number, null, parts, null, null)
            } else {
                sms.sendTextMessage(number, null, text, null, null)
            }
            // insert outgoing message
            val message = Message(contactId = contactId, sender = number, body = text, timestamp = System.currentTimeMillis(), incoming = false)
            vm.insert(message)
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Toast.makeText(this, "Failed to send SMS: ${ex.message}", Toast.LENGTH_LONG).show()
        }
    }
}

package com.example.contactapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactapp.adapter.ContactAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.example.contactapp.util.PermissionHelper
import com.example.contactapp.db.ContactDao
import com.example.contactapp.db.ContactDbHelper

class MainActivity : AppCompatActivity()
{
    private lateinit var dbHelper: ContactDbHelper
    private lateinit var dao: ContactDao

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ContactDbHelper(this)
        dao = ContactDao(dbHelper)

        // Setup RecyclerView
        val recycler = findViewById<RecyclerView>(R.id.recyclerContacts)
            val adapter = ContactAdapter(mutableListOf()) { contact ->
                // Open conversation for contact
                val i = Intent(this, ConversationActivity::class.java)
                i.putExtra("contact_id", contact.id)
                i.putExtra("contact_name", contact.name)
                startActivity(i)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val fab = findViewById<View>(R.id.fabAdd)
        fab.setOnClickListener {
            startActivity(Intent(this, ContactEditActivity::class.java))
        }

        // Request runtime permissions if needed
        PermissionHelper.requestMissing(this)

            // Use ViewModel to load contacts
            val factory = com.example.contactapp.viewmodel.ViewModelFactory(application)
            val vm = androidx.lifecycle.ViewModelProvider(this, factory).get(com.example.contactapp.viewmodel.ContactViewModel::class.java)
            vm.contacts.observe(this) { list ->
                adapter.setItems(list)
            }
            vm.loadContacts()

            // ensure at least one sample exists (insert via ViewModel)
            vm.findById(1L) { c ->
                if (c == null)
                {
                    vm.insert(Contact(name = "Test User", phone = "+33000000000", email = "test@example.com", address = "42 campus", notes = "Init"))
                }
            }
    }

    private fun checkAndRequestPermissions()
    {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.RECEIVE_SMS)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) perms.add(Manifest.permission.SEND_SMS)
        if (perms.isNotEmpty())
        {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), 1001)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Log and handle consolidated result
        for (i in permissions.indices)
        {
            Log.d("ContactApp", "Permission ${permissions[i]} result=${grantResults.getOrNull(i)}")
        }
        if (requestCode == PermissionHelper.REQ_CODE_PERMS)
        {
            if (PermissionHelper.grantedResult(permissions, grantResults))
            {
                Log.d("ContactApp", "All requested permissions granted")
            }
            else
            {
                Log.w("ContactApp", "Some permissions were denied. Functionality may be limited.")
            }
        }
    }
}

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
    private lateinit var vm: com.example.contactapp.viewmodel.ContactViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ContactDbHelper(this)
        dao = ContactDao(dbHelper)

        // Setup RecyclerView
        val recycler = findViewById<RecyclerView>(R.id.recyclerContacts)
        val adapter = ContactAdapter(
            items = mutableListOf(),
            onClick = { contact ->
                // Open edit screen
                val i = Intent(this, ContactEditActivity::class.java)
                i.putExtra("contact_id", contact.id)
                startActivity(i)
            },
            onLongClick = { contact ->
                showDeleteConfirmation(contact)
            },
            onHistoryClick = { contact ->
                // Open conversation for contact
                val i = Intent(this, ConversationActivity::class.java)
                i.putExtra("contact_id", contact.id)
                i.putExtra("contact_name", contact.name)
                startActivity(i)
            },
            onFavoriteToggle = { contact ->
                contact.isFavorite = !contact.isFavorite
                vm.update(contact)
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Setup Search
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })

        val fab = findViewById<View>(R.id.fabAdd)
        fab.setOnClickListener {
            startActivity(Intent(this, ContactEditActivity::class.java))
        }

        // Request runtime permissions if needed
        PermissionHelper.requestMissing(this)

        // Use ViewModel to load contacts
        val factory = com.example.contactapp.viewmodel.ViewModelFactory(application)
        vm = androidx.lifecycle.ViewModelProvider(this, factory).get(com.example.contactapp.viewmodel.ContactViewModel::class.java)
        val tvEmpty = findViewById<android.widget.TextView>(R.id.tvEmpty)
        vm.contacts.observe(this) { list ->
            adapter.setItems(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.loadContacts()
    }

    override fun onResume()
    {
        super.onResume()
        if (::vm.isInitialized) {
            vm.loadContacts()
        }
    }

    private fun showDeleteConfirmation(contact: Contact)
    {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.delete_contact_title)
            .setMessage(R.string.delete_contact_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                vm.delete(contact.id)
            }
            .setNegativeButton(R.string.no, null)
            .show()
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

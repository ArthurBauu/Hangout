package com.example.contactapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import com.example.contactapp.util.PermissionHelper
import android.util.Log
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ContactEditActivity : AppCompatActivity()
{
    private lateinit var vm: com.example.contactapp.viewmodel.ContactViewModel
    private var editingId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_edit)

        val factory = com.example.contactapp.viewmodel.ViewModelFactory(application)
        vm = androidx.lifecycle.ViewModelProvider(this, factory).get(com.example.contactapp.viewmodel.ContactViewModel::class.java)

        val ivPhoto = findViewById<ImageView>(R.id.ivPhoto)
        val btnPick = findViewById<Button>(R.id.btnPickPhoto)
        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etNotes = findViewById<EditText>(R.id.etNotes)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Activity result for picking image
        var currentPhotoUri: String? = null
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                currentPhotoUri = uri.toString()
                ivPhoto.setImageURI(uri)
            }
        }

        // Check if editing existing contact
        val cid = intent.getLongExtra("contact_id", 0L)
        if (cid != 0L)
        {
            editingId = cid
            vm.findById(cid) { c ->
                runOnUiThread {
                    if (c != null)
                    {
                        etName.setText(c.name)
                        etPhone.setText(c.phone)
                        etEmail.setText(c.email)
                        etAddress.setText(c.address)
                        etNotes.setText(c.notes)
                        if (!c.photoUri.isNullOrEmpty()) ivPhoto.setImageURI(Uri.parse(c.photoUri))
                        currentPhotoUri = c.photoUri
                    }
                }
            }
        }

        btnPick.setOnClickListener { pickImage.launch("image/*") }

        btnSave.setOnClickListener {
            // Ensure permissions for contacts are present (app uses local DB but check anyway)
            val missing = PermissionHelper.missingPermissions(this)
            if (missing.isNotEmpty())
            {
                PermissionHelper.requestMissing(this)
                Log.w("ContactEdit", "Missing permissions requested before save: ${missing.joinToString()}")
                return@setOnClickListener
            }

            // Basic validation
            val nameStr = etName.text.toString().trim()
            val phoneStr = etPhone.text.toString().trim()
            if (nameStr.isEmpty())
            {
                etName.error = getString(R.string.error_name_required)
                etName.requestFocus()
                return@setOnClickListener
            }
            val digits = phoneStr.filter { it.isDigit() }
            if (digits.length < 6)
            {
                etPhone.error = getString(R.string.error_phone_invalid)
                etPhone.requestFocus()
                return@setOnClickListener
            }

            val contact = Contact(id = editingId, name = nameStr, phone = phoneStr, email = etEmail.text.toString(), address = etAddress.text.toString(), notes = etNotes.text.toString(), photoUri = currentPhotoUri)
            if (editingId != 0L)
            {
                vm.update(contact)
                finish()
            }
            else
            {
                vm.insert(contact)
                finish()
            }
        }
    }
}

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
        val cbFavorite = findViewById<android.widget.CheckBox>(R.id.cbFavorite)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        // Activity result for picking image
        var currentPhotoUri: String? = null
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val localUri = copyImageToInternalStorage(uri)
                if (localUri != null) {
                    currentPhotoUri = localUri.toString()
                    ivPhoto.setImageURI(localUri)
                }
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
                        cbFavorite.isChecked = c.isFavorite
                        if (!c.photoUri.isNullOrEmpty()) {
                            try {
                                ivPhoto.setImageURI(Uri.parse(c.photoUri))
                            } catch (_: Exception) {
                                ivPhoto.setImageResource(R.drawable.ic_contact_placeholder)
                            }
                        }
                        currentPhotoUri = c.photoUri
                        btnDelete.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }

        btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.delete_contact_title)
                .setMessage(R.string.delete_contact_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    vm.delete(editingId)
                    finish()
                }
                .setNegativeButton(R.string.no, null)
                .show()
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

            val contact = Contact(
                id = editingId, 
                name = nameStr, 
                phone = phoneStr, 
                email = etEmail.text.toString(), 
                address = etAddress.text.toString(), 
                notes = etNotes.text.toString(), 
                photoUri = currentPhotoUri,
                isFavorite = cbFavorite.isChecked
            )

            if (editingId == 0L) {
                // Creation: check for duplicates
                vm.findByName(nameStr) { existing ->
                    runOnUiThread {
                        if (existing != null) {
                            showMergeDialog(existing, contact)
                        } else {
                            vm.insert(contact)
                            finish()
                        }
                    }
                }
            } else {
                // Update
                vm.update(contact)
                finish()
            }
        }
    }

    private fun showMergeDialog(existing: Contact, newContact: Contact)
    {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.merge_title)
            .setMessage(getString(R.string.merge_message, existing.name))
            .setPositiveButton(R.string.merge) { _, _ ->
                vm.mergeContacts(existing, newContact)
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ ->
                vm.insert(newContact)
                finish()
            }
            .show()
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri?
    {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "contact_${System.currentTimeMillis()}.jpg"
            val file = java.io.File(filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("ContactEdit", "Error copying image: ${e.message}")
            null
        }
    }
}

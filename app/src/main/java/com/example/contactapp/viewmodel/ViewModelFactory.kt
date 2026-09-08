package com.example.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.ContactDao
import com.example.contactapp.db.MessageDao
import com.example.contactapp.repo.ContactRepositoryAdapter
import com.example.contactapp.repo.MessageRepositoryAdapter

class ViewModelFactory(private val app: Application) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T
    {
        val helper = ContactDbHelper(app)
        val contactDao = ContactDao(helper)
        val messageDao = MessageDao(helper)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ContactViewModel::class.java) -> ContactViewModel(app, ContactRepositoryAdapter(contactDao)) as T
            modelClass.isAssignableFrom(MessageViewModel::class.java) -> MessageViewModel(app, MessageRepositoryAdapter(messageDao)) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}

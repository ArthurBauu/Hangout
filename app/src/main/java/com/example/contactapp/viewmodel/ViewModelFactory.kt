package com.example.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.contactapp.repo.ContactRepositoryAdapter

class ViewModelFactory(private val app: Application) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T
    {
        val contactRepo = com.example.contactapp.repo.ContactRepository.getInstance(app)
        val systemMessageRepo = com.example.contactapp.repo.SystemMessageRepository(app)

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ContactViewModel::class.java) -> ContactViewModel(app, contactRepo) as T
            modelClass.isAssignableFrom(MessageViewModel::class.java) -> MessageViewModel(app, systemMessageRepo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}

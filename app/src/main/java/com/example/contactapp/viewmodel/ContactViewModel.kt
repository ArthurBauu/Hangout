package com.example.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactapp.Contact
import com.example.contactapp.repo.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.CoroutineDispatcher

class ContactViewModel(
    application: Application,
    private val repo: IContactRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application)
{


    private val _contacts = MutableLiveData<List<Contact>>(emptyList())
    val contacts: LiveData<List<Contact>> = _contacts

    fun loadContacts()
    {
        viewModelScope.launch(dispatcher) {
            val all = repo.getAllContacts()
            _contacts.postValue(all)
        }
    }

    fun insert(contact: Contact)
    {
        viewModelScope.launch(dispatcher) {
            repo.insert(contact)
            loadContacts()
        }
    }

    fun update(contact: Contact)
    {
        viewModelScope.launch(dispatcher) {
            repo.update(contact)
            loadContacts()
        }
    }

    fun delete(id: Long)
    {
        viewModelScope.launch(dispatcher) {
            repo.delete(id)
            loadContacts()
        }
    }

    fun findById(id: Long, callback: (Contact?) -> Unit)
    {
        viewModelScope.launch(dispatcher) {
            val c = repo.getContactById(id)
            callback(c)
        }
    }

    fun findByName(name: String, callback: (Contact?) -> Unit)
    {
        viewModelScope.launch(dispatcher) {
            val c = repo.findByName(name)
            callback(c)
        }
    }

    fun mergeContacts(master: Contact, duplicate: Contact)
    {
        viewModelScope.launch(dispatcher) {
            // Logic: merge fields from duplicate into master if master's fields are empty
            val merged = master.copy(
                phone = if (master.phone.isEmpty()) duplicate.phone else master.phone,
                email = if (master.email.isEmpty()) duplicate.email else master.email,
                address = if (master.address.isEmpty()) duplicate.address else master.address,
                notes = (master.notes + "\n" + duplicate.notes).trim(),
                photoUri = master.photoUri ?: duplicate.photoUri,
                isFavorite = master.isFavorite || duplicate.isFavorite
            )
            repo.update(merged)
            repo.delete(duplicate.id)
            loadContacts()
        }
    }
}

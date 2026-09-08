package com.example.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactapp.Contact
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.ContactDao
import com.example.contactapp.repo.ContactRepositoryAdapter
import com.example.contactapp.repo.IContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlinx.coroutines.CoroutineDispatcher

class ContactViewModel(application: Application, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : AndroidViewModel(application)
{
    private val helper = ContactDbHelper(application)
    private val dao = ContactDao(helper)
    private var repo: IContactRepository = ContactRepositoryAdapter(dao)

    // secondary constructor for injection/testing
    constructor(application: Application, repo: IContactRepository) : this(application, Dispatchers.IO) {
        this.repo = repo
    }

    // convenience constructor for tests to inject repo and dispatcher
    constructor(application: Application, repo: IContactRepository, dispatcher: CoroutineDispatcher) : this(application, dispatcher) {
        this.repo = repo
    }

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

    fun findById(id: Long, callback: (Contact?) -> Unit)
    {
        viewModelScope.launch(dispatcher) {
            val c = repo.getContactById(id)
            callback(c)
        }
    }
}

package com.example.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactapp.Message
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.MessageDao
import com.example.contactapp.repo.IMessageRepository
import com.example.contactapp.repo.MessageRepositoryAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageViewModel(application: Application, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : AndroidViewModel(application)
{
    private val helper = ContactDbHelper(application)
    private val dao = MessageDao(helper)
    private var repo: IMessageRepository = MessageRepositoryAdapter(dao)

    constructor(application: Application, repo: IMessageRepository) : this(application, Dispatchers.IO) {
        this.repo = repo
    }

    // convenience constructor for tests to inject repo and dispatcher
    constructor(application: Application, repo: IMessageRepository, dispatcher: CoroutineDispatcher) : this(application, dispatcher) {
        this.repo = repo
    }

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    fun loadConversation(contactId: Long)
    {
        viewModelScope.launch(dispatcher) {
            val list = repo.getConversationByContact(contactId)
            _messages.postValue(list)
        }
    }

    fun insert(message: Message)
    {
        viewModelScope.launch(dispatcher) {
            repo.insert(message)
            loadConversation(message.contactId)
        }
    }
}

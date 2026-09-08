package com.example.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactapp.Message
import com.example.contactapp.repo.IMessageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class MessageViewModel(
    application: Application,
    private val repo: IMessageRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application)
{
    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private var currentContactId: Long = -1L

    private val smsObserver = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            if (currentContactId != -1L) {
                loadConversation(currentContactId)
            }
        }
    }

    init {
        application.contentResolver.registerContentObserver(
            "content://sms/".toUri(),
            true,
            smsObserver
        )
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(smsObserver)
    }

    fun loadConversation(contactId: Long)
    {
        currentContactId = contactId
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

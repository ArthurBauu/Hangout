package com.example.contactapp.repo

import com.example.contactapp.Message

interface IMessageRepository {
    fun getConversationByContact(contactId: Long): List<Message>
    fun insert(message: Message): Long
}

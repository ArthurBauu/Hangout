package com.example.contactapp.repo

import com.example.contactapp.Message
import com.example.contactapp.db.MessageDao

class MessageRepositoryAdapter(private val dao: MessageDao) : IMessageRepository
{
    override fun getConversationByContact(contactId: Long): List<Message> = dao.getConversationByContact(contactId)
    override fun insert(message: Message): Long = dao.insertMessage(message)
}

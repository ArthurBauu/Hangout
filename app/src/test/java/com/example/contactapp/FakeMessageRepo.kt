package com.example.contactapp

import com.example.contactapp.repo.IMessageRepository

class FakeMessageRepo : IMessageRepository {
    private val storage = mutableMapOf<Long, MutableList<Message>>()
    private var seq = 1L

    override fun getConversationByContact(contactId: Long): List<Message> {
        return storage[contactId]?.toList() ?: emptyList()
    }

    override fun insert(message: Message): Long {
        val id = seq++
        val list = storage.getOrPut(message.contactId) { mutableListOf() }
        list.add(message.copy(id = id))
        return id
    }
}

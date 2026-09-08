package com.example.contactapp

import com.example.contactapp.repo.IContactRepository

class FakeContactRepo : IContactRepository {
    private val storage = mutableMapOf<Long, Contact>()
    private var seq = 1L

    override fun getAllContacts(): List<Contact> = storage.values.toList()

    override fun getContactById(id: Long): Contact? = storage[id]

    override fun findByPhone(phone: String): Contact? = storage.values.find { it.phone == phone }

    override fun insert(contact: Contact): Long {
        val id = seq++
        contact.id = id
        storage[id] = contact
        return id
    }

    override fun update(contact: Contact): Int {
        val id = contact.id
        if (storage.containsKey(id)) {
            storage[id] = contact
            return 1
        }
        return 0
    }
}

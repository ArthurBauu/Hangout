package com.example.contactapp.controller

import com.example.contactapp.Contact
import com.example.contactapp.repo.IContactRepository

class PlainContactController(private val repo: IContactRepository) {
    fun loadAll(): List<Contact> = repo.getAllContacts()
    fun insert(contact: Contact): Long = repo.insert(contact)
    fun update(contact: Contact): Int = repo.update(contact)
    fun findByPhone(phone: String): Contact? = repo.findByPhone(phone)
}

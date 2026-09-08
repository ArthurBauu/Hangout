package com.example.contactapp.repo

import com.example.contactapp.Contact

interface IContactRepository {
    fun getAllContacts(): List<Contact>
    fun getContactById(id: Long): Contact?
    fun findByPhone(phone: String): Contact?
    fun insert(contact: Contact): Long
    fun update(contact: Contact): Int
}

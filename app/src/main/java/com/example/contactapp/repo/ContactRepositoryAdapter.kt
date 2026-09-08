package com.example.contactapp.repo

import com.example.contactapp.Contact
import com.example.contactapp.db.ContactDao

class ContactRepositoryAdapter(private val dao: ContactDao) : IContactRepository
{
    override fun getAllContacts(): List<Contact> = dao.getAllContacts()

    override fun getContactById(id: Long): Contact? = dao.getContact(id)

    override fun findByPhone(phone: String): Contact? = dao.findByPhone(phone)

    override fun findByName(name: String): Contact? = dao.findByName(name)

    override fun insert(contact: Contact): Long = dao.insertContact(contact)

    override fun update(contact: Contact): Int = dao.updateContact(contact)

    override fun delete(id: Long): Int = dao.deleteContact(id)
}

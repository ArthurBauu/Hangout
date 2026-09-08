package com.example.contactapp.repo

import android.content.Context
import com.example.contactapp.Contact
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.ContactDao
import java.util.concurrent.ConcurrentHashMap

class ContactRepository private constructor(context: Context) : IContactRepository {
    private val helper = ContactDbHelper(context.applicationContext)
    private val dao = ContactDao(helper)

    // simple caches
    private val cacheById = ConcurrentHashMap<Long, Contact>()
    private val cacheByPhoneNorm = ConcurrentHashMap<String, Contact>()

    override fun getAllContacts(): List<Contact> {
        val list = dao.getAllContacts()
        list.forEach { c ->
            cacheById[c.id] = c
            if (!c.phone.isNullOrEmpty()) cacheByPhoneNorm[com.example.contactapp.util.PhoneUtils.normalize(c.phone)] = c
        }
        return list
    }

    override fun getContactById(id: Long): Contact? {
        cacheById[id]?.let { return it }
        val c = dao.getContact(id)
        if (c != null) cacheById[id] = c
        return c
    }

    override fun findByPhone(phone: String): Contact? {
        val norm = com.example.contactapp.util.PhoneUtils.normalize(phone)
        cacheByPhoneNorm[norm]?.let { return it }
        val c = dao.findByPhone(phone)
        if (c != null) {
            cacheById[c.id] = c
            if (!c.phone.isNullOrEmpty()) cacheByPhoneNorm[com.example.contactapp.util.PhoneUtils.normalize(c.phone)] = c
        }
        return c
    }

    override fun findByName(name: String): Contact? {
        // We could cache by name too, but let's keep it simple for now
        return dao.findByName(name)
    }

    override fun insert(contact: Contact): Long {
        val id = dao.insertContact(contact)
        val c = dao.getContact(id)
        if (c != null) {
            cacheById[id] = c
            if (!c.phone.isNullOrEmpty()) cacheByPhoneNorm[com.example.contactapp.util.PhoneUtils.normalize(c.phone)] = c
        }
        return id
    }

    override fun update(contact: Contact): Int {
        val res = dao.updateContact(contact)
        if (res > 0) {
            val c = dao.getContact(contact.id)
            if (c != null) {
                cacheById[c.id] = c
                if (!c.phone.isNullOrEmpty()) cacheByPhoneNorm[com.example.contactapp.util.PhoneUtils.normalize(c.phone)] = c
            }
        }
        return res
    }

    override fun delete(id: Long): Int {
        val contact = cacheById[id]
        val res = dao.deleteContact(id)
        if (res > 0) {
            cacheById.remove(id)
            contact?.phone?.let {
                cacheByPhoneNorm.remove(com.example.contactapp.util.PhoneUtils.normalize(it))
            }
        }
        return res
    }

    companion object {
        @Volatile
        private var INSTANCE: ContactRepository? = null

        fun getInstance(context: Context): ContactRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactRepository(context).also { INSTANCE = it }
            }
        }
    }
}

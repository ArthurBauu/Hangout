package com.example.contactapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactDaoInstrumentedTest {
    @Test
    fun insertAndQueryContact() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val helper = com.example.contactapp.db.ContactDbHelper(appContext)
        val dao = com.example.contactapp.db.ContactDao(helper)

        val c = Contact(name = "Unit Test", phone = "+33000000001", email = "u@test", address = "", notes = "")
        val id = dao.insertContact(c)
        val fetched = dao.getContact(id)
        assertNotNull(fetched)
        assertEquals("Unit Test", fetched?.name)
        // cleanup
        dao.deleteContact(id)
    }
}

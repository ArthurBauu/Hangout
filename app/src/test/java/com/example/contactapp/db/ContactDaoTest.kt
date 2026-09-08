package com.example.contactapp.db

import androidx.test.core.app.ApplicationProvider
import com.example.contactapp.Contact
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ContactDaoTest {
    private lateinit var helper: ContactDbHelper
    private lateinit var dao: ContactDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        helper = ContactDbHelper(ctx)
        dao = ContactDao(helper)
    }

    @After
    fun teardown() {
        helper.writableDatabase.close()
        helper.close()
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        try { ctx.deleteDatabase(ContactDbHelper.DATABASE_NAME) } catch (_: Exception) {}
    }

    @Test
    fun insertAndGet() {
        val c = Contact(name = "Alice", phone = "+1 555 1234")
        val id = dao.insertContact(c)
        assertTrue(id > 0)
        val got = dao.getContact(id)
        assertNotNull(got)
        assertEquals("Alice", got?.name)
        assertEquals("+1 555 1234", got?.phone)
    }

    @Test
    fun findByPhoneNormalized() {
        dao.insertContact(Contact(name = "Bob", phone = "+33 6 12 34 56 78"))
        val found = dao.findByPhone("06 12 34 56 78")
        assertNotNull(found)
        assertEquals("Bob", found?.name)
    }

    @Test
    fun insertDuplicateUpdates() {
        val id1 = dao.insertContact(Contact(name = "C", phone = "+1-202-555-0100"))
        val id2 = dao.insertContact(Contact(name = "C2", phone = "2025550100"))
        assertEquals(id1, id2)
        val got = dao.getContact(id1)
        assertEquals("C2", got?.name)
    }
}

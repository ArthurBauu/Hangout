package com.example.contactapp.db

import androidx.test.core.app.ApplicationProvider
import com.example.contactapp.Message
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MessageDaoTest {
    private lateinit var helper: ContactDbHelper
    private lateinit var msgDao: MessageDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        helper = ContactDbHelper(ctx)
        msgDao = MessageDao(helper)
    }

    @After
    fun teardown() {
        helper.writableDatabase.close()
        helper.close()
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        try { ctx.deleteDatabase(ContactDbHelper.DATABASE_NAME) } catch (_: Exception) {}
    }

    @Test
    fun insertAndRetrieveConversation() {
        val now = System.currentTimeMillis()
        val m1 = Message(contactId = 1L, sender = "+1000", body = "Hello", timestamp = now - 1000, incoming = true)
        val m2 = Message(contactId = 1L, sender = "+1000", body = "Reply", timestamp = now, incoming = false)
        val id1 = msgDao.insertMessage(m1)
        val id2 = msgDao.insertMessage(m2)
        assertTrue(id1 > 0)
        assertTrue(id2 > 0)
        val conv = msgDao.getConversationByContact(1L)
        assertEquals(2, conv.size)
        assertEquals("Hello", conv[0].body)
        assertEquals("Reply", conv[1].body)
    }

    @Test
    fun getMessagesBySender() {
        val sender = "+1999"
        msgDao.insertMessage(Message(contactId = 0L, sender = sender, body = "a", timestamp = 1L, incoming = true))
        msgDao.insertMessage(Message(contactId = 0L, sender = sender, body = "b", timestamp = 2L, incoming = true))
        val list = msgDao.getMessagesBySender(sender)
        assertEquals(2, list.size)
        assertEquals("a", list[0].body)
        assertEquals("b", list[1].body)
    }
}

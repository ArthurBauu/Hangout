package com.example.contactapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDaoInstrumentedTest {
    @Test
    fun insertAndQueryMessage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val helper = com.example.contactapp.db.ContactDbHelper(appContext)
        val cdao = com.example.contactapp.db.ContactDao(helper)
        val mdao = com.example.contactapp.db.MessageDao(helper)

        val c = Contact(name = "MsgTest", phone = "+33000000002")
        val cid = cdao.insertContact(c)
        val msg = Message(contactId = cid, sender = "+33000000002", body = "hello", timestamp = System.currentTimeMillis(), incoming = true)
        val mid = mdao.insertMessage(msg)
        val conv = mdao.getConversationByContact(cid)
        assertTrue(conv.isNotEmpty())
        // cleanup
        cdao.deleteContact(cid)
    }
}

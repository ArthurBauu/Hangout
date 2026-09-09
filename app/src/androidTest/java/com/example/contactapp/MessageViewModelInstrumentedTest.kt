package com.example.contactapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.contactapp.viewmodel.MessageViewModel
import com.example.contactapp.db.ContactDbHelper
import com.example.contactapp.db.ContactDao
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageViewModelInstrumentedTest {
    @Test
    fun insertAndLoadConversation() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as android.app.Application
        val helper = ContactDbHelper(appContext)
        val cdao = ContactDao(helper)
        val c = Contact(name = "MsgVM", phone = "+33000000004")
        val cid = cdao.insertContact(c)

        val repo = com.example.contactapp.repo.MessageRepositoryAdapter(com.example.contactapp.db.MessageDao(helper))
        val vm = MessageViewModel(appContext, repo)
        val msg = Message(contactId = cid, sender = "+33000000004", body = "vm hello", timestamp = System.currentTimeMillis(), incoming = true)
        vm.insert(msg)
        Thread.sleep(1500)
        vm.loadConversation(cid)
        Thread.sleep(1500)
        val list = vm.messages.value
        assertNotNull(list)
        assertTrue(list!!.any { it.body == "vm hello" })
        // cleanup
        cdao.deleteContact(cid)
    }
}

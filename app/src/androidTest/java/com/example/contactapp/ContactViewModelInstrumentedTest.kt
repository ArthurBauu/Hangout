package com.example.contactapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.contactapp.viewmodel.ContactViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactViewModelInstrumentedTest {
    @Test
    fun insertAndLoadViaViewModel() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as android.app.Application
        val vm = ContactViewModel(appContext)
        val c = Contact(name = "VM Test", phone = "+33000000003")
        vm.insert(c)
        // allow some time for coroutine to run
        Thread.sleep(500)
        vm.loadContacts()
        Thread.sleep(500)
        val list = vm.contacts.value
        assertNotNull(list)
        assertTrue(list!!.any { it.name == "VM Test" })
    }
}

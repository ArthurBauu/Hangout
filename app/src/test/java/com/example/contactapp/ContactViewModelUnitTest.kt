package com.example.contactapp

import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.example.contactapp.repo.IContactRepository
import com.example.contactapp.viewmodel.ContactViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric

class FakeContactRepoTest : IContactRepository {
    private val storage = mutableMapOf<Long, Contact>()
    private var seq = 1L

    override fun getAllContacts(): List<Contact> = storage.values.toList()

    override fun getContactById(id: Long): Contact? = storage[id]

    override fun findByPhone(phone: String): Contact? = storage.values.find { it.phone == phone }

    override fun findByName(name: String): Contact? = storage.values.find { it.name == name }

    override fun insert(contact: Contact): Long {
        val id = seq++
        contact.id = id
        storage[id] = contact
        return id
    }

    override fun update(contact: Contact): Int {
        val id = contact.id
        if (storage.containsKey(id)) {
            storage[id] = contact
            return 1
        }
        return 0
    }

    override fun delete(id: Long): Int {
        return if (storage.remove(id) != null) 1 else 0
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ContactViewModelUnitTest {

    private lateinit var repo: IContactRepository
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        repo = FakeContactRepoTest()
    }

    @Test
    fun insertAndLoadContacts() = runTest {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = ContactViewModel(app, repo, dispatcher)

        val c = Contact(name = "UT Test", phone = "+33012345678")
        vm.insert(c)
        // advance until coroutines complete
        this.testScheduler.advanceUntilIdle()
        Robolectric.flushForegroundThreadScheduler()

        vm.loadContacts()
        this.testScheduler.advanceUntilIdle()
        Robolectric.flushForegroundThreadScheduler()

        val list = vm.contacts.value
        assertTrue(list != null && list.any { it.name == "UT Test" })
    }
}

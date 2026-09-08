package com.example.contactapp

import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.example.contactapp.repo.IMessageRepository
import com.example.contactapp.viewmodel.MessageViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MessageViewModelUnitTest {

    private lateinit var repo: IMessageRepository
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        repo = FakeMessageRepo()
    }

    @Test
    fun insertAndLoadConversation() = runTest {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val vm = MessageViewModel(app, repo, dispatcher)

        val cid = 42L
        val msg = Message(contactId = cid, sender = "+330111222333", body = "hello test", timestamp = System.currentTimeMillis(), incoming = true)
        vm.insert(msg)
        this.testScheduler.advanceUntilIdle()
        Robolectric.flushForegroundThreadScheduler()

        vm.loadConversation(cid)
        this.testScheduler.advanceUntilIdle()
        Robolectric.flushForegroundThreadScheduler()

        val list = vm.messages.value
        assertTrue(list != null && list.any { it.body == "hello test" })
    }
}

package com.example.contactapp

import org.junit.Assert.*
import org.junit.Test

class PlainContactControllerTest {
    @Test
    fun insertAndFind() {
        val repo = FakeContactRepo()
        val controller = com.example.contactapp.controller.PlainContactController(repo)
        controller.insert(Contact(name = "TC", phone = "+33000123456"))
        val all = controller.loadAll()
        assertEquals(1, all.size)
        val found = controller.findByPhone("+33000123456")
        assertNotNull(found)
        assertEquals("TC", found?.name)
    }
}

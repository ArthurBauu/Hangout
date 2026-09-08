package com.example.contactapp

data class Message(
    var id: Long = 0,
    var contactId: Long = 0,
    var sender: String = "",
    var body: String = "",
    var timestamp: Long = 0,
    var incoming: Boolean = true
)

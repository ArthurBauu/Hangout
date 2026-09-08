package com.example.contactapp

data class Contact(
    var id: Long = 0,
    var name: String = "",
    var phone: String = "",
    var email: String = "",
    var address: String = "",
    var notes: String = "",
    var photoUri: String? = null,
    var isFavorite: Boolean = false
)

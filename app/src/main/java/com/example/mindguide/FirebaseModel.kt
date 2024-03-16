package com.example.mindguide

data class FirebaseModel(

    var personName: String = "",
    var personProfession: String = "",
    var personAge: String = "",
    var personAbout: String = "",
    var downloadUrl: String = "",
    var personId: String = ""
) {
    constructor() : this("", "", "", "", "","")
}



package com.example.mindguide

data class FirebaseModelPlace(

    var latitude: String = "",
    var longitude: String = "",
    var placeAbout: String = "",
    var placeTitle: String = "",
    var downloadUrl: String = "",
    var userId: String = ""
){
    constructor() : this("", "", "", "", "")
}

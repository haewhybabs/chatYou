package com.chatyou.model

class User {
    var uid:String? = null
    var name:String? = null
    var phoneNumber:String? = null
    var email:String? = null
    var profileImage:String? = null
    var userType:String? = null
    constructor(){}
    constructor(
        uid:String?,
        name:String?,
        phoneNumber: String?,
        profileImage:String?,
        userType:String,
        email:String,
    ){
        this.uid = uid
        this.name = name
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
        this.userType = userType
        this.email = email
    }
}
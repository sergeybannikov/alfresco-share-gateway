package com.sergeybannikov.alfrescogateway.model

data class AlfrescoUserModel (

    var id: String,
    var firstName: String,
    var lastName: String,
    var description: String? = null,
    var email: String = "email@example.com",
    var skypeId: String? = null,
    var googleId: String? = null,
    var instantMessageId: String? = null,
    var jobTitle: String? = null,
    var location: String? = null,
    var company: AlfrescoCompany? = null,
    var mobile: String? = null,
    var telephone: String? = null,
    var userStatus: String? = null,
    var enabled: Boolean? = null,
    var emailNotificationsEnabled: Boolean? = null,
    var password: String? = null,
    var aspectNames: List<String>? = null,
    var properties: Map<String, String>? = null
)


data class AlfrescoCompany (

    var organization: String? = null,
    var address1: String? = null,
    var address2: String? = null,
    var address3: String? = null,
    var postcode: String? = null,
    var telephone: String? = null,
    var fax: String? = null,
    var email: String? = null
)
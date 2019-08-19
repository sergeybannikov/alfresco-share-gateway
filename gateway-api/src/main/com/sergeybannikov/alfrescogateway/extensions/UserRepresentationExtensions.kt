package com.sergeybannikov.alfrescogateway.extensions

import com.sergeybannikov.alfrescogateway.model.AlfrescoUserModel
import com.sergeybannikov.alfrescogateway.repository.impl.AlfrescoGroupRepositoryImpl
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import java.util.*

val logger = LoggerFactory.getLogger(AlfrescoGroupRepositoryImpl::class.java)


fun UserRepresentation.toAlfrescoUser(): AlfrescoUserModel {


    return AlfrescoUserModel(
        this.username,
        this.firstName,
        lastName = this.lastName ?: "",
        email = if(this.email != null)  this.email else "email@example.com",
        password = UUID.randomUUID().toString()
    )
}

private fun UserRepresentation.getCommonName(): String? {
    val attrs = this.getAttributes()

    val commonNameAttr = attrs.get("commonName")

    if (commonNameAttr == null || commonNameAttr.isEmpty()) {
        logger.warn("Common name is not present for user: " + this.getUsername())
        return "${this.firstName} ${this.lastName}"
    }

    val commonName = commonNameAttr.single()

    Objects.requireNonNull(commonName, "commonName")

    return commonName
}

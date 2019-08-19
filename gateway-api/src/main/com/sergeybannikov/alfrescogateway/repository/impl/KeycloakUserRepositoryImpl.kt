package com.sergeybannikov.alfrescogateway.repository.impl

import com.sergeybannikov.alfrescogateway.repository.KeycloakUserRepository
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class KeycloakUserRepositoryImpl

@Autowired
constructor (val realmResource: RealmResource) : KeycloakUserRepository
{
    override fun  all(): List<UserRepresentation> {
        return realmResource.users().list(0, 10000)
    }

    override fun getByLogin(login: String): UserRepresentation {
        return realmResource.users().search(login)
            .filter({ u -> u.getUsername().equals(login, ignoreCase = true) })
            .single()
    }

    override fun getUserRoles(userId: String): List<String> {
        val rolesRep = realmResource.users().get(userId).roles().all
        return rolesRep.realmMappings
            .map{ it.name }
    }
}
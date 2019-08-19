package com.sergeybannikov.alfrescogateway.repository

import org.keycloak.representations.idm.UserRepresentation

interface KeycloakUserRepository {
    fun all(): List<UserRepresentation>

    fun getByLogin(login: String): UserRepresentation

    fun getUserRoles(userId: String): List<String>

}
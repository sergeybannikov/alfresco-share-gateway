package com.sergeybannikov.alfrescogateway.service.impl

import com.sergeybannikov.alfrescogateway.extensions.toAlfrescoUser
import com.sergeybannikov.alfrescogateway.repository.AlfrescoGroupRepository
import com.sergeybannikov.alfrescogateway.repository.AlfrescoUserRepository
import com.sergeybannikov.alfrescogateway.repository.KeycloakUserRepository
import com.sergeybannikov.alfrescogateway.service.UserService
import io.vavr.control.Either
import io.vavr.control.Either.left
import io.vavr.control.Either.right
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserServiceImpl
@Autowired
constructor(
    val keycloakUserRepository: KeycloakUserRepository,
    val alfrescoUserRepository: AlfrescoUserRepository,
    val alfrescoGroupRepository: AlfrescoGroupRepository
) : UserService {
    override fun ensureUser(login: String): Either<Exception, Nothing> {
        return ensureUserExists(login)
            .fold({ e -> left(e) },
                { s ->
                    ensureUserInAdminGroup(login)
                        .fold(
                            { e -> left(e) },
                            { s2 -> right(s2) })
                }
            )
    }

    private fun ensureUserExists(login: String): Either<Exception, Nothing> {
        return alfrescoUserRepository.checkExists(login)
            .fold(
                { e -> left(e) },
                { s ->
                    if (!s) {
                        createUser(login)
                    }
                    right(null)
                })
    }

    private fun createUser(login: String): Either<Exception, Nothing> {
        val user: UserRepresentation
        try {
            user = keycloakUserRepository.getByLogin(login)
        } catch (e: Exception) {
            return left(e)
        }

        return alfrescoUserRepository.create(user.toAlfrescoUser())
    }

    private fun ensureUserInAdminGroup(login: String): Either<Exception, Nothing> {
        return alfrescoGroupRepository.isAdminGroupMember(login)
            .fold({ e -> left(e) },
                { s ->
                    if (!s) {
                        alfrescoGroupRepository.addAdminGroupMember(login)
                    }
                    right(null)
                })
    }
}
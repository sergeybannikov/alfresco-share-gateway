package com.sergeybannikov.alfrescogateway.repository

import io.vavr.control.Either

interface AlfrescoGroupRepository {
    fun isAdminGroupMember(login: String): Either<Exception, Boolean>
    fun addAdminGroupMember(login: String): Either<Exception, Nothing>
}
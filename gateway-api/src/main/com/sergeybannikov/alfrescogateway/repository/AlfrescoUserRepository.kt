package com.sergeybannikov.alfrescogateway.repository

import com.sergeybannikov.alfrescogateway.model.AlfrescoUserModel
import io.vavr.control.Either

interface AlfrescoUserRepository {
    fun checkExists(login: String): Either<Exception, Boolean>

    fun getPerson(login: String): Either<Exception, AlfrescoUserModel>

    fun create(user: AlfrescoUserModel): Either<Exception, Nothing>

    fun delete(login: String): Either<Exception, Nothing>
}
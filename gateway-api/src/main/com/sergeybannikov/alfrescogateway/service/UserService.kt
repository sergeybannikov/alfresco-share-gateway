package com.sergeybannikov.alfrescogateway.service

import io.vavr.control.Either

interface UserService {
    fun ensureUser(login: String): Either<Exception, Nothing>;
}

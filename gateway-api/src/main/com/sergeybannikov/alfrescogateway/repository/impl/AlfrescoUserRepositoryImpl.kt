package com.sergeybannikov.alfrescogateway.repository.impl

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import com.google.gson.Gson
import com.sergeybannikov.alfrescogateway.config.properties.TargetServiceProperties
import com.sergeybannikov.alfrescogateway.model.AlfrescoUserModel
import com.sergeybannikov.alfrescogateway.repository.AlfrescoUserRepository
import com.sergeybannikov.alfrescogateway.results.AlfrescoUserEntryResult
import io.vavr.control.Either
import io.vavr.control.Either.left
import io.vavr.control.Either.right
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
class AlfrescoUserRepositoryImpl
@Autowired
constructor(
    val serviceProperties: TargetServiceProperties
)
: AlfrescoUserRepository
{
    val uri : String
    val logger = LoggerFactory.getLogger(AlfrescoUserRepositoryImpl::class.java)
    init {
        uri = "${serviceProperties.host}:${serviceProperties.port}/alfresco/api/-default-/public/alfresco/versions/1"
    }

    override fun getPerson(login: String): Either<Exception, AlfrescoUserModel> {
         val (request, response, result) = "$uri/people/$login".httpGet()
            .header("X-Alfresco-Remote-User" to "admin")
            .responseObject<AlfrescoUserEntryResult>()

        if(response.statusCode != 200) {
            logger.error("GetPerson failed: ${response.statusCode}, ${response.responseMessage}")
            return Either.left(java.lang.Exception("GetPerson failed: ${response.statusCode}, ${response.responseMessage}"))
        }

        return result.fold({ Either.right(it.entry)},
            {error -> Either.left(java.lang.Exception(error))})
    }

    override fun delete(login: String): Either<Exception, Nothing> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkExists(login: String): Either<Exception, Boolean> {
        val (request, response, result) = "$uri/people/$login".httpGet()
            .header("X-Alfresco-Remote-User" to "admin")
            .responseObject<AlfrescoUserEntryResult>()

        when(response.statusCode){
            200 -> return right(true)
            404 -> return right(false)
            else -> {
                return left(java.lang.Exception(
                "${response.responseMessage} %nForwarding Headers: ${response.headers.toString()}"))}
        }
    }

    override fun create(user: AlfrescoUserModel): Either<Exception, Nothing> {
        logger.debug("Creating User ${user.toString()}")
        val (request, response, result) = "$uri/people".httpPost()
            .body(Gson().toJson(user))
            .header("X-Alfresco-Remote-User" to "admin")
            .responseObject<AlfrescoUserEntryResult>()

        logger.debug("${result.toString()} :::::: ${response.toString()}")

        when(response.statusCode){
            201 -> return right(null)
            409 -> return left(java.lang.Exception(
                "${response.statusCode}:${response.responseMessage} " +
                        "%nPerson within given id already exists"))
            400, 401, 403, 422 -> return left(java.lang.Exception(
                "${response.statusCode}:${response.responseMessage} " +
                        "%nForwarding Headers: ${response.headers.toString()}"))
            else -> {
                return left(java.lang.Exception(
                    "${response.responseMessage} %nForwarding Headers: ${response.headers.toString()}"))}
        }
    }
}
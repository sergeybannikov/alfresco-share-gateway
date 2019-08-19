package com.sergeybannikov.alfrescogateway.repository.impl

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.jackson.responseObject
import com.google.gson.Gson
import com.sergeybannikov.alfrescogateway.config.properties.TargetServiceProperties
import com.sergeybannikov.alfrescogateway.model.AlfrescoGroupMembershipModel
import com.sergeybannikov.alfrescogateway.repository.AlfrescoGroupRepository
import com.sergeybannikov.alfrescogateway.results.AlfrescoGroupMemberEntryResult
import com.sergeybannikov.alfrescogateway.results.AlfrescoGroupMembersResult
import io.vavr.control.Either
import io.vavr.control.Either.left
import io.vavr.control.Either.right
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class AlfrescoGroupRepositoryImpl
@Autowired
constructor(
    val serviceProperties: TargetServiceProperties
)
: AlfrescoGroupRepository
{
    val uri : String
    val adminGroup = "GROUP_ALFRESCO_ADMINISTRATORS"
    init {
        uri = "${serviceProperties.host}:${serviceProperties.port}/alfresco/api/-default-/public/alfresco/versions/1"
    }
    override fun isAdminGroupMember(login: String): Either<Exception, Boolean> {
        val (request, response, result) = "$uri/people/$login/groups"
            .httpGet(listOf("maxItems" to Int.MAX_VALUE))
            .header("X-Alfresco-Remote-User" to "admin")
            .responseObject<AlfrescoGroupMembersResult>()

        result.fold({success ->
            when (response.statusCode) {
                200 ->
                    return right(result.get().list!!.entries.any { it.entry.id.equals(adminGroup) })
                404 -> return left(
                    java.lang.Exception(
                        "${response.statusCode}:${response.responseMessage} " +
                                "PersonId does not exist"
                    )
                )
                else -> {
                    return left(
                        java.lang.Exception(
                            "${response.responseMessage} Forwarding Headers: ${response.headers.toString()}"
                        )
                    )
                }
            }
        }, {error ->
            return left(java.lang.Exception( result.component2().toString()))
        })

    }

    override fun addAdminGroupMember(login: String): Either<Exception, Nothing> {
        val (request, response, result) = "$uri/groups/$adminGroup/members"
            .httpPost()
            .body(Gson().toJson(AlfrescoGroupMembershipModel(login)))
            .header("X-Alfresco-Remote-User" to "admin")
            .responseObject<AlfrescoGroupMemberEntryResult>()

        result.fold({success ->
            when (response.statusCode) {
                201 ->
                    return right(null)
                404 -> return left(
                    java.lang.Exception(
                        "${response.statusCode}:${response.responseMessage} " +
                                "GroupId or id (of group or person) does not exist"
                    )
                )
                else -> {
                    return left(
                        java.lang.Exception(
                            "${response.responseMessage} Forwarding Headers: ${response.headers.toString()}"
                        )
                    )
                }
            }
        }, {error ->
            return left(java.lang.Exception( result.component2().toString()))
        })
    }

}
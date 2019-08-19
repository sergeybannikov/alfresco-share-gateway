package com.sergeybannikov.alfrescogateway.zuulFilters

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.sergeybannikov.alfrescogateway.config.properties.KeycloakProperties
import com.sergeybannikov.alfrescogateway.config.properties.TargetServiceProperties
import com.sergeybannikov.alfrescogateway.service.UserService
import okhttp3.*
import okhttp3.internal.http.HttpMethod
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import com.sergeybannikov.alfrescogateway.utils.RequestBodyUtil
import javax.servlet.ServletInputStream


@Component
class HttpRoutingFilter
@Autowired
constructor(
    val helper: ProxyRequestHelper,
    val serviceProperties: TargetServiceProperties,
    val keycloakProperties: KeycloakProperties,
    val userService: UserService,
    val okHttpClient: OkHttpClient
) : ZuulFilter() {

    val context get() = RequestContext.getCurrentContext()!!
    val request get() = context.request!!
    val outgoingRequestUri: String get() {
        logger.debug("Incomming reguest: ${request.requestURL.toString()}")
        val params = this.helper!!.buildZuulRequestQueryParams(request).filter { it.key != "access_token" }
        val paramsString = this.helper!!.getQueryString(LinkedMultiValueMap(params))

        val uri = context["routeHost"].toString() + this.helper.buildZuulRequestURI(request) + paramsString
        logger.debug("Outgoing reguest URI: $uri")
        return uri
    }

    var logger = LoggerFactory.getLogger(HttpRoutingFilter::class.java)

    override fun filterType(): String {
        return ROUTE_TYPE
    }

    override fun filterOrder(): Int {
        return SIMPLE_HOST_ROUTING_FILTER_ORDER - 1
    }

    override fun shouldFilter(): Boolean {
        return RequestContext.getCurrentContext().getRouteHost() != null && RequestContext.getCurrentContext().sendZuulResponse()
    }

    override fun run(): Any? {

        val userLogin = getCurrentUserLogin()

        val headers = buildHeaders()

        val requestBuilder = Request.Builder()
            .headers(headers)
            .url(outgoingRequestUri)

        requestBuilder.addHeader(serviceProperties.userHeader, userLogin)

        try {
            val requestBody = getRequestBody(headers, request.inputStream)

            requestBuilder.method(request.method, requestBody)

            logger.debug("Forwarding to: $serviceProperties")

            val requestBuilt = requestBuilder.build()
            logger.debug("Forwarding Headers: ${requestBuilt.headers().toString()}")

            val response = okHttpClient.newCall(requestBuilt).execute()
            val responseHeaders = LinkedMultiValueMap<String, String>()

            for (entry in response.headers().toMultimap()) {
                responseHeaders[entry.key] = entry.value
            }

            this.helper.setResponse(
                response.code(), response.body()!!.byteStream(),
                responseHeaders
            )
            context.routeHost = null // prevent SimpleHostRoutingFilter from running
        } finally {

        }
        return null
    }

    private fun buildHeaders(): Headers{
        val headers = Headers.Builder()
        val headerNames = request.headerNames.toList().filter { !it.equals("authorization", true) }
        headerNames.forEach { name ->
            request.getHeaders(name).toList().forEach {
                headers.add(name, it)
            }
        }
        return headers.build()
    }

    private fun getCurrentUserLogin(): String{
        val userLogin = if (keycloakProperties.enabled) {
            val authentication = SecurityContextHolder.getContext().authentication
            authentication.name
        } else {
            "testuser"
        }

        logger.debug("Ensuring user: $userLogin")
        val userEnsured = userService.ensureUser(userLogin)

        if (userEnsured.isLeft) throw userEnsured.left

        return userLogin
    }

    private fun getRequestBody(headers: Headers, inputStream: ServletInputStream): RequestBody?{
        if (inputStream != null && HttpMethod.permitsRequestBody(request.method)) {
            var mediaType: MediaType? = null
            if (headers.get("Content-Type") != null) {
                mediaType = MediaType.parse(headers.get("Content-Type"))
            }

            if(inputStream.available() == 0)
                return RequestBody.create(mediaType, inputStream.readBytes())

            val requestBody = com.sergeybannikov.alfrescogateway.utils.RequestBodyUtil.create(mediaType, inputStream, request.contentLengthLong)

            return requestBody
        }
        return null
    }

}
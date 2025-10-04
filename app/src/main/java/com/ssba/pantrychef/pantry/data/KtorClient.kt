package com.ssba.pantrychef.pantry.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache

object KtorClient {
    fun ktorClientConfig(
        baseUrl: String,
        tokenName: String,
        tokenValue: String,
        timeOut: Long,
    ): HttpClient {
        val client = HttpClient() {
            install(DefaultRequest) {
                url(baseUrl) {
                    parameters.append(tokenName, tokenValue)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = timeOut
            }
            install(HttpCache)
        }
        return client
    }

}
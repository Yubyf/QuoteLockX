package com.crossbowffs.quotelock.data.modules.openai.geo

import kotlinx.serialization.Serializable

@Serializable
data class OpenAITraceResponse(
    val fl: String,
    val h: String,
    val ip: String,
    val ts: String,
    val visitScheme: String,
    val uag: String,
    val colo: String,
    val sliver: String,
    val http: String,
    val loc: String,
    val tls: String,
    val sni: String,
    val warp: String,
    val gateway: String,
    val rbi: String,
    val kex: String,
)

fun parseTraceResponse(response: String): OpenAITraceResponse {
    val lines = response.split("\n")
    val map = mutableMapOf<String, String>()
    for (line in lines) {
        val parts = line.split("=")
        if (parts.size != 2) continue
        map[parts[0]] = parts[1]
    }
    return OpenAITraceResponse(
        fl = map["fl"] ?: "",
        h = map["h"] ?: "",
        ip = map["ip"] ?: "",
        ts = map["ts"] ?: "",
        visitScheme = map["visit_scheme"] ?: "",
        uag = map["uag"] ?: "",
        colo = map["colo"] ?: "",
        sliver = map["sliver"] ?: "",
        http = map["http"] ?: "",
        loc = map["loc"] ?: "",
        tls = map["tls"] ?: "",
        sni = map["sni"] ?: "",
        warp = map["warp"] ?: "",
        gateway = map["gateway"] ?: "",
        rbi = map["rbi"] ?: "",
        kex = map["kex"] ?: "",
    )
}
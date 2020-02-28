package fr.uvsq.folex

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.FileReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*

const val PROPERTY_FILE = "folex.properties"

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val properties = Properties()
    FileReader(PROPERTY_FILE).use { properties.load(it) }

    val firstQuery = GithubQuery("hal91190", listOf("Coster2", "packer"))
    val jsonParser = Parser.default()
    val jsonQuery = jsonParser.parse(StringBuilder(firstQuery.toString())) as JsonObject

    val httpClient = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder()
        .uri(URI.create(properties.getProperty("github_api_url")))
        .header("Authorization", "bearer ${properties.getProperty("github_token")}")
        .POST(HttpRequest.BodyPublishers.ofString(jsonQuery.toJsonString()))
        .build()
    val response = httpClient.send(request, BodyHandlers.ofString())
    println(response.statusCode())
    println(response.headers())
    println(response.body())
}

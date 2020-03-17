package fr.uvsq.folex

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * La classe <code>GithubGraphqlRequest</code> permet de soumettre une requête GraphQL à github.
 *
 * @author hal
 * @version 2020
 */
class GithubGraphqlRequest(githubApiUrl : String, githubToken : String, query: GithubQuery) {
    private val jsonParser = Parser.default()
    private val httpClient = HttpClient.newBuilder().build()

    val response : HttpResponse<String>
    init {
        val jsonQuery = jsonParser.parse(StringBuilder(query.toString())) as JsonObject

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(githubApiUrl))
            .header("Authorization", "bearer $githubToken")
            .POST(HttpRequest.BodyPublishers.ofString(jsonQuery.toJsonString()))
            .build()
        response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
    }

    fun parseResponse(repositoryNames : List<String>) : Map<String, Int>? {
        if (response.statusCode() != 200) return null
        val jsonResponse = jsonParser.parse(StringBuilder(response.body())) as JsonObject
        val account = jsonResponse.obj("data")?.obj("repositoryOwner") ?: return null
        val repositories = mutableMapOf<String, Int>()
        for (repositoryName in repositoryNames) {
            val fixedRepositoryName = "${repositoryName.replace('.', '_')}repo"
            if (account.obj(fixedRepositoryName) != null) { // le dépôt existe
                val jsonRepository = account.obj(fixedRepositoryName)?.obj("defaultBranchRef")
                val nbCommits = if (jsonRepository != null)
                    jsonRepository.obj("target")?.obj("history")?.int("totalCount")?: 0
                else 0
                repositories[repositoryName] = nbCommits
            }
        }
        return repositories
    }
}

package fr.uvsq.folex.github

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import fr.uvsq.folex.Cfg
import fr.uvsq.folex.Exercise
import fr.uvsq.folex.Student
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
class GithubGraphqlRequest(githubApiUrl: String, githubToken: String, private val student: Student) {
    companion object {
        /**
         * Complète la liste des étudiants avec leurs dépôts github.
         */
        fun queryGithubForStudents(students: List<Student>) {
            for (student in students) {
                if (!student.hasGithubAccount()) continue

                val githubGraphqlRequest = GithubGraphqlRequest(
                    Cfg.githubApiUrl,
                    Cfg.githubToken,
                    student
                )

                if (githubGraphqlRequest.response.statusCode() == 200) {
                    student.repositories = githubGraphqlRequest.parseResponse(Cfg.repositoryNames)
                }
            }
        }
    }

    private val jsonParser = Parser.default()
    private val httpClient = HttpClient.newBuilder().build()

    private val response: HttpResponse<String>

    init {
        val githubQuery = GithubQuery(student.githubLogin, Cfg.repositoryNames)
        val jsonQuery = jsonParser.parse(StringBuilder(githubQuery.toString())) as JsonObject

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(githubApiUrl))
            .header("Authorization", "bearer $githubToken")
            .POST(HttpRequest.BodyPublishers.ofString(jsonQuery.toJsonString()))
            .build()
        response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
    }

    private fun parseResponse(repositoryNames: List<String>): Map<String, Exercise>? {
        if (response.statusCode() != 200) return null
        val jsonResponse = jsonParser.parse(StringBuilder(response.body())) as JsonObject
        val account = jsonResponse.obj("data")?.obj("repositoryOwner") ?: return null
        val repositories = mutableMapOf<String, Exercise>()
        for (repositoryName in repositoryNames) {
            val fixedRepositoryName = "${repositoryName.replace('.', '_')}repo"
            if (account.obj(fixedRepositoryName) != null) { // le dépôt existe
                val jsonRepository = account.obj(fixedRepositoryName)?.obj("defaultBranchRef")
                val nbCommits = if (jsonRepository != null)
                    jsonRepository.obj("target")?.obj("history")?.int("totalCount") ?: 0
                else 0
                repositories[repositoryName] = Exercise(student, repositoryName, nbCommits)
            }
        }
        return repositories
    }
}

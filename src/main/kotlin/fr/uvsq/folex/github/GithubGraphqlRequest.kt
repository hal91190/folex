package fr.uvsq.folex.github

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import fr.uvsq.folex.Cfg
import fr.uvsq.folex.Exercise
import fr.uvsq.folex.Student
import org.slf4j.LoggerFactory
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
        private val logger = LoggerFactory.getLogger(GithubGraphqlRequest::class.java)

        private const val HTTP_STATUS_OK = 200

        private val jsonParser = Parser.default()
        private val httpClient = HttpClient.newBuilder().build()

        /**
         * Complète la liste des étudiants avec leurs dépôts github.
         * La propriété <code>repositories</code> de <code>student</code> sera défini avec la réponse à la requête
         * comme un dictionnaire associant le nom d'un dépôt avec une instance d'<code>Exercice</code>.
         *
         * @param students la liste des étudiants à traiter.
         *
         */
        fun queryGithubForStudents(students: List<Student>) {
            for (student in students) {
                logger.debug("Querying github for student {}", student)
                if (!student.hasGithubAccount()) {
                    logger.debug("Student {} has no github account", student)
                    continue
                }

                val githubGraphqlRequest = GithubGraphqlRequest(
                    Cfg.githubApiUrl,
                    Cfg.githubToken,
                    student
                )

                logger.debug("Sending HTTP request for student {}", student.githubLogin)
                val response = githubGraphqlRequest.sendHttpRequest()

                if (response.statusCode() == HTTP_STATUS_OK) {
                    logger.debug("Parsing HTTP response for student {}", student.githubLogin)
                    student.repositories = githubGraphqlRequest.parseResponse(Cfg.repositoryNames, response)
                } else {
                    logger.error("Unattended HTTP response status {}", response.statusCode())
                }
            }
        }
    }

    private val httpRequest : HttpRequest

    init {
        val githubQuery = GithubQuery(student.githubLogin, Cfg.repositoryNames)
        val jsonQuery = jsonParser.parse(StringBuilder(githubQuery.toString())) as JsonObject

        httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(githubApiUrl))
            .header("Authorization", "bearer $githubToken")
            .POST(HttpRequest.BodyPublishers.ofString(jsonQuery.toJsonString()))
            .build()
    }

    private fun sendHttpRequest() = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

    private fun parseResponse(repositoryNames: List<String>, response: HttpResponse<String>): Map<String, Exercise>? {
        if (response.statusCode() != 200) return null
        val jsonResponse = jsonParser.parse(StringBuilder(response.body())) as JsonObject
        val account = jsonResponse.obj("data")?.obj("repositoryOwner")
        if (account == null) {
            logger.debug("Field repositoryOwner is not present or valid in the JSON response for student {}", student.githubLogin)
            return null
        }
        val repositories = mutableMapOf<String, Exercise>()
        for (repositoryName in repositoryNames) {
            val fixedRepositoryName = "${repositoryName.replace('.', '_')}repo"
            if (account.obj(fixedRepositoryName) != null) { // le dépôt existe
                val jsonRepository = account.obj(fixedRepositoryName)?.obj("defaultBranchRef")
                val nbCommits = if (jsonRepository != null)
                    jsonRepository.obj("target")?.obj("history")?.int("totalCount") ?: 0
                else 0
                logger.trace("Repository {} for student {} has {} commit(s) ", repositoryName, student.githubLogin, nbCommits)
                repositories[repositoryName] = Exercise(student, repositoryName, nbCommits)
            } else {
                logger.trace("Repository {} does not exist for student {}", repositoryName, student.githubLogin)
            }
        }
        return repositories
    }
}

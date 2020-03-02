package fr.uvsq.folex

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.apache.commons.csv.CSVFormat
import java.io.FileReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


const val PROPERTY_FILE = "folex.properties"
const val MINIMUM_NUMBER_OF_COMMITS = 4

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val properties = Properties()
    FileReader(PROPERTY_FILE).use { properties.load(it) }

    val repositories = properties.getProperty("repositories").split(',')

    val jsonParser = Parser.default()
    val httpClient = HttpClient.newBuilder().build()

    val studentFilename = properties.getProperty("student_file")
    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    val outputFile = Files.newBufferedWriter(Paths.get(outputFilename))

    var tableHeader = "No. d'étudiant | Nom | Prénom | Github |"
    var tableSeparator = "-------------- | ----|--------|--------|"
    for (repository in repositories) {
        tableHeader += " $repository |"
        tableSeparator += "----------|"
    }
    outputFile.appendln(tableHeader)
    outputFile.appendln(tableSeparator)

    val studentFile = properties.getProperty("student_file")
    val reader = Files.newBufferedReader(Paths.get(studentFile))
    val students = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
    for (student in students) {
        val githubLogin = student["url"].substring(student["url"].lastIndexOf("/") + 1).trimEnd()
        var adocLine = "${student["no_etudiant"]} | ${student["nom"]} | ${student["prenom"]} | "

        if (!student["url"].startsWith("https://github.com/", ignoreCase = true)) {
            adocLine += ":x: |"
            outputFile.appendln(adocLine)
            continue
        }

        adocLine += ":heavy_check_mark: | "

        val githubQuery = GithubQuery(githubLogin, repositories)

        val jsonQuery = jsonParser.parse(StringBuilder(githubQuery.toString())) as JsonObject

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(properties.getProperty("github_api_url")))
            .header("Authorization", "bearer ${properties.getProperty("github_token")}")
            .POST(HttpRequest.BodyPublishers.ofString(jsonQuery.toJsonString()))
            .build()
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val jsonResponse = jsonParser.parse(StringBuilder(response.body())) as JsonObject
            val account = jsonResponse.obj("data")?.obj("repositoryOwner")
            if (account != null) {
                for (repository in repositories) {
                    val repoName = "${repository.replace('.', '_')}repo"
                    adocLine += if (account.obj(repoName) != null) {
                        val jsonRepository = account.obj(repoName)?.obj("defaultBranchRef")
                        if (jsonRepository != null) {
                            val nbCommits = jsonRepository.obj("target")?.obj("history")?.int("totalCount") ?: 0
                            if (nbCommits > MINIMUM_NUMBER_OF_COMMITS) {
                                ":heavy_check_mark: |"
                            } else { // insufficient number of commits
                                ":warning: |"
                            }
                        } else { // repository does not have commit
                            ":x: |"
                        }
                    } else { // repository does not exist
                        ":x: |"
                    }
                }
            }
        }
        outputFile.appendln(adocLine)
    }
    outputFile.close()
}

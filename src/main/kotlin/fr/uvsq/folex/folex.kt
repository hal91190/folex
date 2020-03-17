package fr.uvsq.folex

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import fr.uvsq.folex.Cfg.githubApiUrl
import fr.uvsq.folex.Cfg.githubToken
import fr.uvsq.folex.Cfg.repositories
import fr.uvsq.folex.Cfg.studentFilename
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Paths

const val MINIMUM_NUMBER_OF_COMMITS = 4

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val jsonParser = Parser.default()
    val httpClient = HttpClient.newBuilder().build()

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

    val studentFileParser = StudentFileParser(studentFilename)

    for (student in studentFileParser.students) {
        var adocLine = "${student.studentNo} | ${student.lastname} | ${student.firstname} | "

        if (student.githubLogin.isEmpty()) {
            adocLine += ":x: |"
            outputFile.appendln(adocLine)
            continue
        }

        val githubQuery = GithubQuery(student.githubLogin, repositories)

        val jsonQuery = jsonParser.parse(StringBuilder(githubQuery.toString())) as JsonObject

        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(githubApiUrl))
            .header("Authorization", "bearer $githubToken")
            .POST(HttpRequest.BodyPublishers.ofString(jsonQuery.toJsonString()))
            .build()
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val jsonResponse = jsonParser.parse(StringBuilder(response.body())) as JsonObject
            val account = jsonResponse.obj("data")?.obj("repositoryOwner")
            if (account != null) {
                adocLine += ":heavy_check_mark: | "
                for (repository in repositories) {
                    val repoName = "${repository.replace('.', '_')}repo"
                    adocLine += if (account.obj(repoName) != null) {
                        val jsonRepository = account.obj(repoName)?.obj("defaultBranchRef")
                        if (jsonRepository != null) {
                            val nbCommits = jsonRepository.obj("target")?.obj("history")?.int("totalCount") ?: 0
                            "${if (nbCommits > MINIMUM_NUMBER_OF_COMMITS) ":heavy_check_mark:" else ":warning:"} ($nbCommits) |"
                        } else { // repository does not have commit
                            ":x: |"
                        }
                    } else { // repository does not exist
                        ":x: |"
                    }
                }
            } else {
                adocLine += ":x: |"
            }
        }
        outputFile.appendln(adocLine)
    }
    outputFile.close()
}

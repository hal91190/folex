package fr.uvsq.folex

import fr.uvsq.folex.Cfg.githubApiUrl
import fr.uvsq.folex.Cfg.githubToken
import fr.uvsq.folex.Cfg.repositoryNames
import fr.uvsq.folex.Cfg.studentFilename
import java.nio.file.Files
import java.nio.file.Paths

const val MINIMUM_NUMBER_OF_COMMITS : Int = 4

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    val outputFile = Files.newBufferedWriter(Paths.get(outputFilename))

    var tableHeader = "No. d'étudiant | Nom | Prénom | Github |"
    var tableSeparator = "-------------- | ----|--------|--------|"
    for (repository in repositoryNames) {
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

        val githubQuery = GithubQuery(student.githubLogin, repositoryNames)

        val githubGraphqlRequest = GithubGraphqlRequest(githubApiUrl, githubToken, githubQuery)

        if (githubGraphqlRequest.response.statusCode() == 200) {
            val repositories = githubGraphqlRequest.parseResponse(repositoryNames)

            if (repositories != null) {
                adocLine += ":heavy_check_mark: | "
                for (repositoryName in repositoryNames) {
                    adocLine += when (val nbCommits = repositories[repositoryName]) {
                        0 -> ":x: |"
                        in 1..MINIMUM_NUMBER_OF_COMMITS -> ":warning: ($nbCommits)|"
                        is Int -> ":heavy_check_mark: ($nbCommits)|"
                        else -> ":x: |"
                    }
                }
            } else { // le compte n'existe pas ou le code de réponse est incorrect
                adocLine += ":x: |"
            }
        }
        outputFile.appendln(adocLine)
    }
    outputFile.close()
}

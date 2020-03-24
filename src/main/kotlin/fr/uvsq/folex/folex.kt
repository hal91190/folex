package fr.uvsq.folex

import fr.uvsq.folex.Cfg.studentFilename

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val studentFileParser = StudentFileParser(studentFilename)

    for (student in studentFileParser.students) {
        if (!student.hasGithubAccount()) continue

        val githubQuery = GithubQuery(student.githubLogin, Cfg.repositoryNames)

        val githubGraphqlRequest = GithubGraphqlRequest(Cfg.githubApiUrl, Cfg.githubToken, githubQuery)

        if (githubGraphqlRequest.response.statusCode() == 200) {
            student.repositories = githubGraphqlRequest.parseResponse(Cfg.repositoryNames)
        }
    }

    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    MarkdownReportGenerator(outputFilename, studentFileParser.students).use { report -> report.generate() }
}

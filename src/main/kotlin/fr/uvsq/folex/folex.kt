package fr.uvsq.folex

import fr.uvsq.folex.Cfg.studentFilename
import fr.uvsq.folex.github.GithubGraphqlRequest

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val studentFileParser = StudentFileParser(studentFilename)
    GithubGraphqlRequest.queryGithubForStudents(studentFileParser.students)

    Exercise.cloneOrPullRepositories(studentFileParser.students)

    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    MarkdownReportGenerator(outputFilename, studentFileParser.students).use { report -> report.generate() }
}

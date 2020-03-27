package fr.uvsq.folex

import fr.uvsq.folex.Cfg.studentFilename
import fr.uvsq.folex.Exercise.Companion.cloneOrPullRepositories
import fr.uvsq.folex.github.GithubGraphqlRequest
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val studentFileParser = StudentFileParser(studentFilename)
    GithubGraphqlRequest.queryGithubForStudents(studentFileParser.students)

    cloneOrPullRepositories(studentFileParser.students)

    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    MarkdownReportGenerator(outputFilename, studentFileParser.students).use { report -> report.generate() }
}

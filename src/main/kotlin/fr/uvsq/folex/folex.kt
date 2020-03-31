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
    val students = StudentFileParser(studentFilename).students

    //GithubGraphqlRequest.queryGithubForStudents(students)

    //Exercise.cloneOrPullRepositories(students)

    //Exercise.buildExercisesWithMaven(students)

    //val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    //MarkdownReportGenerator(outputFilename, students).use { report -> report.generate() }
}

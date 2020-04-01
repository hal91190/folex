package fr.uvsq.folex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import fr.uvsq.folex.Cfg.studentFilename
import fr.uvsq.folex.github.GithubGraphqlRequest
import java.io.IOException
import kotlin.system.exitProcess

class Folex: CliktCommand() {
    val input by option(help = "Nom du fichier CSV contenant la liste des étudiants")
    val output by option(help = "Nom du fichier Markdown de sortie")

    override fun run() {
        val inputFilename = input ?: studentFilename
        val students = StudentFileParser(inputFilename).students
        if (students.isEmpty()) {
            echo("Le fichier CSV $inputFilename est vide ou une erreur d'E/S s'est produite.")
            exitProcess(1)
        }

        GithubGraphqlRequest.queryGithubForStudents(students)

        //Exercise.cloneOrPullRepositories(students)

        //Exercise.buildExercisesWithMaven(students)

        val outputFilename = output ?: inputFilename.replaceAfterLast(".", "md")
        try {
            MarkdownReportGenerator(outputFilename, students).generate()
        } catch (e: IOException) {
            echo("Erreur d'E/S lors de l'ouverture du fichier de sortie $outputFilename.")
            exitProcess(1)
        }
    }
}

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main(args: Array<String>) = Folex().main(args)

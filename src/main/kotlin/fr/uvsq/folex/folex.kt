package fr.uvsq.folex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import fr.uvsq.folex.Cfg.studentFilename
import fr.uvsq.folex.github.GithubGraphqlRequest
import java.io.IOException
import kotlin.system.exitProcess

class Folex: CliktCommand() {
    private val build by option(help = "Construit les projets").flag()
    private val clone by option(help = "Clone ou met à jour les dépôts locaux à partir de github").flag()
    private val input by option("-i", "--input", help = "Nom du fichier CSV contenant la liste des étudiants")
    private val noGithub by option(help = "Ignore l'interrogation de l'API github").flag()
    private val output by option("-o", "--output", help = "Nom du fichier de sortie (sans extension)")
    private val csv by option(help = "Fichier de sortie au format csv").flag()
    private val md by option(help = "Fichier de sortie au format Markdown").flag()

    override fun run() {
        val inputFilename = input ?: studentFilename
        val students = StudentFileParser(inputFilename).students
        if (students.isEmpty()) {
            echo("Le fichier CSV $inputFilename est vide ou une erreur d'E/S s'est produite.")
            exitProcess(1)
        }

        if (!noGithub){
            GithubGraphqlRequest.queryGithubForStudents(students)
        }

        if (clone) {
            Exercise.cloneOrPullRepositories(students)
        }

        if (build) {
            ExerciseBuilder.buildExercisesWithMaven(students)
        }

        if (md) {
            val outputFilename = output?.plus(".md") ?: inputFilename.replaceAfterLast(".", "md")
            try {
                MarkdownReportGenerator(outputFilename, students).generate(build)
            } catch (e: IOException) {
                echo("Erreur d'E/S lors de l'ouverture du fichier de sortie $outputFilename.")
                exitProcess(1)
            }
        }

        if (csv) {
            val outputFilename = output?.plus(".csv") ?: inputFilename.replaceAfterLast(".", "out.csv")
            try {
                CsvReportGenerator(outputFilename, students).generate(build)
            } catch (e: IOException) {
                echo("Erreur d'E/S lors de l'ouverture du fichier de sortie $outputFilename.")
                exitProcess(1)
            }
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

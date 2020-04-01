package fr.uvsq.folex

import fr.uvsq.folex.Cfg.repositoryNames
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * La classe <code>MarkdownReportGenerator</code> permet de générer un rapport au format Markdown.
 *
 * @author hal
 * @version 2020
 */
class MarkdownReportGenerator(private val reportFilename : String, private val students : List<Student>) {
    companion object {
        private val logger = LoggerFactory.getLogger(MarkdownReportGenerator::class.java)

        private const val MINIMUM_NUMBER_OF_COMMITS = 4

        private const val MD_OK = ":heavy_check_mark:"
        private const val MD_KO = ":x:"
        private const val MD_WARNING = ":warning:"
    }

    /**
     * Écrit l'entête du tableau.
     */
    private fun writeHeader(output: Writer) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
        output.appendln("Dernière mise à jour : $now")
        output.appendln()

        var tableHeader = "No. d'étudiant | Nom | Prénom | Github |"
        var tableSeparator = "-------------- | ----|--------|--------|"
        for (repository in repositoryNames) {
            tableHeader += " $repository |"
            tableSeparator += "----------|"
        }
        output.appendln(tableHeader)
        output.appendln(tableSeparator)
    }

    /**
     * Écrit le corps du tableau.
     */
    private fun writeBody(output: Writer) {
        for (student in students) {
            logger.debug("Generating Markdown report line for student {}", student)
            var mdLine = "${student.studentNo} | ${student.lastname} | ${student.firstname} | "

            if (!student.hasGithubAccount()) {
                mdLine += "$MD_KO |"
                output.appendln(mdLine)
                continue
            }

            val repositories = student.repositories
            if (repositories != null) {
                mdLine += "$MD_OK | "
                for (repositoryName in repositoryNames) {
                    mdLine += when (val nbCommits = repositories[repositoryName]?.nbCommits) {
                        0 -> "$MD_KO |"
                        in 1..MINIMUM_NUMBER_OF_COMMITS -> "$MD_WARNING ($nbCommits)|"
                        is Int -> "$MD_OK ($nbCommits)|"
                        else -> "$MD_KO |"
                    }
                }
            } else { // le compte n'existe pas ou le code de réponse est incorrect
                mdLine += "$MD_KO |"
            }
            output.appendln(mdLine)
        }
    }

    /**
     * Génère le rapport.
     */
    fun generate() {
        try {
            Files.newBufferedWriter(Paths.get(reportFilename)).use {
                logger.info("Generating Markdown report in {}", reportFilename)
                writeHeader(it)
                writeBody(it)
            }
        } catch (e: IOException) {
            logger.error("I/O error while opening output file")
            throw e
        }
    }
}

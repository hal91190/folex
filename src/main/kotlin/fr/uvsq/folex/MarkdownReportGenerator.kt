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
    private fun writeBody(output: Writer, withBuildInfos: Boolean) {
        for (student in students) {
            logger.debug("Generating Markdown report line for student {}", student)
            var mdLine = "${student.studentNo} | ${student.lastname} | ${student.firstname} | "

            if (!student.hasGithubAccount() || !student.hasRepositories()) {
                // L'URL du compte github est incorrect
                // Le compte n'existe pas sur github
                // Le code de réponse de la requête HTTP est incorrect
                mdLine += "$MD_KO |"
                output.appendln(mdLine)
                continue
            }

            mdLine += "$MD_OK | " // L'étudiant dispose d'un compte github accessible

            val repositories = student.repositories
            if (repositories != null) {
                for (repositoryName in repositoryNames) {
                    val repository = repositories[repositoryName]
                    mdLine += "["
                    if (repository == null) { //TODO dans quels cas ?
                        mdLine += MD_KO
                    } else {
                        mdLine += when (val nbCommits = repository.nbCommits) {
                            0 -> MD_KO
                            in 1..MINIMUM_NUMBER_OF_COMMITS -> "$MD_WARNING ($nbCommits)"
                            is Int -> "$MD_OK ($nbCommits)"
                            else -> MD_KO
                        } // Statut git
                        if (withBuildInfos) {
                            mdLine += ", "
                            if (!repository.isMavenProject || !repository.hasBuilt) {
                                // Ce n'est pas un projet Maven
                                // Le build a échoué
                                mdLine += MD_KO
                            } else {
                                mdLine += MD_OK
                            } // Statut Maven
                            mdLine += ", "
                            val result = repository.aggregateJUnitResults(repositoryName, student)
                            mdLine += result
                            mdLine += ", "
                            //TODO intégrer checkstyle
                        }
                    }
                    mdLine += "] |"
                }
            } //TODO else ?

            output.appendln(mdLine)
        }
    }

    /**
     * Génère le rapport.
     */
    fun generate(withBuildInfos : Boolean = false) {
        try {
            Files.newBufferedWriter(Paths.get(reportFilename)).use {
                logger.info("Generating Markdown report in {}", reportFilename)
                writeHeader(it)
                writeBody(it, withBuildInfos)
            }
        } catch (e: IOException) {
            logger.error("I/O error while opening output file")
            throw e
        }
    }
}

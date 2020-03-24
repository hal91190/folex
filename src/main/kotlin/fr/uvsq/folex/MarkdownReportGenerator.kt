package fr.uvsq.folex

import fr.uvsq.folex.Cfg.githubApiUrl
import fr.uvsq.folex.Cfg.githubToken
import fr.uvsq.folex.Cfg.repositoryNames
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

const val MINIMUM_NUMBER_OF_COMMITS = 4

const val MD_OK = ":heavy_check_mark:"
const val MD_KO = ":x:"
const val MD_WARNING = ":warning:"

/**
 * La classe <code>MarkdownReportGenerator</code> permet de générer un rapport au format Markdown.
 *
 * @author hal
 * @version 2020
 */
class MarkdownReportGenerator(reportFilename : String, private val students : List<Student>) : Closeable {
    /**
     * Fichier de sortie.
     */
    private val outputFile = Files.newBufferedWriter(Paths.get(reportFilename))

    /**
     * Écrit l'entête du tableau.
     */
    private fun writeHeader() {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
        outputFile.appendln("Dernière mise à jour : $now")
        outputFile.appendln()

        var tableHeader = "No. d'étudiant | Nom | Prénom | Github |"
        var tableSeparator = "-------------- | ----|--------|--------|"
        for (repository in repositoryNames) {
            tableHeader += " $repository |"
            tableSeparator += "----------|"
        }
        outputFile.appendln(tableHeader)
        outputFile.appendln(tableSeparator)
    }

    /**
     * Écrit le corps du tableau.
     */
    private fun writeBody() {
        for (student in students) {
            var mdLine = "${student.studentNo} | ${student.lastname} | ${student.firstname} | "

            if (student.githubLogin.isEmpty()) {
                mdLine += "$MD_KO |"
                outputFile.appendln(mdLine)
                continue
            }

            val githubQuery = GithubQuery(student.githubLogin, repositoryNames)

            val githubGraphqlRequest = GithubGraphqlRequest(githubApiUrl, githubToken, githubQuery)

            if (githubGraphqlRequest.response.statusCode() == 200) {
                val repositories = githubGraphqlRequest.parseResponse(repositoryNames)

                if (repositories != null) {
                    mdLine += "$MD_OK | "
                    for (repositoryName in repositoryNames) {
                        mdLine += when (val nbCommits = repositories[repositoryName]) {
                            0 -> "$MD_KO |"
                            in 1..MINIMUM_NUMBER_OF_COMMITS -> "$MD_WARNING ($nbCommits)|"
                            is Int -> "$MD_OK ($nbCommits)|"
                            else -> "$MD_KO |"
                        }
                    }
                } else { // le compte n'existe pas ou le code de réponse est incorrect
                    mdLine += "$MD_KO |"
                }
            }
            outputFile.appendln(mdLine)
        }
    }

    /**
     * Génère le rapport.
     */
    fun generate() {
        writeHeader()
        writeBody()

    }

    override fun close() {
        outputFile.close()
    }
}

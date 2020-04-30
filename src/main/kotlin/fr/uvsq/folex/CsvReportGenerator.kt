package fr.uvsq.folex

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDate
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


/**
 * La classe <code>CsvReportGenerator</code> permet de générer un rapport au format CSV.
 *
 * @author hal
 * @version 2020
 */
class CsvReportGenerator(private val reportFilename : String, private val students : List<Student>) {
    companion object {
        private val logger = LoggerFactory.getLogger(CsvReportGenerator::class.java)
    }

    fun generate() {
        try {
            CSVPrinter(FileWriter(reportFilename), CSVFormat.EXCEL).use {
                logger.info("Generating CSV report in {}", reportFilename)
                writeHeader(it)
                writeBody(it)
            }
        } catch (e: IOException) {
            logger.error("I/O error while opening output file")
            throw e
        }
    }

    private fun writeHeader(output: CSVPrinter) {
        val header = mutableListOf("no_etud", "nom", "prenom", "github")
        for (repository in Cfg.repositoryNames) {
            header.add("${repository}_nb_commits")
            header.add("${repository}_maven")
            header.add("${repository}_junit_tests")
            header.add("${repository}_junit_skipped")
            header.add("${repository}_junit_failure")
            header.add("${repository}_junit_errors")
            header.add("${repository}_junit_time")
        }
        output.printRecord(header)
    }

    private fun writeBody(output: CSVPrinter) {
        for (student in students) {
            logger.debug("Generating CSV report line for student {}", student)
            val line = mutableListOf<Any>(student.studentNo, student.lastname, student.firstname)

            if (!student.hasGithubAccount() || !student.hasRepositories()) {
                // L'URL du compte github est incorrect
                // Le compte n'existe pas sur github
                // Le code de réponse de la requête HTTP est incorrect
                line.add("non")
                output.printRecord(line)
                continue
            }

            line.add("oui") // L'étudiant dispose d'un compte github accessible

            val repositories = student.repositories
            if (repositories != null) {
                for (repositoryName in Cfg.repositoryNames) {
                    val repository = repositories[repositoryName]
                    if (repository == null) { //TODO dans quels cas ?
                        line.add(-1)
                    } else {
                        line.add(repository.nbCommits ?: -1)
                        if (!repository.isMavenProject || !repository.hasBuilt) {
                            // Ce n'est pas un projet Maven
                            // Le build a échoué
                            line.add("non")
                        } else {
                            line.add("oui")
                        } // Statut Maven
                        val result = repository.aggregateJUnitResults(repositoryName, student)
                        line.add(result.nbTests)
                        line.add(result.nbSkipped)
                        line.add(result.nbFailures)
                        line.add(result.nbErrors)
                        line.add(result.executionTime)
                    }
                }
            }

            output.printRecord(line)
        }
    }
}

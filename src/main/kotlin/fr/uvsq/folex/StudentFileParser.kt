package fr.uvsq.folex

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Collections.unmodifiableList

/**
 * La classe <code>StudentFileParser</code> crée la liste des étudiants à partir d'un fichier de données CSV.
 *
 * @author hal
 * @version 2020
 */
class StudentFileParser(studentFilename: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(StudentFileParser::class.java)

        private const val GITHUB_URL_PREFIX = "https://github.com/"
        private const val CSV_URL = "url"
        private const val CSV_STUDENT_NUMBER = "no_etudiant"
        private const val CSV_STUDENT_LASTNAME = "nom"
        private const val CSV_STUDENT_FIRSTNAME = "prenom"

        private fun fixStudentGithubLogin(csvGithubLogin : String) : String {
            val fixedStudentGithubLogin = csvGithubLogin.trim().removeSuffix("/")
            return if (fixedStudentGithubLogin.startsWith(GITHUB_URL_PREFIX, ignoreCase = true))
                fixedStudentGithubLogin.substringAfterLast("/")
            else ""
        }
    }

    /**
     * La liste des étudiants issue de l'analyse du fichier CSV.
     */
    val students : List<Student>
        get() = unmodifiableList(field)

    init {
        val mutableStudents = mutableListOf<Student>()
        try {
            Files.newBufferedReader(Paths.get(studentFilename)).use {
                logger.info("Reading CSV file {}", studentFilename)
                val csvStudents: CSVParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(it)
                for (student in csvStudents) {
                    val githubLogin = fixStudentGithubLogin(student[CSV_URL])
                    logger.debug(
                        "Loading student ({}, {}, {}, {})",
                        student[CSV_STUDENT_NUMBER],
                        student[CSV_STUDENT_LASTNAME],
                        student[CSV_STUDENT_FIRSTNAME],
                        githubLogin
                    )
                    mutableStudents.add(
                        Student(
                            student[CSV_STUDENT_NUMBER],
                            student[CSV_STUDENT_LASTNAME],
                            student[CSV_STUDENT_FIRSTNAME],
                            githubLogin
                        )
                    )
                }
            }
        } catch (e : IOException) {
            logger.error("I/O error reading CSV file {}", studentFilename)
        }
        students = mutableStudents
        logger.info("{} student(s) loaded", students.size)
    }
}

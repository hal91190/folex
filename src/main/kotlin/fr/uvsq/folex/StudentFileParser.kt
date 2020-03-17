package fr.uvsq.folex

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Collections.unmodifiableList

const val GITHUB_URL_PREFIX = "https://github.com/"
const val CSV_URL = "url"
const val CSV_STUDENT_NUMBER = "no_etudiant"
const val CSV_STUDENT_LASTNAME = "nom"
const val CSV_STUDENT_FIRSTNAME = "prenom"

/**
 * La classe <code>StudentFileParser</code> crée la liste des étudiants à partir d'un fichier de données CSV.
 *
 * @author hal
 * @version 2020
 */
class StudentFileParser(studentFilename: String) {
    /**
     * Les enregistrements du CSV.
     */
    private val csvStudents : CSVParser

    init {
        val reader = Files.newBufferedReader(Paths.get(studentFilename))
        csvStudents = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)
    }

    /**
     * La liste des étudiants issue l'analyse du fichier CSV.
     */
    val students : List<Student>
        get() = unmodifiableList(field)

    init {
        val mutableStudents = mutableListOf<Student>()
        for (student in csvStudents) {
            val githubLogin = if (student[CSV_URL].startsWith(GITHUB_URL_PREFIX, ignoreCase = true))
                student[CSV_URL].substring(student[CSV_URL].lastIndexOf("/") + 1).trimEnd()
            else
                ""
            mutableStudents.add(Student(student[CSV_STUDENT_NUMBER], student[CSV_STUDENT_LASTNAME], student[CSV_STUDENT_FIRSTNAME], githubLogin))
        }
        students = mutableStudents
    }
}

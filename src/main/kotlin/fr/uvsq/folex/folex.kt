package fr.uvsq.folex

import fr.uvsq.folex.Cfg.studentFilename

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val studentFileParser = StudentFileParser(studentFilename)
    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    MarkdownReportGenerator(outputFilename, studentFileParser.students).use { report -> report.generate() }
}

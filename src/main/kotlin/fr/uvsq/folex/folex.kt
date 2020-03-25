package fr.uvsq.folex

import fr.uvsq.folex.Cfg.studentFilename
import fr.uvsq.folex.github.GithubGraphqlRequest
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

const val PROJECT_DIRECTORY_NAME = "projects"

/**
 * La classe <code>FolexKt</code> est la classe principale de l'application.
 *
 * @author hal
 * @version 2020
 */
fun main() {
    val studentFileParser = StudentFileParser(studentFilename)
    GithubGraphqlRequest.queryGithubForStudents(studentFileParser.students)

    cloneOrPullRepositories(studentFileParser.students)

    val outputFilename = studentFilename.substring(0, studentFilename.lastIndexOf(".")) + ".md"
    MarkdownReportGenerator(outputFilename, studentFileParser.students).use { report -> report.generate() }
}

fun cloneOrPullRepositories(students : List<Student>) {
    val projectPath = createProjectDirectory()

    for (student in students) {
        if (!student.hasGithubAccount()) continue
        val studentPath = createStudentPath(projectPath, student)
        val repositoryNames = student.repositories?.keys ?: continue
        for (repositoryName in repositoryNames) {
            val exercise = Exercise(student.githubLogin, repositoryName, studentPath)
            if (exercise.exists) {
                exercise.pullRepository()
            } else {
                exercise.cloneRepository()
            }
        }
    }
}

fun createStudentPath(projectPath: Path, student: Student): Path {
    val studentPath = projectPath.resolve(student.githubLogin)
    if (!Files.exists(studentPath)) {
        Files.createDirectory(studentPath)
    }
    return studentPath
}

fun createProjectDirectory() : Path {
    val fs = FileSystems.getDefault()
    val projectPath = fs.getPath(PROJECT_DIRECTORY_NAME)
    if (!Files.exists(projectPath)) {
        Files.createDirectory(projectPath)
    }
    return projectPath
}

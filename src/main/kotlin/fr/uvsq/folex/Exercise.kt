package fr.uvsq.folex

import org.eclipse.jgit.api.Git
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

/**
 * La classe <code>Exercise</code> représente un exercice dans un repository github.
 *
 * @author hal
 * @version 2020
 */
class Exercise(githubLogin : String, repository : String, studentPath : Path) {
    companion object {
        private val logger = LoggerFactory.getLogger(Exercise::class.java)

        /**
         * Nom du répertoire qui recevra les projets.
         */
        private const val PROJECT_DIRECTORY_NAME = "projects"

        /**
         * Clone ou met à jour les dépôts des étudiants.
         */
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
                        logger.info("Pulling repository {} for github account {}", repositoryName, student.githubLogin)
                    } else {
                        exercise.cloneRepository()
                        logger.info("Cloning repository {} for github account {}", repositoryName, student.githubLogin)
                    }
                }
            }
        }

        private fun createProjectDirectory() : Path {
            val fs = FileSystems.getDefault()
            val projectPath = fs.getPath(PROJECT_DIRECTORY_NAME)
            if (!Files.exists(projectPath)) {
                Files.createDirectory(projectPath)
                logger.info("Creating directory {}", projectPath)
            }
            return projectPath
        }

        private fun createStudentPath(projectPath: Path, student: Student): Path {
            val studentPath = projectPath.resolve(student.githubLogin)
            if (!Files.exists(studentPath)) {
                Files.createDirectory(studentPath)
                logger.info("Creating student directory {}", projectPath)
            }
            return studentPath
        }
    }

    private val gitDirectory = ".git"

    private val repositoryUrl = "$GITHUB_URL_PREFIX/$githubLogin/$repository"
    private val localPath = studentPath.resolve(repository)

    val exists = Files.exists(localPath.resolve(gitDirectory))

    fun cloneRepository() {
        Git.cloneRepository()
            .setURI(repositoryUrl)
            .setDirectory(localPath.toFile())
            .call()
    }

    fun pullRepository() {
        val localRepository = Git.open(localPath.toFile())
        localRepository.pull()
            .call()
    }
}

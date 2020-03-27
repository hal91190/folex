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
class Exercise(student : Student, repository : String, val nbCommits : Int) {
    companion object {
        private val logger = LoggerFactory.getLogger(Exercise::class.java)

        /**
         * Nom du répertoire qui recevra les projets.
         */
        private const val PROJECT_DIRECTORY_NAME = "projects"
        private val projectPath = FileSystems.getDefault().getPath(PROJECT_DIRECTORY_NAME)

        /**
         * Clone ou met à jour les dépôts des étudiants.
         */
        fun cloneOrPullRepositories(students : List<Student>) {
            createProjectDirectory()
            for (student in students) {
                if (!student.hasGithubAccount()) continue
                val exercises = student.repositories ?: continue
                for (repository in exercises) {
                    val exercise = repository.value
                    if (exercise.exists) {
                        exercise.pullRepository()
                        logger.info("Pulling repository {} for github account {}", repository.key, student.githubLogin)
                    } else {
                        exercise.cloneRepository()
                        logger.info("Cloning repository {} for github account {}", repository.key, student.githubLogin)
                    }
                }
            }
        }

        private fun createProjectDirectory() : Path {
            if (!Files.exists(projectPath)) {
                Files.createDirectory(projectPath)
                logger.info("Creating directory {}", projectPath)
            }
            return projectPath
        }
    }

    private val gitDirectory = ".git"

    private val repositoryUrl = "$GITHUB_URL_PREFIX/${student.githubLogin}/$repository"
    private val localPath = student.createLocalDirectory(projectPath).resolve(repository)

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

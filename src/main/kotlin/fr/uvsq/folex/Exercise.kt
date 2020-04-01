package fr.uvsq.folex

import org.apache.maven.shared.invoker.*
import org.eclipse.jgit.api.Git
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.StreamSupport


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

        private const val GITHUB_URL_PREFIX = "https://github.com/"
        private const val GIT_DIRECTORY = ".git"

        private const val MAVEN_POM_FILE = "pom.xml"
        private val MAVEN_GOALS = listOf("clean", "package")

        /**
         * Clone ou met à jour les dépôts des étudiants.
         */
        fun cloneOrPullRepositories(students : List<Student>) {
            createProjectDirectory()
            for (student in students) {
                logger.debug("Cloning or updating repositories for student {}", student)
                if (!student.hasGithubAccount())  {
                    logger.debug("Student {} has no github account", student)
                    continue
                }

                if (student.repositories == null) {
                    logger.debug("Student {} has no exercise, attempting to load them locally", student.githubLogin)
                    loadLocalExercises(student)
                }
                val exercises = student.repositories
                if (exercises == null) {
                    logger.debug("Student {} has definitely no exercise", student.githubLogin)
                    continue
                }

                for (repository in exercises) {
                    val exercise = repository.value
                    if (exercise.exists) {
                        exercise.pullRepository()
                        logger.trace("Pulling repository {} for github account {}", repository.key, student.githubLogin)
                    } else {
                        exercise.cloneRepository()
                        logger.trace("Cloning repository {} for github account {}", repository.key, student.githubLogin)
                    }
                }
            }
        }

        fun buildExercisesWithMaven(students: List<Student>) {
            createProjectDirectory()
            for (student in students) {
                logger.debug("Building projects for student {}", student)
                if (!student.hasGithubAccount())  {
                    logger.debug("Student {} has no github account", student)
                    continue
                }

                if (student.repositories == null) {
                    logger.debug("Student {} has no exercise, attempting to load them locally", student.githubLogin)
                    loadLocalExercises(student)
                }
                val exercises = student.repositories
                if (exercises == null) {
                    logger.debug("Student {} has definitely no exercise", student.githubLogin)
                    continue
                }

                for (repository in exercises) {
                    val exercise = repository.value
                    if (exercise.exists && exercise.isMavenProject) {
                        logger.trace("Build exercise {} with maven for student {}", repository.key, student.githubLogin)
                        val request: InvocationRequest = DefaultInvocationRequest()
                            .setBatchMode(true)
                        //TODO Des saisies dans les tests unitaires peuvent bloquer le processus
                        request.pomFile = exercise.localPath.resolve(MAVEN_POM_FILE).toFile()
                        request.goals = MAVEN_GOALS

                        //TODO La variable d'environnement M2_HOME doit pointer sur le répertoire d'installation de maven
                        // export M2_HOME=/usr/share/maven/
                        val invoker: Invoker = DefaultInvoker()
                        try {
                            val result : InvocationResult = invoker.execute(request)
                            logger.trace("Build exercise {} with maven for student {} ({})", repository.key, student.githubLogin, if (result.exitCode == 0) "OK" else "FAILED")
                        } catch (e: MavenInvocationException) {
                            logger.trace("Maven error building exercise {} for student {}", repository.key, student.githubLogin)
                        }
                    } else {
                        logger.trace("Exercise {} for student {} does not exist or is not a maven project", repository.key, student.githubLogin)
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

        private fun loadLocalExercises(student: Student) {
            val repositories = mutableMapOf<String, Exercise>()
            for (repositoryName in Cfg.repositoryNames) {
                val localPath = student.getOrCreateLocalDirectory(projectPath).resolve(repositoryName)
                if (!Files.exists(localPath)) {
                    logger.trace("Directory {} does not exist", localPath)
                    continue
                }
                if (!Files.exists(localPath.resolve(GIT_DIRECTORY))) {
                    logger.trace("Directory {} exists but is not a git repository", localPath)
                    continue
                }
                val repository = Git.open(localPath.toFile())
                val log = repository.log().call()
                val nbCommits = StreamSupport.stream(log.spliterator(), false).count()
                logger.trace("Adding exercise {} with {} commits for student {}", repositoryName, nbCommits, student.githubLogin)
                repositories[repositoryName] = Exercise(student, repositoryName, nbCommits.toInt())
            }
            student.repositories = if (repositories.isEmpty()) null else repositories
        }
    }

    private val repositoryUrl = "$GITHUB_URL_PREFIX/${student.githubLogin}/$repository"
    private val localPath = student.getOrCreateLocalDirectory(projectPath).resolve(repository)

    val exists = Files.exists(localPath.resolve(GIT_DIRECTORY))
    val isMavenProject = Files.exists(localPath.resolve(MAVEN_POM_FILE))

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

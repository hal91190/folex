package fr.uvsq.folex

import org.apache.maven.shared.invoker.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.StreamSupport

/**
 * La classe <code>ExerciseBuilder</code> permet de construire un projet avec Maven.
 *
 * @author hal
 * @version 2020
 */
class ExerciseBuilder {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseBuilder::class.java)

        private const val MAVEN_POM_FILE = "pom.xml"
        private val MAVEN_GOALS = listOf("clean", "package")
        private val MAVEN_PROPS = Properties()
        init {
            MAVEN_PROPS.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn")
        }

        private fun loadLocalExercises(student: Student) {
            val repositories = mutableMapOf<String, Exercise>()
            for (repositoryName in Cfg.repositoryNames) {
                val localPath = student.getOrCreateLocalDirectory(Exercise.projectPath).resolve(repositoryName)
                if (!Files.exists(localPath)) {
                    logger.trace("Directory {} does not exist", localPath)
                    continue
                }
                if (!Files.exists(localPath.resolve(Exercise.GIT_DIRECTORY))) {
                    logger.trace("Directory {} exists but is not a git repository", localPath)
                    continue
                }
                try {
                    val repository = Git.open(localPath.toFile())
                    val log = repository.log().call()
                    val nbCommits = StreamSupport.stream(log.spliterator(), false).count()
                    logger.trace(
                        "Adding exercise {} with {} commits for student {}",
                        repositoryName,
                        nbCommits,
                        student.githubLogin
                    )
                    repositories[repositoryName] = Exercise(student, repositoryName, nbCommits.toInt())
                } catch (e : GitAPIException) {
                    logger.error("Git exception for exercise {} of student {}: {}", repositoryName, student.githubLogin, e)
                }
            }
            student.repositories = if (repositories.isEmpty()) null else repositories
        }

        fun buildExercisesWithMaven(students: List<Student>) {
            Exercise.createProjectDirectory()
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
                    val localPath : Path = student.getOrCreateLocalDirectory(Exercise.projectPath).resolve(repository.key)
                    val exercise = repository.value
                    if (exercise.isGitRepository && exercise.isMavenProject) {
                        logger.trace("Build exercise {} with maven for student {}", repository.key, student.githubLogin)
                        val request: InvocationRequest = DefaultInvocationRequest()
                            .setBatchMode(true)
                        //TODO Des saisies dans les tests unitaires peuvent bloquer le processus
                        request.properties = MAVEN_PROPS
                        request.pomFile = exercise.localPath.resolve(MAVEN_POM_FILE).toFile()
                        request.goals = MAVEN_GOALS

                        //TODO La variable d'environnement M2_HOME doit pointer sur le répertoire d'installation de maven
                        // export M2_HOME=/usr/share/maven/
                        val invoker: Invoker = DefaultInvoker()
                        try {
                            val result : InvocationResult = invoker.execute(request)
                            logger.trace("Exercise {} for student {} built ({})", repository.key, student.githubLogin, if (result.exitCode == 0) "OK" else "FAILED")
                            if (result.exitCode == 0) {
                                exercise.hasBuilt = true
                                val testCollector = JUnitTestCollector(localPath.toFile())
                                testCollector.collect()
                                if (testCollector.jUnitResults.isEmpty()) {
                                    logger.trace("No JUnit results for exercise {} for student {}", repository.key, student.githubLogin)
                                } else {
                                    logger.trace("JUnit results for exercise {} for student {}", repository.key, student.githubLogin)
                                    testCollector.jUnitResults.forEach { r -> println(r) } //TODO ajouter dans l'exercice
                                    exercise.jUnitResult = testCollector.jUnitResults
                                }
                            }
                        } catch (e: MavenInvocationException) {
                            logger.trace("Maven error building exercise {} for student {}", repository.key, student.githubLogin)
                        }
                    } else {
                        logger.trace("Exercise {} for student {} does not exist or is not a maven project", repository.key, student.githubLogin)
                    }
                }
            }
        }
    }
}
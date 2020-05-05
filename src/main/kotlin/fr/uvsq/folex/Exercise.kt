package fr.uvsq.folex

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.StreamSupport

//TODO faire de Exercise une data class ?

/**
 * La classe <code>Exercise</code> représente un exercice dans un repository github.
 *
 * @author hal
 * @version 2020
 */
class Exercise(student : Student, repository : String, val nbCommits : Int? = null) {
    companion object {
        private val logger = LoggerFactory.getLogger(Exercise::class.java)

        private const val GITHUB_URL_PREFIX = "https://github.com/"
        const val GIT_DIRECTORY = ".git"

        const val MAVEN_POM = "pom.xml"

        /**
         * Clone ou met à jour les dépôts des étudiants.
         */
        fun cloneOrPullRepositories(students : List<Student>) {
            for (student in students) {
                logger.debug("Cloning or updating repositories for student {}", student)
                if (!student.hasGithubAccount())  {
                    logger.debug("Student {} has no github account", student)
                    continue
                }

                val noGithub = student.repositories == null
                val exercises = student.repositories?.toMutableMap() ?: mutableMapOf()

                for (repositoryName in Cfg.repositoryNames) {
                    if (noGithub) {
                        val exercise = createExerciseFromLocalDirectory(student, repositoryName)
                        if (exercise != null) exercises[repositoryName] = exercise
                    } else {
                        val exercise = exercises[repositoryName]
                        if (exercise == null) {
                            logger.trace("Exercise {} does not exist on github for student {}", repositoryName, student.githubLogin)
                            continue
                        }
                        try {
                            if (exercise.isGitRepository) {
                                exercise.pullRepository()
                                logger.trace(
                                    "Pulling repository {} for github account {}",
                                    repositoryName,
                                    student.githubLogin
                                )
                            } else {
                                exercise.cloneRepository()
                                logger.trace(
                                    "Cloning repository {} for github account {}",
                                    repositoryName,
                                    student.githubLogin
                                )
                            }
                        } catch (e : GitAPIException) {
                            logger.error("Git error accessing repository {}", repositoryName)
                            //TODO signaler l'exception
                        }
                    }
                }
            }
        }

        private fun createExerciseFromLocalDirectory(student: Student, repositoryName: String): Exercise? {
            logger.trace("Loading exercise {} from local directory for student {}", repositoryName, student.githubLogin)
            val localPath = student.getOrCreateLocalDirectory(Cfg.projectPath).resolve(repositoryName)
            if (!Files.exists(localPath)) {
                logger.trace("Directory {} does not exist", localPath)
                return null
            }
            if (!Files.exists(localPath.resolve(GIT_DIRECTORY))) {
                logger.trace("Directory {} exists but is not a git repository", localPath)
                return null
            }
            //TODO gérer les erreurs liées à git
            try {
                val repository = Git.open(localPath.toFile())
                repository.pull().call()
                val log = repository.log().call()
                val nbCommits = StreamSupport.stream(log.spliterator(), false).count()
                logger.trace(
                    "Creating exercise {} with {} commits for student {}",
                    repositoryName,
                    nbCommits,
                    student.githubLogin
                )
                return Exercise(student, repositoryName, nbCommits.toInt())
            } catch (e : GitAPIException) {
                logger.error("Git error accessing repository {}", repositoryName)
                //TODO signaler l'exception
            }
            return null
        }
    }

    private val repositoryUrl = "$GITHUB_URL_PREFIX/${student.githubLogin}/$repository"
    val localPath : Path = student.getOrCreateLocalDirectory(Cfg.projectPath).resolve(repository)

    val isGitRepository = Files.exists(localPath.resolve(GIT_DIRECTORY))

    val isMavenProject = Files.exists(localPath.resolve(MAVEN_POM))

    var hasBuilt = false

    var jUnitResults = listOf<JUnitResult>()

    /**
     * Fait la somme de chacune des stats JUnit.
     *
     * @return une instance de JUnitResult contenant les sommes.
     */
    fun aggregateJUnitResults(repositoryName: String, student: Student) : JUnitResult {
        //TODO à réécrire en style fonctionnel
        val name = "$repositoryName@${student.githubLogin}"
        var nbTests = 0
        var nbSkipped = 0
        var nbFailures = 0
        var nbErrors = 0
        var executionTime = 0.0

        for (result in jUnitResults) {
            nbTests += result.nbTests
            nbSkipped += result.nbSkipped
            nbFailures += result.nbFailures
            nbErrors += result.nbErrors
            executionTime += result.executionTime
        }

        return JUnitResult(name, nbTests, nbSkipped, nbFailures, nbErrors, executionTime)
    }

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

package fr.uvsq.folex

import org.eclipse.jgit.api.Git
import java.nio.file.Files
import java.nio.file.Path

/**
 * La classe <code>Exercise</code> représente un exercice dans un repository github.
 *
 * @author hal
 * @version 2020
 */
class Exercise(githubLogin : String, repository : String, studentPath : Path) {
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

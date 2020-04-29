package fr.uvsq.folex

import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * La classe <code>Student</code> représente un étudiant.
 *
 * @author hal
 * @version 2020
 */
data class Student(val studentNo : String, val lastname : String, val firstname : String, val githubLogin : String) {
    companion object {
        private val logger = LoggerFactory.getLogger(Student::class.java)
    }

    /**
     * Dictionnaire contenant le nombre de commits pour chaque dépôt valide de l'étudiant.
     * Cette attribut est null si le compte gituhb n'existe pas ou si la requête HTTP a retourné un code différent de 200.
     * Le dictionnaire est vide si aucun dépôt recherché n'existe sur ce compte.
     */
    var repositories : Map<String, Exercise>? = null

    fun hasRepositories() = repositories != null

    /**
     * Retourne true si l'étudiant a déclaré un compte github valide.
     */
    fun hasGithubAccount() = githubLogin.isNotEmpty()

    /**
     * Retourne ou crée (s'il n'existe pas déjà) le réperoire local de l'étudiant.
     */
    fun getOrCreateLocalDirectory(projectPath: Path): Path {
        val studentPath = projectPath.resolve(githubLogin)
        if (!Files.exists(studentPath)) {
            Files.createDirectory(studentPath)
            logger.info("Creating student directory {}", projectPath)
        }
        return studentPath
    }
}

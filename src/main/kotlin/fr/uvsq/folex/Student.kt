package fr.uvsq.folex

/**
 * La classe <code>Student</code> représente un étudiant.
 *
 * @author hal
 * @version 2020
 */
data class Student(val studentNo : String, val lastname : String, val firstname : String, val githubLogin : String) {
    /**
     * Dictionnaire contenant le nombre de commits pour chaque dépôt valide de l'étudiant.
     * Cette attribut est null si le compte gituhb n'existe pas ou si la requête HTTP a retourné un code différent de 200.
     * Le dictionnaire est vide si aucun dépôt recherché n'existe sur ce compte.
     */
    var repositories : Map<String, Int>? = null

    /**
     * Retourne true si l'étudiant a déclaré un compte github valide.
     */
    fun hasGithubAccount() = !githubLogin.isEmpty()
}

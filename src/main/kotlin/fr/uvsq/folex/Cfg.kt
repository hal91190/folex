package fr.uvsq.folex

import java.io.FileReader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * La classe <code>Cfg</code> maintient la configuration de l'application.
 * La configuration initiale est lue dans un fichier de propriétés.
 *
 * @author hal
 * @version 2020
 */
object Cfg {
    /**
     * Le nom du fichier de propriétés contenant la configuration.
     */
    private const val PROPERTY_FILE = "folex.properties"

    /**
     * Nom du répertoire qui recevra les projets.
     */
    private const val PROJECT_DIRECTORY_NAME = "projects"
    val projectPath : Path = FileSystems.getDefault().getPath(PROJECT_DIRECTORY_NAME)
    init {
        if (!Files.exists(projectPath)) {
            Files.createDirectory(projectPath)
        }
    }

    /**
     * Les propriétés chargées depuis le fichier de configuration.
     */
    private val properties = Properties()
    init {
        FileReader(PROPERTY_FILE).use { properties.load(it) }
    }

    /**
     * La liste des repositories à considérer.
     */
    val repositoryNames = properties.getProperty("repositories").split(',')

    /**
     * Le nom du fichier CSV contentant la liste des étudiants.
     */
    val studentFilename : String = properties.getProperty("student_file")

    /**
     * L'URL de l'API GraphQL de github.
     */
    val githubApiUrl : String = properties.getProperty("github_api_url")

    /**
     * Le token github pour l'accès à l'API.
     */
    val githubToken : String = properties.getProperty("github_token")
}

package fr.uvsq.folex

import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.util.*
import javax.xml.parsers.ParserConfigurationException

class JUnitTestCollector(private val projectDir: File) {
    var jUnitResults: MutableList<JUnitResult> = ArrayList()
    @Throws(IOException::class, SAXException::class, ParserConfigurationException::class)
    fun collect() {
        val junitResultDir = File(projectDir, DEFAULT_RESULT_DIR)
        val xmlFiles = junitResultDir.listFiles { file ->
            file.isFile && file.name.toLowerCase().endsWith(".xml")
        }
            ?: return
        for (xmlFile in xmlFiles) {
            val result = JUnitResult.parse(xmlFile)
            if (result != null) jUnitResults.add(result)
        }
    }

    companion object {
        private const val DEFAULT_RESULT_DIR = "/target/surefire-reports/"
    }

}

package fr.uvsq.folex

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class JUnitResult constructor(
        val name: String,
        val nbTests: Int,
        val nbSkipped: Int,
        val nbFailures: Int,
        val nbErrors: Int,
        val executionTime: Double) {

    override fun toString(): String {
        return (name + '\t'
                + nbTests + '\t'
                + nbSkipped + '\t'
                + nbFailures + '\t'
                + nbErrors + '\t'
                + executionTime)
    }

    companion object {
        @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
        fun parse(xmlFile: File?): JUnitResult? {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(xmlFile)
            doc.documentElement.normalize()
            val testSuiteElement: Node = doc.documentElement
            if (testSuiteElement.nodeType == Node.ELEMENT_NODE) {
                val elt = testSuiteElement as Element
                return JUnitResult(
                        elt.getAttribute("name"),
                        elt.getAttribute("tests").toInt(),
                        elt.getAttribute("skipped").toInt(),
                        elt.getAttribute("failures").toInt(),
                        elt.getAttribute("errors").toInt(),
                        elt.getAttribute("time").toDouble())
            }
            return null
        }
    }
}

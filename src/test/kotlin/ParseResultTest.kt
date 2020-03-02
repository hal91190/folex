import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class ParseResultTest {
    companion object {
        val repositoryOwnerIsNull = "{\"data\":{\"repositoryOwner\":null}}"
        val allRepositoriesAreNull = "{\"data\":{\"repositoryOwner\":{\"pglp_3_2repo\":null,\"pglp_3_3repo\":null,\"pglp_3_5repo\":null}}}"
        val pglp_3_2HasNoCommit = "{\"data\":{\"repositoryOwner\":{\"pglp_3_2repo\":{\"name\":\"pglp_3.2\",\"defaultBranchRef\":null},\"pglp_3_3repo\":null,\"pglp_3_5repo\":null}}}"
        val repositoriesWithCommits = "{\"data\":{\"repositoryOwner\":{\"pglp_3_2repo\":{\"name\":\"pglp_3.2\",\"defaultBranchRef\":{\"target\":{\"history\":{\"totalCount\":6}}}},\"pglp_3_3repo\":{\"name\":\"pglp_3.3\",\"defaultBranchRef\":{\"target\":{\"history\":{\"totalCount\":3}}}},\"pglp_3_5repo\":null}}}"

        val jsonParser = Parser.default()
    }

    @Test
    fun `When the repository owner does not exist, repositoryOwner is null`() {
        val json = jsonParser.parse(StringBuilder(repositoryOwnerIsNull)) as JsonObject

        assertNotNull(json.obj("data"))
        assertNull(json.obj("data")?.obj("repositoryOwner"))
    }

    @Test
    fun `When a repository does not exist, XXXrepo is null`() {
        val json = jsonParser.parse(StringBuilder(allRepositoriesAreNull)) as JsonObject

        assertNotNull(json.obj("data"))
        assertNotNull(json.obj("data")?.obj("repositoryOwner"))
        assertNull(json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_2repo"))
        assertNull(json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_3repo"))
        assertNull(json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_5repo"))
    }

    @Test
    fun `When a repository has no commit, defaultBranchRef is null`() {
        val json = jsonParser.parse(StringBuilder(pglp_3_2HasNoCommit)) as JsonObject

        assertNotNull(json.obj("data"))
        assertNotNull(json.obj("data")?.obj("repositoryOwner"))
        assertNotNull(json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_2repo"))
        assertNull(json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_2repo")?.obj("defaultBranchRef"))
    }

    @Test
    fun `When a repository has commit, totalCount gives the count`() {
        val json = jsonParser.parse(StringBuilder(repositoriesWithCommits)) as JsonObject
        assertNotNull(json.obj("data"))
        assertNotNull(json.obj("data")?.obj("repositoryOwner"))
        assertNotNull(json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_2repo"))

        val jsonPglp_3_2 = json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_2repo")?.obj("defaultBranchRef")
        assertNotNull(jsonPglp_3_2)
        assertNotNull(jsonPglp_3_2?.obj("target"))
        assertNotNull(jsonPglp_3_2?.obj("target")?.obj("history"))
        assertEquals(6, jsonPglp_3_2?.obj("target")?.obj("history")?.int("totalCount"))
        val jsonPglp_3_3 = json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_3repo")?.obj("defaultBranchRef")
        assertNotNull(jsonPglp_3_3)
        assertNotNull(jsonPglp_3_3?.obj("target"))
        assertNotNull(jsonPglp_3_3?.obj("target")?.obj("history"))
        assertEquals(3, jsonPglp_3_3?.obj("target")?.obj("history")?.int("totalCount"))
        val jsonPglp_3_5 = json.obj("data")?.obj("repositoryOwner")?.obj("pglp_3_5repo")?.obj("defaultBranchRef")
        assertNull(jsonPglp_3_5)
    }
}

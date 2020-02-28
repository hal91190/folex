package fr.uvsq.folex

class GithubQuery(val login : String, val repositories : List<String>) {
    override fun toString(): String {
        return """
            $header
            ${content()}
            $footer
        """.trimIndent()
    }

    private fun content(): String {
        var content = StringBuilder()
        for (repository in repositories) {
            content.append("""
                ${repository.replace('.', '_')}repo: repository(name: \"$repository\") {
                    ...repositoryInfo
                }
            """.trimIndent())
        }
        return content.toString()
    }

    private val header = """
        {
            "query" : "query userRepo(${'$'}login: String = \"$login\") {
                            repositoryOwner(login: ${'$'}login) {
    """.trimIndent()

    private val footer = """
                            }
                        }

                        fragment repositoryInfo on Repository {
                          name
                          defaultBranchRef {
                            target {
                              ... on Commit {
                                history {
                                  totalCount
                                }
                              }
                            }
                          }
                        }"
        }
    """.trimIndent()
}

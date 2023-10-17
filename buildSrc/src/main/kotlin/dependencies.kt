import default.*

object KtorHtml: DependencyGroup {
    override val groupId = KtorDefaults.groupId
    override val version = KtorDefaults.version

    val htmlBuilder = dependency("ktor-server-html-builder")
}

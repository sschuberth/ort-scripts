#!/usr/bin/env kotlin

@file:CompilerOptions("-jvm-target", "21")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.1.0")
@file:DependsOn("org.apache.logging.log4j:log4j-to-slf4j:2.25.3")
@file:DependsOn("org.ossreviewtoolkit:model:79.1.0")
@file:DependsOn("org.eclipse.jgit:org.eclipse.jgit:7.5.0.202512021534-r")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.rendering.Theme

import java.io.File

import org.eclipse.jgit.ignore.IgnoreNode
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult

import org.ossreviewtoolkit.model.config.RepositoryConfiguration
import org.ossreviewtoolkit.model.readValue

object : CliktCommand(name = __FILE__.name) {
    val ortRepoConfigFile by argument()
        .file(mustExist = true, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = true, canBeSymlink = true)

    val pathToCheck by option()

    init {
        context {
            helpFormatter = { MordantHelpFormatter(context = it, "*", showDefaultValues = true) }
        }
    }

    override fun help(context: Context) = "Check ORT repository configuration for stale path excludes."

    override fun run() {
        val config = ortRepoConfigFile.readValue<RepositoryConfiguration>()

        val pathExcludes = config.excludes.paths.toMutableList()
        echo(Theme.Default.info("Found ${pathExcludes.size} path exclude(s)."))

        val analyzerRoot = ortRepoConfigFile.parentFile
        val paths = if (pathToCheck == null) {
            val ignoreNodes = mutableMapOf<File, IgnoreNode>()

            analyzerRoot.walk().onEnter { dir ->
                val gitignore = dir.parentFile.resolve(".gitignore")
                if (!gitignore.isFile) return@onEnter true

                val ignoreNode = ignoreNodes.getOrPut(gitignore) {
                    echo("Parsing '$gitignore' file...")
                    gitignore.inputStream().use { IgnoreNode().apply { parse(it) } }
                }

                val isIgnored = ignoreNode.isIgnored(dir.name, true) == MatchResult.IGNORED
                if (isIgnored) echo(Theme.Default.warning("Not entering '$dir' which is ignored in '$gitignore'."))

                !isIgnored
            }.map {
                it.toRelativeString(analyzerRoot)
            }
        } else {
            sequenceOf(checkNotNull(pathToCheck))
        }

        paths.forEach { path ->
            val match = pathExcludes.find { it.matches(path) }
            if (match != null) {
                echo("Pattern '${match.pattern}' matches e.g. '$path'.")
                pathExcludes.remove(match)
            }
        }

        if (pathExcludes.isNotEmpty()) {
            echo(Theme.Default.danger("The following path excludes match no input path:"))
            pathExcludes.forEach {
                echo(it)
            }
        } else {
            echo(Theme.Default.success("All path excludes match something."))
        }
    }
}.main(args)

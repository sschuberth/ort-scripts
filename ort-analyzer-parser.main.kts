#!/usr/bin/env kotlin

@file:CompilerOptions("-jvm-target", "21")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.1.0")
@file:DependsOn("org.ossreviewtoolkit:analyzer:76.0.0")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file

import org.ossreviewtoolkit.model.OrtResult
import org.ossreviewtoolkit.model.VcsType
import org.ossreviewtoolkit.model.readValue

object : CliktCommand(name = __FILE__.name) {
    val ortResultFile by argument()
        .file(mustExist = true, canBeFile = true, canBeDir = false, mustBeWritable = false, mustBeReadable = true, canBeSymlink = true)

    init {
        context {
            helpFormatter = { MordantHelpFormatter(context = it, "*", showDefaultValues = true) }
        }
    }

    override fun help(context: Context) = "An example to show how to parse analyzer results."

    override fun run() {
        val ortResult = ortResultFile.readValue<OrtResult>()

        val svnPackages = ortResult.analyzer?.result?.packages?.filter { it.vcsProcessed.type == VcsType.SUBVERSION }.orEmpty()

        echo("Number of Subversion packages: ${svnPackages.size}")

        svnPackages.map { it.id.toCoordinates() }.sorted().forEach {
            echo(it)
        }
    }
}.main(args)

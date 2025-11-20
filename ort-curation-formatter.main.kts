#!/usr/bin/env kotlin

@file:CompilerOptions("-jvm-target", "21")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.0.3")
@file:DependsOn("org.ossreviewtoolkit:model:71.5.0")

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file

import org.ossreviewtoolkit.model.PackageCuration
import org.ossreviewtoolkit.model.mapper

object : CliktCommand(name = __FILE__.name) {
    val curationsDir by argument()
        .file(mustExist = true, canBeFile = false, canBeDir = true, mustBeWritable = true, mustBeReadable = true, canBeSymlink = true)

    init {
        context {
            helpFormatter = { MordantHelpFormatter(context = it, "*", showDefaultValues = true) }
        }
    }

    override fun help(context: Context) = "Normalize the format of package curation files."

    override fun run() {
        curationsDir.walk().filter { it.isFile && it.extension == "yml" }.forEach { file ->
            val mapper = (file.mapper() as YAMLMapper).copy()
                .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
                .disable(YAMLGenerator.Feature.SPLIT_LINES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)

            val curations = mapper.readValue<List<PackageCuration>>(file)
            mapper.writeValue(file, curations)

            echo("Rewritten file '${file.relativeTo(curationsDir)}'.")
        }
    }
}.main(args)

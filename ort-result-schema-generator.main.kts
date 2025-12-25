#!/usr/bin/env kotlin

@file:CompilerOptions("-jvm-target", "21")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:5.0.3")
@file:DependsOn("com.github.victools:jsonschema-generator:4.38.0")
@file:DependsOn("org.ossreviewtoolkit:model:74.1.0")

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum

import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion

import org.ossreviewtoolkit.model.OrtResult

object : CliktCommand(name = __FILE__.name) {
    val schemaVersion by option().enum<SchemaVersion>().default(SchemaVersion.DRAFT_2020_12)

    init {
        context {
            helpFormatter = { MordantHelpFormatter(context = it, "*", showDefaultValues = true) }
        }
    }

    override fun help(context: Context) = "Generate a JSON schema for the `OrtResult` class."

    override fun run() {
        val config = SchemaGeneratorConfigBuilder(schemaVersion, OptionPreset.PLAIN_JSON).build()
        val generator = SchemaGenerator(config)
        val jsonSchema = generator.generateSchema(OrtResult::class.java)

        echo(jsonSchema.toPrettyString())
    }
}.main(args)

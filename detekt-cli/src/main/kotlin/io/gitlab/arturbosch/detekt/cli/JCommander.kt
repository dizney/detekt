package io.gitlab.arturbosch.detekt.cli

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import java.nio.file.Files

fun parseArguments(args: Array<out String>): CliArgs {
    val cli = CliArgs()

    val jCommander = JCommander(cli)
    jCommander.programName = "detekt"

    try {
        @Suppress("SpreadOperator")
        jCommander.parse(*args)
    } catch (@Suppress("SwallowedException") ex: ParameterException) {
        // Stacktrace in jCommander is likely irrelevant
        throw HandledArgumentViolation(ex.message, jCommander.usageAsString())
    }

    if (cli.help) {
        throw HelpRequest(jCommander.usageAsString())
    }

    return cli.apply { validate(jCommander) }
}

private fun JCommander.usageAsString(): String {
    val usage = StringBuilder()
    this.usageFormatter.usage(usage)
    return usage.toString()
}

private fun CliArgs.validate(jCommander: JCommander) {
    val violations = StringBuilder()
    val baseline = baseline

    if (createBaseline && baseline == null) {
        violations.appendLine("Creating a baseline.xml requires the --baseline parameter to specify a path.")
    }

    if (!createBaseline && baseline != null) {
        if (Files.notExists(baseline)) {
            violations.appendLine("The file specified by --baseline should exist '$baseline'.")
        } else if (!Files.isRegularFile(baseline)) {
            violations.appendLine("The path specified by --baseline should be a file '$baseline'.")
        }
    }

    if (violations.isNotEmpty()) {
        throw HandledArgumentViolation(violations.toString(), jCommander.usageAsString())
    }
}

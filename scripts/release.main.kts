#!/usr/bin/env kotlinr

/**
 * Sets one version across every manifest.
 *
 * The tree has a single version, not one per plugin. Every `version` key in the two marketplace
 * manifests and in both manifests of every plugin gets the given value, so they cannot drift.
 *
 * Usage:
 *   ./scripts/release.main.kts 1.2.0
 */

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.File
import kotlin.system.exitProcess

fun fail(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}

val version = args.singleOrNull()?.takeIf { Regex("""\d+\.\d+\.\d+""").matches(it) }
    ?: fail("Usage: release.main.kts <version>, e.g. ./scripts/release.main.kts 1.2.0")

val marketplacePath = ".claude-plugin/marketplace.json"
val repoRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
    .firstOrNull { File(it, marketplacePath).isFile }
    ?: fail("not inside the kortex repository")

val manifests = buildList {
    add(File(repoRoot, marketplacePath))
    add(File(repoRoot, ".junie-extension/marketplace.json"))
    val plugins = (File(repoRoot, "plugins").listFiles() ?: emptyArray())
        .filter { it.isDirectory }
        .sortedBy { it.name }
    for (plugin in plugins) {
        add(File(plugin, ".claude-plugin/plugin.json"))
        add(File(plugin, ".codex-plugin/plugin.json"))
    }
}
manifests.firstOrNull { !it.isFile }?.let { fail("missing manifest: $it") }

/** Every `version` key in the tree, at any depth, replaced with [version]. */
fun JsonElement.withVersion(version: String): JsonElement = when (this) {
    is JsonObject -> JsonObject(mapValues { (key, value) ->
        if (key == "version") JsonPrimitive(version) else value.withVersion(version)
    })
    is JsonArray -> JsonArray(map { it.withVersion(version) })
    else -> this
}

fun JsonElement.countVersions(): Int = when (this) {
    is JsonObject -> entries.sumOf { (key, value) -> if (key == "version") 1 else value.countVersions() }
    is JsonArray -> sumOf { it.countVersions() }
    else -> 0
}

val json = Json { prettyPrint = true; prettyPrintIndent = "  " }

// Everything is parsed and checked before anything is written, so a broken manifest cannot leave
// the tree half-released.
val sources = manifests.associateWith { file ->
    val root = try {
        json.parseToJsonElement(file.readText())
    } catch (e: SerializationException) {
        fail("$file: not valid JSON (${e.message})")
    }
    if (root.countVersions() == 0) fail("$file: no version key found")
    root
}

var total = 0
for ((file, root) in sources) {
    val count = root.countVersions()
    total += count
    file.writeText(json.encodeToString(root.withVersion(version)) + "\n")
    println("  ${file.relativeTo(repoRoot).path.padEnd(42)} $count")
}

println()
println("$total version keys set to $version. Review with `git diff`, then commit.")

#!/usr/bin/env kotlinr
// List Claude Code sessions or send one message to an exact target. Requires JDK 17+ and Kotlin 2.4.21+.
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")

import java.io.File
import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.security.MessageDigest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import kotlin.system.exitProcess

fun configDir(override: String?): File {
    val configured = override?.takeIf { it.isNotEmpty() } ?: System.getenv("CLAUDE_CONFIG_DIR")?.takeIf { it.isNotEmpty() } ?: "~/.claude"
    return when {
        configured == "~" -> File(System.getProperty("user.home"))
        configured.startsWith("~/") -> File(System.getProperty("user.home"), configured.drop(2))
        else -> File(configured)
    }
}
fun registeredSessions(root: File): List<JsonObject> = root.resolve("sessions").listFiles().orEmpty()
    .filter { it.extension == "json" }.sortedBy { it.name }.mapNotNull { registry ->
        val candidate = try { Json.parseToJsonElement(registry.readText()) as? JsonObject }
        catch (_: IOException) { null }
        catch (_: SerializationException) { null }
        candidate?.takeIf { (it["messagingSocketPath"] as? JsonPrimitive)?.contentOrNull?.isNotEmpty() == true }
    }
fun JsonObject.field(name: String) = (get(name) as? JsonPrimitive)?.contentOrNull.orEmpty()
fun selectTarget(sessions: List<JsonObject>, name: String, cwd: String?, pid: Int?): JsonObject {
    val expectedCwd = cwd?.let { File(it).canonicalFile }
    val matches = sessions.filter {
        it.field("name") == name && (pid == null || (it["pid"] as? JsonPrimitive)?.intOrNull == pid) &&
            (expectedCwd == null || File(it.field("cwd")).canonicalFile == expectedCwd)
    }
    require(matches.size == 1) {
        val details = matches.joinToString(", ") { "pid=${it.field("pid")} cwd=${it.field("cwd")}" }
        "expected one target named '$name', found ${matches.size}" + if (details.isEmpty()) "" else ": $details"
    }
    return matches.single()
}
fun readPeerToken(root: File, target: JsonObject): String {
    val pidValue = target["pid"] as? JsonPrimitive
    val pid = pidValue?.takeUnless { it.isString }?.intOrNull
    val socketValue = target["messagingSocketPath"] as? JsonPrimitive
    val socket = socketValue?.takeIf { it.isString }?.content
    require(pid != null && socket != null) { "target registry entry has no valid pid or socket path" }
    val digest = MessageDigest.getInstance("SHA-256").digest(socket.toByteArray(Charsets.UTF_8)).toHexString()
    val token = try {
        val entry = Json.parseToJsonElement(root.resolve("sessions/$pid.$digest.key").readText()) as? JsonObject
        entry?.get("peerToken") as? JsonPrimitive
    } catch (error: IOException) {
        throw IllegalArgumentException("peerToken not found for target pid=$pid", error)
    } catch (error: SerializationException) {
        throw IllegalArgumentException("peerToken not found for target pid=$pid", error)
    }
    require(token != null && token.isString && token.content.isNotEmpty()) { "peerToken is invalid for target pid=$pid" }
    return token.content
}
fun sendMessage(root: File, target: JsonObject, message: String): Int {
    require(message.isNotEmpty()) { "message is empty" }
    val token = readPeerToken(root, target)
    val auth = buildJsonObject { put("type", "auth"); put("token", token) }
    val user = buildJsonObject {
        put("type", "user")
        putJsonObject("message") { put("role", "user"); put("content", message) }
    }
    val payload = "$auth\n$user\n".toByteArray(Charsets.UTF_8)
    val socket = target.field("messagingSocketPath")
    try {
        SocketChannel.open(StandardProtocolFamily.UNIX).use { client ->
            client.connect(UnixDomainSocketAddress.of(socket))
            val buffer = ByteBuffer.wrap(payload)
            while (buffer.hasRemaining()) client.write(buffer)
            client.shutdownOutput()
        }
    } catch (error: IOException) {
        throw IOException("${error.message}: $socket", error)
    }
    return payload.size
}
fun main(): Int {
    val options = mutableMapOf<String, String>()
    var list = false
    var index = 0
    while (index < args.size) {
        val argument = args[index++]
        val option = argument.substringBefore('=')
        when (option) {
            "--help", "-h" -> {
                println("Usage: message_claude.main.kts [--config-dir DIR] --list | --name NAME [--cwd DIR] [--pid PID] [--message TEXT]")
                return 0
            }
            "--list" -> { require(argument == option) { "--list takes no value" }; list = true }
            "--config-dir", "--name", "--cwd", "--pid", "--message" -> {
                val value = if ('=' in argument) argument.substringAfter('=') else {
                    require(index < args.size) { "Missing value for $option" }; args[index++]
                }
                options[option] = value
            }
            else -> error("Unknown option: $option")
        }
    }
    val pid = options["--pid"]?.let { it.toIntOrNull() ?: throw IllegalArgumentException("--pid must be an integer") }
    val root = configDir(options["--config-dir"])
    val sessions = registeredSessions(root)
    if (list) {
        println("NAME\tSTATUS\tPID\tCWD")
        for (session in sessions) println(listOf("name", "status", "pid", "cwd").joinToString("\t") { session.field(it) })
        return 0
    }
    val name = requireNotNull(options["--name"]?.takeIf { it.isNotEmpty() }) { "--name is required unless --list is used" }
    require(options.containsKey("--message") || System.console() == null) { "--message is required when stdin is a terminal" }
    val message = options["--message"] ?: System.`in`.bufferedReader(Charsets.UTF_8).readText()
    val target = selectTarget(sessions, name, options["--cwd"], pid)
    val size = sendMessage(root, target, message)
    println("sent target=${target.field("name")} pid=${target.field("pid")} bytes=$size")
    return 0
}
try { exitProcess(main()) }
catch (error: IOException) { System.err.println("error: ${error.message}"); exitProcess(1) }
catch (error: IllegalArgumentException) { System.err.println("error: ${error.message}"); exitProcess(1) }
catch (error: IllegalStateException) { System.err.println("error: ${error.message}"); exitProcess(1) }

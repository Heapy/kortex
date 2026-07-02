# Ktor Server 3.5.x

Use this reference for Ktor server applications: routing, request/response handling, plugins, serialization,
authentication, sessions, WebSockets, SSE, testing, and deployment.

## Server Setup

Core dependency:

```kotlin
implementation("io.ktor:ktor-server-core:$ktorVersion")
```

Choose an engine and add exactly one unless the project intentionally supports several:

- CIO: `io.ktor:ktor-server-cio`
- Netty: `io.ktor:ktor-server-netty`
- Jetty Jakarta: `io.ktor:ktor-server-jetty-jakarta`
- Tomcat Jakarta: `io.ktor:ktor-server-tomcat-jakarta`

Two common startup styles:

```kotlin
// Embedded, config in code.
fun main() = embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
    module()
}.start(wait = true)

fun Application.module() {
    configureRouting()
}
```

```kotlin
// EngineMain, config in application.conf/application.yaml.
fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureRouting()
}
```

Use `EngineMain` when deployment/runtime configuration should come from files or env. Use `embeddedServer` for direct
code-driven startup, CLIs, or simple services.

## Application Structure

Recommended pattern:

```kotlin
fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureHTTP()
    configureRouting()
}

fun Application.configureRouting() = routing {
    route("/api") {
        healthRoutes()
        userRoutes()
    }
}

fun Route.healthRoutes() {
    get("/health") { call.respondText("OK") }
}
```

Keep route handlers small. Move domain logic to services. Inject dependencies through constructor/DI instead of global
singletons where possible.

## Configuration

Ktor supports config in code and config files. In 3.5, `ApplicationConfig.getAs<T>()` can deserialize root configuration
into a data class when the project has the right config serialization setup.

Common values:

- host/port
- deployment mode/development mode
- module names
- database/API secrets via environment variables
- plugin-specific settings

Do not hardcode secrets in `application.conf`, YAML, or source. Prefer env-backed config.

## Dependency Injection

Ktor 3.5 docs include built-in DI via `ktor-server-di`.

Use DI for services, repositories, clients, and lifecycle-managed resources. Test DI wiring with Ktor test application
rather than booting the production server.

Typical lifecycle guidance:

- Register long-lived resources at application startup.
- Resolve dependencies inside route modules or handlers.
- Close/dispose resources on application stop when not handled by the DI container.

If a project already uses Koin, Dagger, Guice, Spring, or manual constructor injection, preserve that style unless the
user asks to migrate.

## Routing

Install/define routing:

```kotlin
fun Application.configureRouting() = routing {
    get("/") { call.respondText("Hello") }
    route("/users") {
        get { /* list */ }
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        }
        post { /* create */ }
    }
}
```

Path patterns:

- `{id}` captures one segment: `/users/{id}`
- `{...}` tailcard captures remaining path segments
- `*` wildcard matches one segment without capturing
- Regex routes are available for advanced matching

Use typed `Resources` for type-safe routing when routes are shared or URL construction must be safe. Add the resource
plugin/artifact required by the project before using it.

## Requests

Common request APIs:

```kotlin
val id = call.parameters["id"]
val q = call.request.queryParameters["q"]
val header = call.request.headers[HttpHeaders.Authorization]
val cookie = call.request.cookies["session"]
val dto = call.receive<CreateUser>()
```

Body handling:

- Raw text/bytes/channels: use `receiveText`, byte/channel APIs, or multipart APIs.
- Objects: requires `ContentNegotiation` plus serializer.
- Forms: use `receiveParameters()` for `application/x-www-form-urlencoded`.
- Multipart: iterate parts and dispose file parts/resources after processing.

A request body is normally consumable once. If you truly need multiple reads, install `DoubleReceive`, but prefer to read
once and pass a parsed value onward.

## Responses

Common response APIs:

```kotlin
call.respond(HttpStatusCode.Created, dto)
call.respondText("OK", ContentType.Text.Plain)
call.respondBytes(bytes, ContentType.Application.OctetStream)
call.respondFile(file)
call.respondRedirect("/login")
call.response.headers.append(HttpHeaders.CacheControl, "no-store")
call.response.cookies.append("name", "value")
```

Objects require `ContentNegotiation`. For HTML, either use `respondHtml`/HTML DSL, templates, or static content depending
on the project style.

## Content Negotiation and Serialization

Dependencies:

```kotlin
implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
```

Install:

```kotlin
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UserDto(val id: Long, val name: String)

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            prettyPrint = false
        })
    }
}
```

Alternative serializers: Gson, Jackson, Kotlinx XML, CBOR, ProtoBuf. Match the artifact and install call.

## Plugins

Plugin rule: add dependency if separate, import correct server package, then `install(Plugin) { ... }`.

Common HTTP plugins:

```kotlin
install(DefaultHeaders)
install(Compression) { gzip() }
install(CORS) {
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowHeader(HttpHeaders.ContentType)
    anyHost() // development only; restrict in production
}
install(StatusPages) {
    exception<IllegalArgumentException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, mapOf("error" to cause.message))
    }
    status(HttpStatusCode.NotFound) { call, _ -> call.respondText("Not found") }
}
```

Other server plugins/topics:

- `PartialContent` for range requests
- `CachingHeaders` and `ConditionalHeaders`
- `HSTS` and `HttpsRedirect`
- `ForwardedHeaders`/`XForwardedHeaders` behind proxies
- `RateLimit` for request limiting
- `RequestValidation` for input validation
- `MethodOverride` for `X-HTTP-Method-Override`
- `CallLogging`, `CallId`, Micrometer/Dropwizard metrics, OpenTelemetry

Be conservative with CORS and forwarded headers in production. Only trust proxy headers if the app is actually behind a
trusted proxy/load balancer.

## Authentication and Authorization

Dependency:

```kotlin
implementation("io.ktor:ktor-server-auth:$ktorVersion")
```

Optional:

```kotlin
implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
implementation("io.ktor:ktor-server-auth-ldap:$ktorVersion")
```

Install and protect routes:

```kotlin
install(Authentication) {
    basic("basic") {
        realm = "admin"
        validate { credentials ->
            if (credentials.name == "admin" && credentials.password == "secret") UserIdPrincipal(credentials.name)
            else null
        }
    }
}

routing {
    authenticate("basic") {
        get("/admin") {
            val principal = call.principal<UserIdPrincipal>()
            call.respondText("Hello ${principal?.name}")
        }
    }
}
```

Supported providers include Basic, Digest, Bearer, Form, Session, JWT, LDAP, OAuth, and custom providers.

Ktor 3.5 Digest auth is RFC 7616-oriented:

```kotlin
digest("auth") {
    realm = "MyRealm"
    algorithms = listOf(DigestAlgorithm.SHA_512_256, DigestAlgorithm.MD5)
    digestProvider { userName, realm, algorithm ->
        val password = lookupPassword(userName) ?: return@digestProvider null
        algorithm.toDigester().digest("$userName:$realm:$password".toByteArray())
    }
}
```

For new code, prefer SHA-256/SHA-512-256 over MD5 unless legacy compatibility requires MD5.

Custom providers in 3.5 can use a suspending `authenticate { context -> ... }` lambda.

## Sessions

Dependency:

```kotlin
implementation("io.ktor:ktor-server-sessions:$ktorVersion")
```

Example:

```kotlin
data class UserSession(val userId: Long)

install(Sessions) {
    cookie<UserSession>("user_session") {
        cookie.httpOnly = true
        cookie.secure = true
        cookie.extensions["SameSite"] = "lax"
        // In 3.5, sessions can be configured to send cookies only when modified.
    }
}

routing {
    get("/me") {
        val session = call.sessions.get<UserSession>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
        call.respond(session)
    }
    post("/logout") {
        call.sessions.clear<UserSession>()
        call.respond(HttpStatusCode.NoContent)
    }
}
```

Session payload can be cookie/header-based and client-side or server-side. Protect client-side payloads with signing or
signing+encryption. Use server-side storage for sensitive or large session state.

## Static Content, SPA, Templates

Static content:

```kotlin
routing {
    staticResources("/static", "static")
    singlePageApplication {
        react("build/dist")
    }
}
```

Templating options include HTML DSL, CSS DSL, FreeMarker, Velocity, Mustache, Thymeleaf, Pebble, and JTE. Add the
matching artifact and keep template rendering separate from API DTO serialization.

## WebSockets

Dependency:

```kotlin
implementation("io.ktor:ktor-server-websockets:$ktorVersion")
```

Example:

```kotlin
install(WebSockets)

routing {
    webSocket("/ws") {
        send("connected")
        for (frame in incoming) {
            if (frame is Frame.Text) send("echo: ${frame.readText()}")
        }
    }
}
```

For typed messages, add WebSocket serialization support and use the docs-specific serialization APIs. Deflate and custom
extensions are available but should be explicitly needed and tested with clients.

## Server-Sent Events

SSE is appropriate for one-way server-to-client event streams. Prefer SSE over WebSockets when the client only listens.
Use WebSockets for bidirectional interactive communication.

Ensure responses are not buffered by proxies and that heartbeat/retry behavior matches deployment constraints.

## Testing

Dependency:

```kotlin
testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
testImplementation(kotlin("test"))
```

Basic test:

```kotlin
class ApplicationTest {
    @Test
    fun health() = testApplication {
        application { module() }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}
```

Use the test host to:

- load modules explicitly or from config
- install extra test routes/plugins
- customize environment/config
- configure the test client
- test JSON POST/PUT, forms, multipart, cookies, HTTPS, and WebSockets
- mock external services where practical

Do not bind real ports for unit/integration tests unless testing deployment wiring or E2E behavior.

## Deployment and Packaging

Options:

- fat JAR via Gradle Shadow or Maven Assembly
- Gradle application plugin/distributions
- WAR for servlet containers
- GraalVM native image for supported cases
- Docker/Docker Compose
- cloud targets such as Azure App Service, Google App Engine, Heroku, Dokku, Sevalla, Elastic Beanstalk

Production checklist:

- configure host/port from environment
- do not enable development-only CORS or debug logging
- configure TLS at proxy/load balancer or Ktor as appropriate
- trust forwarded headers only behind trusted proxies
- expose `/health` and metrics/tracing if needed
- set shutdown behavior and resource cleanup

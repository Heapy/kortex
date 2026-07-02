# Ktor Client 3.5.x

Use this reference for Ktor HTTP client applications: engines, requests, responses, serialization, auth, cookies,
timeouts, retries, logging, WebSockets, SSE, testing, and Kotlin Multiplatform clients.

## Client Setup

Core dependency:

```kotlin
implementation("io.ktor:ktor-client-core:$ktorVersion")
```

Add one or more engines appropriate to the target platform:

- JVM/Android/Native/JS/WasmJs: `ktor-client-cio`
- JVM/Android: `ktor-client-okhttp`
- JVM: `ktor-client-apache5`, `ktor-client-java`, `ktor-client-jetty-jakarta`
- Android: `ktor-client-android`
- Apple/native: `ktor-client-darwin`
- Native: `ktor-client-curl`, `ktor-client-winhttp`
- JavaScript: `ktor-client-js`

For Gradle multiplatform, Gradle resolves platform-specific artifacts automatically. For Maven or `.main.kts`, use
platform-specific coordinates where necessary, for example `ktor-client-cio-jvm`.

## Create, Configure, Close

```kotlin
val client = HttpClient(CIO) {
    expectSuccess = true
    engine {
        requestTimeout = 15_000
    }
    install(ContentNegotiation) { json() }
}

try {
    val text: String = client.get("https://example.com").body()
} finally {
    client.close()
}
```

Guidance:

- Reuse `HttpClient`; do not create one per request.
- Close it on application shutdown or use DI/lifecycle management.
- Install plugins once at construction time.
- Use explicit engine selection when behavior matters; default engine selection is convenient but less predictable.

## Multiplatform Pattern

In KMP, put common configuration in `commonMain` and engine dependencies in platform source sets:

```kotlin
// commonMain
expect fun httpClient(config: HttpClientConfig<*>.() -> Unit = {}): HttpClient

fun apiClient() = httpClient {
    install(ContentNegotiation) { json() }
    install(HttpTimeout) { requestTimeoutMillis = 15_000 }
}
```

```kotlin
// jvmMain/androidMain/etc.
actual fun httpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient = HttpClient(CIO) {
    config(this)
}
```

Preserve the project's existing KMP pattern. Some projects use dependency injection instead of `expect/actual` factories.

## Requests

Basic verbs:

```kotlin
val response: HttpResponse = client.get("https://api.example.com/users")
val created: User = client.post("https://api.example.com/users") {
    contentType(ContentType.Application.Json)
    setBody(CreateUser("Ada"))
}.body()
```

URL construction:

```kotlin
client.get {
    url {
        protocol = URLProtocol.HTTPS
        host = "api.example.com"
        path("v1", "users")
        parameters.append("page", "1")
        fragment = "top"
    }
}
```

Request parameters:

```kotlin
client.get("https://api.example.com") {
    header(HttpHeaders.Accept, ContentType.Application.Json)
    bearerAuth(token)
    cookie("tracking", "off")
}
```

Bodies:

- Text: `setBody("hello")` with an appropriate `contentType`.
- Objects: requires `ContentNegotiation` and serializer.
- Forms: `submitForm(...)` for URL-encoded forms.
- File/multipart: `submitFormWithBinaryData(...)` or `MultiPartFormDataContent(...)`.
- Binary/streaming: use byte arrays, channels, or IO APIs appropriate to the platform.

Parallel requests are just coroutines; use structured concurrency and cancellation.

## Responses

```kotlin
val response = client.get("https://api.example.com/users/1")
println(response.status)
println(response.headers[HttpHeaders.ContentType])

val dto: User = response.body()
val text: String = response.bodyAsText()
```

Streaming:

```kotlin
client.prepareGet("https://example.com/large.bin").execute { response ->
    val channel: ByteReadChannel = response.body()
    while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
        // process and release packet according to ktor-io APIs
    }
}
```

Always stream large downloads/uploads; do not load unbounded content into memory.

## Content Negotiation and Serialization

Dependencies:

```kotlin
implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
```

Install:

```kotlin
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class User(val id: Long, val name: String)

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
}
```

Alternative serializers: Gson, Jackson, Kotlinx XML, CBOR, ProtoBuf. Match the artifact to the install call and payload
format. With Kotlinx serialization, ensure models have `@Serializable` and the Kotlin serialization compiler plugin is
configured in real projects.

## Response Validation and Errors

`expectSuccess = true` makes Ktor throw for non-2xx responses. Default exception types include redirect, client request,
and server response exceptions.

```kotlin
val client = HttpClient(CIO) {
    expectSuccess = true
    HttpResponseValidator {
        validateResponse { response ->
            if (response.status == HttpStatusCode.Accepted) return@validateResponse
        }
        handleResponseExceptionWithRequest { cause, request ->
            // map/log/rethrow
        }
    }
}
```

Use typed domain errors at app boundaries. Avoid swallowing response bodies needed for debugging; read them carefully
because response streams may be single-use.

## Timeouts, Retries, Redirects

Timeouts:

```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 15_000
    connectTimeoutMillis = 5_000
    socketTimeoutMillis = 15_000
}
```

Retries:

```kotlin
install(HttpRequestRetry) {
    retryOnServerErrors(maxRetries = 3)
    exponentialDelay()
}
```

Redirects are handled by `HttpRedirect`; configure if the app needs strict redirect behavior or auth/header preservation
rules. Be careful retrying non-idempotent POST/PUT/PATCH requests.

## Default Request, Headers, User Agent

```kotlin
install(DefaultRequest) {
    url("https://api.example.com/v1/")
    header(HttpHeaders.Accept, ContentType.Application.Json)
}
install(UserAgent) { agent = "my-app/1.0" }
```

Use `DefaultRequest` for base URL and headers shared by all calls. Keep per-request auth/correlation values explicit
unless the project has an interceptor/plugin for them.

## Authentication

Dependency:

```kotlin
implementation("io.ktor:ktor-client-auth:$ktorVersion")
```

Basic:

```kotlin
install(Auth) {
    basic {
        credentials { BasicAuthCredentials(username = "user", password = "pass") }
        sendWithoutRequest { request -> request.url.host == "api.example.com" }
    }
}
```

Bearer:

```kotlin
install(Auth) {
    bearer {
        loadTokens { BearerTokens(accessToken, refreshToken) }
        refreshTokens {
            // call token endpoint, return BearerTokens or null
        }
        sendWithoutRequest { request -> request.url.host == "api.example.com" }
    }
}
```

Digest is supported through the client auth plugin. Ktor 3.5 server Digest follows RFC 7616; if interoperating with it,
prefer modern algorithms and test against the real challenge headers.

Token caching exists inside providers. Clear cached tokens or control caching behavior when implementing logout, account
switching, or token revocation.

## Cookies and Caching

Cookies:

```kotlin
install(HttpCookies) // in-memory by default
```

Use a persistent cookie storage only when the application requires it and the platform supports secure storage.

Caching:

```kotlin
install(HttpCache)
```

Treat client-side caching as behavior-changing. Verify headers and invalidation semantics.

## Logging, Call IDs, OpenTelemetry

Logging dependency:

```kotlin
implementation("io.ktor:ktor-client-logging:$ktorVersion")
```

Install:

```kotlin
install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.INFO
    sanitizeHeader { header -> header == HttpHeaders.Authorization }
}
```

Never log tokens, cookies, API keys, or PII. Use `sanitizeHeader` and project logging policy.

Use Call ID/tracing plugins or OpenTelemetry where the service already has distributed tracing. Propagate correlation IDs
explicitly when integrating with server `CallId`.

## Content Encoding, BOM, Text

- `ContentEncoding` handles gzip/deflate/br where configured/supported.
- BOM remover handles byte-order marks in text responses.
- Plain text/charsets plugin support exists for controlling text conversion.

Only add these plugins when there is a concrete encoding/interop issue.

## Proxy and SSL

Proxy support depends on engine. Configure at engine/client level according to the chosen engine. SSL customization also
varies by engine/platform. Prefer standard trust stores; custom trust managers/pinning should be security-reviewed.

## WebSockets

Dependency depends on client core/engine support; add WebSocket artifacts if the project requires them.

```kotlin
val client = HttpClient(CIO) { install(WebSockets) }

client.webSocket("wss://example.com/ws") {
    send("hello")
    for (frame in incoming) {
        if (frame is Frame.Text) println(frame.readText())
    }
}
```

For typed WebSocket messages, install/configure WebSocket serialization and test both ends.

## Server-Sent Events

Use SSE for one-way event streams:

- good for notifications/progress streams
- simpler than WebSockets for server-to-client only
- needs reconnect/heartbeat/backoff strategy
- beware proxy buffering and mobile/background lifecycle behavior

## Custom Client Plugins

Use a custom plugin for cross-cutting behavior such as signing, correlation IDs, custom metrics, request/response mapping,
or tenant headers. Do not implement custom plugins for one-off per-request logic.

General shape:

```kotlin
val MyPlugin = createClientPlugin("MyPlugin") {
    onRequest { request, _ ->
        request.headers.append("X-My-Header", "value")
    }
}
```

## Testing

Dependency:

```kotlin
testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
```

Use `MockEngine`:

```kotlin
val mockEngine = MockEngine { request ->
    respond(
        content = """{"id":1,"name":"Ada"}""",
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )
}

val client = HttpClient(mockEngine) {
    install(ContentNegotiation) { json() }
}

val user: User = client.get("https://api.example.com/users/1").body()
```

Share production client configuration by extracting a `HttpClientConfig<*>.() -> Unit` block or factory. Test retry,
timeout, serialization, auth, and error mapping without real network calls whenever possible.

## Engine Selection Notes

- CIO is broad and often the default choice for simple clients.
- OkHttp is common on JVM/Android and supports features such as custom DNS resolvers in Ktor 3.5 docs.
- Apache5 is JVM-oriented and also has custom DNS resolver support in 3.5 docs.
- Java engine uses JDK HTTP client.
- Darwin is natural on Apple platforms.
- JS engine follows browser/JS platform limitations.

Feature limitations vary by engine: HTTP/2, WebSockets, security customization, proxy support, logging, and timeout
semantics are not identical. If a task depends on one of those, verify against the chosen engine docs and run a real test.

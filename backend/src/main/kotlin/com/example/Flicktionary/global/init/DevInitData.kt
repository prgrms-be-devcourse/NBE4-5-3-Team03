package com.example.Flicktionary.global.init

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

@Profile("prod")
@Configuration
class DevInitData {
    @Bean
    fun devApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            generateApiJsonFile()
            executeCommand()
        }
    }

    private fun executeCommand() {
        val tsGenCommand = tsGenCommand
        val processBuilder = ProcessBuilder(tsGenCommand)
        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.lines().forEach { x: String? -> println(x) }
        }
        val exitCode = process.waitFor()
        println("프로세스 종료 코드: $exitCode")
    }

    private fun generateApiJsonFile() {
        val filePath = Path.of(API_JSON_FILE)


        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET().build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                throw RuntimeException("API 요청 실패: HTTP 상태 코드 " + response.statusCode())
            }

            Files.writeString(
                filePath,
                response.body(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            println("JSON 데이터가 ${filePath.toAbsolutePath()}에 저장되었습니다.")
        }
    }

    companion object {
        private const val API_URL = "http://localhost:8080/v3/api-docs"
        private const val API_JSON_FILE = "apiV1.json"

        private val tsGenCommand: List<String>
            // OS에 따라 명령어 설정
            get() {
                val os = System.getProperty("os.name").lowercase(Locale.getDefault())

                return if (os.contains("win")) {
                    listOf(
                        "cmd.exe",
                        "/c",
                        ("npx --package typescript --package openapi-typescript --package punycode openapi-typescript "
                                + API_JSON_FILE + " -o ../frontend/src/lib/backend/apiV1/schema.d.ts")
                    )
                } else {
                    listOf(
                        "sh",
                        "-c",
                        ("npx --package typescript --package openapi-typescript --package punycode openapi-typescript "
                                + API_JSON_FILE + " -o ../frontend/src/lib/backend/apiV1/schema.d.ts")
                    )
                }
            }
    }
}
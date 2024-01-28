package com.example.client.controller

import com.example.client.client.ArticleClient
import com.example.client.client.dto.ArticleResponse
import com.example.client.client.dto.ArticleSaveCommand
import com.example.client.client.dto.ArticleUpdateCommand
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
class ArticleController(
    private val articleClient: ArticleClient,
) {

    private val log = KotlinLogging.logger { }

    @PostMapping("/articles")
    fun save(@RequestBody command: ArticleSaveCommand): ArticleResponse {
        return articleClient.save(command)
    }

    @GetMapping("/articles/{id}")
    fun get(@PathVariable id: String): ArticleResponse? {
        return articleClient.get(id)
    }

    @PatchMapping("/articles/{id}")
    fun update(@PathVariable id: String, @RequestBody command: ArticleUpdateCommand): ArticleResponse? {
        return articleClient.update(
            id,
            ArticleUpdateCommand(
                title = command.title,
                body = command.body,
            ),
        )
    }

    @DeleteMapping("/articles/{id}")
    fun delete(@PathVariable id: String): ArticleResponse? {
        return articleClient.delete(id)
    }

    @GetMapping("/error")
    fun error() {
        articleClient.error()
    }

    @PostMapping("/upload")
    fun upload(@RequestPart file: MultipartFile): String {
        return articleClient.upload(file)
    }

    @GetMapping("/download/{path}")
    fun download(@PathVariable path: String, response: HttpServletResponse) {

        articleClient.download(path).body().asInputStream().use { ins ->
            response.outputStream.use { os -> ins.transferTo(os) }
        }
    }
}

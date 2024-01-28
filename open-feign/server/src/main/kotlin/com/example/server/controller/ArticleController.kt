package com.example.server.controller

import com.example.server.controller.dto.ArticleSaveCommand
import com.example.server.controller.dto.ArticleUpdateCommand
import com.example.server.domain.Article
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@RestController
class ArticleController {

    val articleRepository = ConcurrentHashMap<String, Article>()

    private val log = KotlinLogging.logger {  }

    @PostMapping("/articles")
    fun save(@RequestHeader("Authorization") authorization: String, @RequestBody command: ArticleSaveCommand): Article {
        log.info { "Authorization : $authorization" }

        val id = UUID.randomUUID().toString()
        val article = Article(
            id = id,
            title = command.title,
            body = command.body,
            registeredDate = command.requestDate,
        )

        articleRepository[id] = article
        return article
    }

    @GetMapping("/articles/{id}")
    fun get(@PathVariable id: String): Article? {
        return articleRepository[id]
    }

    @PatchMapping("/articles/{id}")
    fun update(@PathVariable id: String, @RequestBody command: ArticleUpdateCommand): Article? {
        val article = articleRepository[id]
            ?.apply {
                this.body = command.body
                this.title = command.title
            }
        return article
    }

    @DeleteMapping("/articles/{id}")
    fun delete(@PathVariable id: String): Article? {
        return articleRepository[id]?.let {
            articleRepository.remove(id)
        }
    }

    @GetMapping("/error")
    fun error(): ResponseEntity<Unit> {
        val listOf = listOf(
            ResponseEntity.status(HttpStatusCode.valueOf(401)),
            ResponseEntity.status(HttpStatusCode.valueOf(500)),
            ResponseEntity.status(HttpStatusCode.valueOf(403)),
        )

        return listOf.random().build()
    }

    val filePath = ConcurrentHashMap<String, String>()

    @PostMapping("/upload")
    fun upload(@RequestPart file: MultipartFile): String {

        val path = UUID.randomUUID().toString()
        val extension = getExtension(file.originalFilename!!)
        val newFile = File("/Users/user/Desktop/${path.plus(".$extension")}")
        newFile.createNewFile()

        filePath[path] = file.originalFilename!!

        file.inputStream.use { ins ->
            newFile.outputStream().use { os ->
                ins.transferTo(os)
            }
        }

        return path
    }

    @GetMapping("/download/{path}")
    fun download(@PathVariable path: String, response: HttpServletResponse) {
        val fileName = filePath[path]!!
        val extension = getExtension(fileName)
        val file = File("/Users/user/Desktop/${path.plus(".$extension")}")

        file.inputStream().use { ins ->
            response.outputStream.use { os ->
                ins.transferTo(os)
            }
        }

        response.setHeader(
            "Content-Disposition",
            "attachment; filename=${URLEncoder.encode(fileName, StandardCharsets.UTF_8)}",
        )
    }

    private fun getExtension(fileName: String): String {
        if (!fileName.contains(".") || fileName.endsWith(".")) {
            throw IllegalArgumentException("FileName does not have an extension. fileName : $fileName")
        }
        return fileName.substringAfterLast('.')
    }
}

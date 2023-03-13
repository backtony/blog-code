package com.example.springmasterslave.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springmasterslave.domain.Book;
import com.example.springmasterslave.dto.SaveBookRequest;
import com.example.springmasterslave.service.BookService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping("/books")
    public Long saveBook(@RequestBody SaveBookRequest saveBookRequest) {
        return bookService.save(saveBookRequest.getTitle());
    }

    @GetMapping("/books/{bookId}")
    public Book findBook(@PathVariable Long bookId) {
        return bookService.findBook(bookId);
    }
}

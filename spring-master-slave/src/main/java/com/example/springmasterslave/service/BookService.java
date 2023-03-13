package com.example.springmasterslave.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.springmasterslave.domain.Book;
import com.example.springmasterslave.domain.BookRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Long save(String title) {
        return bookRepository.save(
            Book.builder()
                .title(title)
                .build()
        ).getId();
    }

    @Transactional(readOnly = true)
    public Book findBook(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("book not found"));
    }
}

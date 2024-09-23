package org.example.librarybooks.Models;

import org.example.librarybooks.Data.BookRepository;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public int bookCount(BookRepository bookRepository) {
        return bookRepository.count();
    }
}

package org.example.librarybooks.Models;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String isbn;
    private Date placedAt;
    private String FIO;
    private Date returnedAt;
//    public static int bookCount = book;

    public Book(Long id, String title, String isbn, Date placedAt, String FIO, Date returnedAt) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.placedAt = placedAt;
        this.FIO = FIO;
        this.returnedAt = returnedAt;
    }

    public Book() {

    }
    public Book( String title, String isbn, Date placedAt, String FIO, Date returnedAt) {
        this.title = title;
        this.isbn = isbn;
        this.placedAt = placedAt;
        this.FIO = FIO;
        this.returnedAt = returnedAt;
    }

}

package org.example.librarybooks.Models;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


@Data
@Entity
//@AllArgsConstructor
//@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private String isbn;
    @NotNull
    private Date placedAt;
    @NotNull
    private String FIO;
    @NotNull
    private Date returnedAt;

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

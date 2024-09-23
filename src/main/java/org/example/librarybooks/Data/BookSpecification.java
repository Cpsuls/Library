package org.example.librarybooks.Data;

import jakarta.persistence.criteria.Path;
import org.example.librarybooks.Models.Book;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class BookSpecification {
    public static Specification<Book> findByField(String field, String value) {
        return (root, query, builder) -> {
            if (field.equals("placedAt") || field.equals("returnedAt")) {
                Path<Date> path = root.get(field);
                return builder.equal(path, java.sql.Timestamp.valueOf(value));
            } else {
                Path<String> path = root.get(field);
                return builder.equal(path, value);
            }
        };
    }
}

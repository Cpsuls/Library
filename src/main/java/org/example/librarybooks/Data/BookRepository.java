package org.example.librarybooks.Data;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.Path;
import org.example.librarybooks.Models.Book;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookRepository extends PagingAndSortingRepository<Book,Long> {
    List<Book> findAll();

    @Modifying
    @Query("DELETE FROM Book b WHERE b.id = :id")
    void delete(@Param("id") Long id);

    void save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findByTitle(@Param("title") String title);

    void deleteByTitle(String title);

    int count();


    List<Book> findByIsbn(@Param("isbn") String isbn);


    List<Book> findByPlacedAt(@Param("date") Date date);

//    @Query("SELECT b FROM Book b WHERE (:spec)")
//    List<Book> findAll(@Param("spec") Specification<Book> specification);

    List<Book> findByFIO(@Param("str") String str);


    List<Book> findByReturnedAt(@Param("date") Date date);

//    List<Book> findByTitleAndIsbn(String title, String isbn);
//    @Query("SELECT b FROM Book b WHERE b.#{#field} = :value")
//    List<Book> findByField(@Param("field") String field, @Param("value") String value);


//    @Query("SELECT COUNT(b) FROM Book b WHERE b.placedAt = :day")
//    int countByPlacedAt(@Param("day") @Temporal(TemporalType.TIMESTAMP) Date day);
}

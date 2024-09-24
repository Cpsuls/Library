package org.example.librarybooks.Controllers;
import jakarta.servlet.http.HttpServletRequest;
import org.jfree.chart.ChartUtilities;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.librarybooks.Data.BookRepository;
import org.example.librarybooks.Models.Book;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@Slf4j
public class BookController {
    BookRepository bookRepository;
    @Autowired
    private int bookCount;
    private static Long BookIdToChange;


    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }


    @GetMapping("/books")
    public String getBooks(Model model) {
        PageRequest pageRequest = PageRequest.of(0, 30, Sort.by("placedAt").descending());
        Iterable<Book> recentBooks = bookRepository.findAll(pageRequest).getContent();
        model.addAttribute("books", recentBooks);
        model.addAttribute("count", bookCount);
        return "books";
    }


    @GetMapping("/books/title")
    public String getBooks(@RequestParam String title, Model model) {
        List<Book> books = bookRepository.findByTitle(title);
        model.addAttribute("books", books);
        model.addAttribute("count", bookCount);
        return "books";
    }


    @GetMapping("/addBook")
    public String addBook(Model model) {
        return "addBook";
    }

    @PostMapping("/books")
    public String addBook(
            @RequestParam("title") String title,
            @RequestParam("isbn") String isbn,
            @RequestParam("placedAt") @DateTimeFormat(pattern = "yyyy-MM-dd") Date placedAt,
            @RequestParam("fio") String fio,
            @RequestParam("returnedAt") @DateTimeFormat(pattern = "yyyy-MM-dd") Date returnedAt) {
        Book book = new Book(title.trim(), isbn.trim(), placedAt, fio.trim(), returnedAt);
        bookRepository.save(book);
        bookCount++;
        return "redirect:/books";
    }

    @PostMapping("/DeleteBook")
    @Transactional
    public String DeleteBook(@RequestParam String title, Model model) {
        bookRepository.deleteByTitle(title);
        bookCount--;
        return "redirect:/books";
    }

    @GetMapping("/DeleteBook")
    public String predelete() {
        return "/DeleteBook";
    }


    @GetMapping("/searchByTitle")
    public String searchByTitle() {
        return "searchByTitle";
    }



    @PostMapping("/searchByTitle")
    public String searchByField(@RequestParam("info") String info,
                                Model model) throws ParseException {
        try {
            List<Book> books = List.of();
            if (!bookRepository.findByTitle(info.trim()).isEmpty()) {
                books = bookRepository.findByTitle(info.trim());
            } else if (!bookRepository.findByIsbn(info.trim()).isEmpty()) {
                books = bookRepository.findByIsbn(info.trim());

            } else if (!bookRepository.findByFIO(info.trim()).isEmpty()) {
                books = bookRepository.findByFIO(info.trim());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                Date date = sdf.parse(info.trim());
                if (!bookRepository.findByPlacedAt(date).isEmpty()) {
                    books = bookRepository.findByPlacedAt(date);

                } else {
                    books = bookRepository.findByReturnedAt(date);
                }

            }
            model.addAttribute("books", books);
            BookIdToChange = books.get(0).getId();
            return "bookList";
        } catch (Exception e){
            return "redirect:/books";
        }
    }




    @GetMapping("/editBook")
    public String editBook(Model model) {
        Book book = bookRepository.findById(BookIdToChange).orElseThrow();
        System.out.println(BookIdToChange);
        model.addAttribute("book", book);
        return "editBook";
    }
    @PostMapping("/editBook")
    public String updateBook(
            @RequestParam String title,
            @RequestParam String isbn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date placedAt,
            @RequestParam String FIO,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date returnedAt
    ) {
        Book book = bookRepository.findById(BookIdToChange).orElseThrow();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPlacedAt(placedAt);
        book.setFIO(FIO);
        book.setReturnedAt(returnedAt);
        bookRepository.save(book);
        return "redirect:/books";
    }

    @GetMapping("/chart")
    public String getBooksChart(Model model, HttpServletRequest request) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Book> books = bookRepository.findAll();
        Map<String, Long> bookCounts = books.stream()
                .collect(Collectors.groupingBy(book -> book.getPlacedAt().toString(), Collectors.counting()));
        for (Map.Entry<String, Long> entry : bookCounts.entrySet()) {
            dataset.addValue(entry.getValue(), "Books", entry.getKey());
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Books by Days",
                "Days",
                "Number of Books",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );


        CategoryAxis axis = chart.getCategoryPlot().getDomainAxis();
        axis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        axis.setTickLabelInsets(new RectangleInsets(40, 40, 40, 40));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ChartUtilities.writeChartAsPNG(bos, chart, 800, 600);
        } catch (IOException e) {
            log.error("Error generating chart", e);
        }
        byte[] chartData = bos.toByteArray();
        String filename = "chart.png";
        String filePath = "C:\\Users\\kosta\\OneDrive\\Документы\\Графики\\" + filename;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(chartData);
        } catch (IOException e) {
            log.error("Error saving chart to file", e);
        }
        model.addAttribute("chartData", Base64.getEncoder().encodeToString(chartData));
        return "chart";
    }





































    //import static org.example.librarybooks.Models.Book.bookCount;
//    List<Book> books = bookRepository.findAll();
//        model.addAttribute("books", books);
//        model.addAttribute("count",bookCount);
    //    @PostMapping("/searchByTitle")
//    public String searchByField(@RequestParam("field") String field, @RequestParam("value") String value, Model model) {
//        Specification<Book> specification = BookSpecification.findByField(field, value);
//        List<Book> books = bookRepository.findAll(specification);
//        model.addAttribute("books", books);
//        BookIdToChange=books.get(0).getId();
//        return "bookList";
//    }
//@PostMapping("/searchByTitle")
//public String searchByField(@RequestParam("field") String field, @RequestParam("value") String value,
//                            Model model) throws ParseException {
//        List<Book> books = List.of();
//                switch (field) {
//                    case "title"-> books = bookRepository.findByTitle(value.trim());
//                    case "isbn"->  books = bookRepository.findByIsbn(value.trim());
//                    case "placedAt"->{
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//                        Date date = sdf.parse(value.trim());
//                        books = bookRepository.findByPlacedAt(date);}
//                    case "FIO"-> books = bookRepository.findByFIO(value.trim());
//                    case "returnedAt"->{
//                        SimpleDateFormat sdfr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//                        Date dater = sdfr.parse(value.trim());
//                        books = bookRepository.findByReturnedAt(dater);}
//                }
//        model.addAttribute("books", books);
//        BookIdToChange=books.get(0).getId();
//        return "bookList";
//    }
//    @PostMapping("/books")
//    public String getBookks(Model model) {
//        List<Book> books = bookRepository.findAll();
//        model.addAttribute("books", books);
//        model.addAttribute("count",bookCount);
//        return "books";
//    }


//    @DeleteMapping("/deleteBook")
//    public String DeleteBook(@RequestParam Long bookId){
//        try{
//            bookRepository.delete(bookId);
//            bookCount--;
//            return "redirect:/books";
//        }catch(EmptyResultDataAccessException e){
//            log.info("No such book:",bookId);
//            return "redirect:/books";
//        }
//    }


//    @PatchMapping("/books/{id}")
//    public String updateBookName(@PathVariable Long id, @RequestBody Book book) {
//        Optional<Book> existingBook = (bookRepository.findById(id));
//        if (existingBook.isPresent()){
//            existingBook.get().setTitle(book.getTitle());
//            bookRepository.save(existingBook.get());
//            return "redirect:/books";
//        }
//       else{
//           log.info("No such book",id);
//            return "redirect:/books";
//
//        }
//    }

//    @GetMapping("/searchBook")
//    public String searchBook() {
//        return "searchBook";
//    }
//    @GetMapping
//    public String searchByKeyword(@Param("keyword") String keyword,Model model){
//        List<Book> listBook=bookRepository.listAll(keyword);
//        model.addAttribute("keyword",keyword);
//    }

//    @PostMapping("/editBook")
//    public String editBook(){
//        return "editBook";
////        return "redirect:/books";
//    }
//    @GetMapping("/updateBook")
//    public String updateBookForm(Model model) {
//        return "updateBookForm";
//    }
//
//    @PostMapping("/updateBook")
//    public String updateBook(@RequestParam Long id, Model model) {
//        Optional<Book> existingBook = bookRepository.findById(id);
//        if (existingBook.isPresent()) {
//            model.addAttribute("book", existingBook.get());
//            return "editBook";
//        } else {
//            log.info("No such book", id);
//            return "redirect:/books";
//        }
//    }
//
//    @PostMapping("/updateBook")
//    public String updateBook(@ModelAttribute Book book) {
//        bookRepository.save(book);
//        return "redirect:/books";
//    }
//@GetMapping("/updateBook")
//public String updateBookForm(Model model) {
//    return "updateBookForm";
//}

//    @PostMapping("/updateBook")
//    public String updateBook(@RequestParam Long id, Model model) {
//        Optional<Book> existingBook = bookRepository.findById(id);
//        if (existingBook.isPresent()) {
//            model.addAttribute("book", existingBook.get());
//            return "updateBook";
//        } else {
//            log.info("No such book", id);
//            return "redirect:/books";
//        }
//    }
//
//    @PostMapping("/updateBook/{id}")
//    public String updateBook(@PathVariable Long id, @ModelAttribute Book book) {
//        Book existingBook = bookRepository.findById(id).get();
//        existingBook.setTitle(book.getTitle());
//        existingBook.setIsbn(book.getIsbn());
//        existingBook.setPlacedAt(book.getPlacedAt());
//        existingBook.setFIO(book.getFIO());
//        existingBook.setReturnedAt(book.getReturnedAt());
//        bookRepository.save(existingBook);
//        return "redirect:/books";
//    }


// ...

//    @GetMapping("/bookIssuanceHistogram")
//    public String bookIssuanceHistogram(Model model) throws IOException {
//        // Get the data for the histogram
//        List<Book> books = bookRepository.findAll();
//        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        for (Book book : books) {
//            String day = book.getPlacedAt().toString().substring(0, 10); // Get the day of the week
//            int count = bookRepository.countByPlacedAt(day);
//            dataset.addValue(count, "Book Issuance", day);
//        }
//
//        // Create the chart
//        JFreeChart chart = ChartFactory.createBarChart(
//                "Book Issuance by Day", // chart title
//                "Day", // domain axis label
//                "Count", // range axis label
//                dataset, // data
//                PlotOrientation.VERTICAL, // orientation
//                true, // include legend
//                true, // tooltips
//                false // urls
//        );
//
//        // Save the chart to a file
//        try {
//            ChartUtils.saveChartAsPNG(new File("bookIssuanceHistogram.png"), chart, 800, 600);
//        } catch (IOException e) {
//            log.error("Error saving chart to file", e);
//        }
//
//        BufferedImage chartImage = chart.createBufferedImage(800, 600);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ImageIO.write(chartImage, "png", bos);
//        byte[] chartBytes = bos.toByteArray();
//
//        model.addAttribute("chartBytes", chartBytes);
//
//        return "bookIssuanceHistogram";
//
//    }
//    @PostMapping("/searchByTitle")
//    public String searchByTitle(@RequestParam("title") String title, Model model) {
//        List<Book> books = bookRepository.findByTitle(title);
//        model.addAttribute("books", books);
//        return "bookList";
//    }

//    @GetMapping("/searchByIsbn")
//    public String searchByIsbn(Model model) {
//        return "searchByIsbn";
//    }
//
//    @PostMapping("/searchByIsbn")
//    public String searchByIsbn(@RequestParam("isbn") String isbn, Model model) {
//        List<Book> books = bookRepository.findByIsbn(isbn);
//        model.addAttribute("books", books);
//        return "bookList";
//    }
//
//    @GetMapping("/searchByTitleAndIsbn")
//    public String searchByTitleAndIsbn(Model model) {
//        return "searchByTitleAndIsbn";
//    }

//    @PostMapping("/searchByTitleAndIsbn")
//    public String searchByTitleAndIsbn(@RequestParam("title") String title, @RequestParam("isbn") String isbn, Model model) {
//        List<Book> books = bookRepository.findByTitleAndIsbn(title, isbn);
//        model.addAttribute("books", books);
////        model.addAttribute("count", bookCount);
//        return "bookList";
//    }


//    @PostMapping("/searchBook")
//    public String searchesBook(@RequestParam("title") String title, Model model) {
//        List<Book> book = bookRepository.findByTitle(title);
//        Book boook=book.isEmpty() ? null : book.get(0);
//        model.addAttribute("book", boook);
//        return "editBook";
//    }

//    @GetMapping("/editBook")
//    public String editsBook() {
//        return "editBook";
//    }
//    @PostMapping("/editsBook")
//    public String editsBook(
//            @RequestParam("title") String title,
//            @RequestParam("isbn") String isbn,
//            @RequestParam("placedAt") @DateTimeFormat(pattern = "yyyy-MM-dd") Date placedAt,
//            @RequestParam("fio") String fio,
//            @RequestParam("returnedAt") @DateTimeFormat(pattern = "yyyy-MM-dd") Date returnedAt) {
//        Book existingBook = bookRepository.findByTitle(title).get(0);
//        existingBook.setTitle(title);
//        existingBook.setIsbn(isbn);
//        existingBook.setPlacedAt(placedAt);
//        existingBook.setFIO(fio);
//        existingBook.setReturnedAt(returnedAt);
//        bookRepository.save(existingBook);
//        return "redirect:/books";
//    }

//    @PutMapping("edit/{id}")
//    public String editBook(@PathVariable Long id){
//        Optional<Book> book = bookRepository.findById(id);
//        return "editBook";
//
//    }



}




package TestCaseDroid.test.MultilevelCall;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private List<Book> books;

    public Library() {
        this.books = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public List<Book> searchByTitle(String title) {
        List<Book> foundBooks = new ArrayList<>();
        Book book = new Book("Java Fundamentals", "John Doe");
        books.add(book);
        book.getTitle();

        for (Book book1 : books) {
            if (book1.getTitle().equalsIgnoreCase(title)) {
                foundBooks.add(book1);
            }
        }
        return foundBooks;
    }
}

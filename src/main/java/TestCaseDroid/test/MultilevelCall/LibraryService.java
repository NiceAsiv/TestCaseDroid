package TestCaseDroid.test.MultilevelCall;

import java.util.List;

public class LibraryService {
    private Library library;

    public LibraryService(Library library) {
        this.library = library;
    }

    public void addBookToLibrary(Book book) {
        library.addBook(book);
    }

    public void displayBooksByTitle(String title) {
        List<Book> books = library.searchByTitle(title);
        for (Book book : books) {
            System.out.println(book);
        }
    }
}

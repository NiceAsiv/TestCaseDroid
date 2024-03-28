package TestCaseDroid.test.MultilevelCall;

public class LibraryApplication {
    public static void main(String[] args) {
        Library library = new Library();
        LibraryService libraryService = new LibraryService(library);

        Book book1 = new Book("Java Fundamentals", "John Doe");
        Book book2 = new Book("Advanced Java", "Jane Doe");

        libraryService.addBookToLibrary(book1);
        libraryService.addBookToLibrary(book2);

        System.out.println("Searching for 'Java Fundamentals':");
        libraryService.displayBooksByTitle("Java Fundamentals");
    }
}

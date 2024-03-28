package TestCaseDroid.test.MultilevelCall;

public class Book {
    private String title;
    private String author;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        vulnerable();
        return title;
    }

    public String getAuthor() {
        return author;
    }


    public void vulnerable(){
        //vulnerable code
        System.out.println("vulnerable");
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}

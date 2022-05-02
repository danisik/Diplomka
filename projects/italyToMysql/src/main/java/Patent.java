import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Patent {

    private long idTable;
    private String id;
    private Date date;
    private String title;
    private List<String> author;
    private String kind;
    private String country;

    private List<String> sections;
    private List<String> classes;
    private List<String> subclasses;

    public Patent() {

        setId("");
        setDate(null);
        setTitle("");
        setKind("");
        setCountry("IT");

        author = new ArrayList<>();
        sections = new ArrayList<>();
        classes =  new ArrayList<>();
        subclasses= new ArrayList<>();
    }

    public void addClass(String sClass) {

        classes.add(sClass);
    }

    public void addSection(String section) {

        sections.add(section);
    }

    public void addSubclass(String subclass) {

        subclasses.add(subclass);
    }

    public List<String> getSections() {

        return sections;
    }

    public List<String> getClasses() {

        return classes;
    }

    public List<String> getSubclasses() {

        return subclasses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthor() {
        return author;
    }

    public void addAuthor(String author) {
        this.author.add(author);
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getIdTable() {
        return idTable;
    }

    public void setIdTable(long idTable) {
        this.idTable = idTable;
    }
}

import java.util.ArrayList;
import java.util.List;

public class ItalyPatent {

    private String country;
    private String patentId;
    private String kind;
    private String date;
    private String language;
    private String title;
    private String inventor;
    private Long id;

    private List<String> sections;
    private List<String> sClasses;
    private List<String> subclasses;

    public ItalyPatent() {

         country = "";
         patentId = "";
         kind = "";
         date = "";
         language = "";
         title = "";
        inventor = "";
        id = 0L;

        sections = new ArrayList<>();
        sClasses = new ArrayList<>();
        subclasses = new ArrayList<>();
    }

    public void addSection(String section) {

        sections.add(section);
    }

    public void addSclass(String sClass) {

        sClasses.add(sClass);
    }

    public void addSubclass(String subclass) {

        subclasses.add(subclass);
    }

    public List<String> getSections() {

        return sections;
    }
    public List<String> getsClasses() {

        return sClasses;
    }
    public List<String> getSubclasses() {

        return subclasses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventor() {
        return inventor;
    }

    public void setInventor(String inventor) {

        if (inventor.length() > 100) inventor = inventor.substring(0, 100);
        this.inventor = inventor;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPatentId() {
        return patentId;
    }

    public void setPatentId(String patentId) {
        this.patentId = patentId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {

        if (title.length() > 300) title = title.substring(0, 300);
        this.title = title;
    }
}

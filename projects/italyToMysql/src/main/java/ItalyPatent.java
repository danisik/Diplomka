import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ItalyPatent {

    private long id;
    private String family;
    private String country;
    private String kind;
    private String docNumber;
    private Date docDate;
    private String wide;
    private String title;

    private List<String> inventors;
    private List<IPCR> ipcrs;

    public ItalyPatent() {

        setId(0);
        setFamily("");
        setCountry("");
        setKind("");
        setDocNumber("");
        setDocDate(null);
        setWide("");
        setTitle("");

        setInventors(new ArrayList<>());
        setIpcrs(new ArrayList<>());
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public Date getDocDate() {
        return docDate;
    }

    public void setDocDate(Date docDate) {
        this.docDate = docDate;
    }

    public String getWide() {
        return wide;
    }

    public void setWide(String wide) {
        this.wide = wide;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getInventors() {
        return inventors;
    }

    public void setInventors(List<String> inventors) {
        this.inventors = inventors;
    }

    public List<IPCR> getIpcrs() {
        return ipcrs;
    }

    public void setIpcrs(List<IPCR> ipcrs) {
        this.ipcrs = ipcrs;
    }

    public void addIpcr(IPCR ipcr) {

        ipcrs.add(ipcr);
    }

    public void addInventor(String inventor) {

        inventors.add(inventor);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String toJson() {

        String jsonString = "{\n";
        jsonString += "\t\"patent\": {\n";

        //jsonString += "\"\": ";

        jsonString += "\t\t\"family\": \"" + family + "\",\n";
        jsonString += "\t\t\"country\": \"" + country + "\",\n";
        jsonString += "\t\t\"kind\": \"" + kind + "\",\n";
        jsonString += "\t\t\"doc-number\": \"" + docNumber + "\",\n";
        jsonString += "\t\t\"date\": \"" + docDate.toString() + "\",\n";
        jsonString += "\t\t\"wide\": \"" + wide + "\",\n";
        jsonString += "\t\t\"title\": \"" + title + "\",\n";

        jsonString += "\t\t\"inventors\": {\n";

        for (int i = 0; i < inventors.size(); i++) {

            jsonString += "\t\t\t\"inventor\": {\n";
            jsonString += "\t\t\t\t\"name\": \"" + inventors.get(i) + "\"\n";
            jsonString += "\t\t\t}";
            if (i < inventors.size() - 1) jsonString += ",";
            jsonString += "\n";
        }

        jsonString += "\t\t},\n";

        jsonString += "\t\t\"classifications\": {\n";

        for (int i = 0; i < ipcrs.size(); i++) {

            IPCR ipcr = ipcrs.get(i);
            jsonString += "\t\t\t\"classification\": {\n";
            jsonString += "\t\t\t\t\"section\": \"" + ipcr.getSection() + "\",\n";
            jsonString += "\t\t\t\t\"class\": \"" + ipcr.getsClass() + "\",\n";
            jsonString += "\t\t\t\t\"subclass\": \"" + ipcr.getSubclass() + "\",\n";
            jsonString += "\t\t\t\t\"group\": \"" + ipcr.getGroup() + "\",\n";
            jsonString += "\t\t\t\t\"subgroup\": \"" + ipcr.getSubgroup() + "\"\n";
            jsonString += "\t\t\t}";
            if (i < inventors.size() - 1) jsonString += ",";
            jsonString += "\n";
        }

        jsonString += "\t\t}\n";


        jsonString += "\t}\n}";
        return jsonString;
    }
}

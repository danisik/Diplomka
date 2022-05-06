import java.util.ArrayList;
import java.util.List;

public class ItalyPatent {

    private String family;
    private String country;
    private String kind;
    private String docNumber;
    private String docDate;
    private String wide;
    private String title;

    private List<String> inventors;
    private List<IPCR> ipcrs;

    public ItalyPatent() {

        setFamily("");
        setCountry("");
        setKind("");
        setDocNumber("");
        setDocDate("");
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

    public String getDocDate() {
        return docDate;
    }

    public void setDocDate(String docDate) {
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
}

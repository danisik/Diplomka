import com.mongodb.*;
import com.mongodb.util.JSON;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.lang.model.util.Elements;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.exit;

public class mainclass {

    static final String inputFolder = "D:\\PATENTY\\2.Data\\";
    static final String outputFolder = "D:\\PATENTY\\3.DataJSON\\";

    public static Connection conn = null;

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        createConnection();

        System.out.println("Anglie");
        // HOTOVO
        //convertAnglie();

        System.out.println("\n\nIsrael");
        // HOTOVO
        //convertIsrael();

        System.out.println("\n\nLitva");
        // HOTOVO
        //convertLitva();

        System.out.println("\n\nPeru");
        // HOTOVO
        //convertPeru();

        System.out.println("\n\nPortugalsko");
        // HOTOVO
        //convertPortugal();

        System.out.println("\n\nItálie");
        // TODO: italii až nakonec
        //convertItaly();

        System.out.println("\n\nŠpanělsko");
        // HOTOVO
        //convertSpanelsko();

        System.out.println("\n\nKanada");
        // HOTOVO
        //convertCanada();

        System.out.println("\n\nFrancie");
        convertFrancie();

        System.out.println("\n\nRusko");
        // HOTOVO
        //convertRusko();

        closeConnection();
    }

    private static void removeDuplicates() {

    }

    private static void convertItaly() throws FileNotFoundException {

        PreparedStatement preparedStmt = null;
        List<ItalyPatent> patents = new ArrayList<>();

        try {

            // patent_id ; title ; date ; kind ; country; language    -  patents
            // id_patent ; section ; class ; subclass    - classification
            // id_patent ; inventor     - inventors

            // Get patents
            preparedStmt = conn.prepareStatement("select * from sapebinich_patent");
            ResultSet set = preparedStmt.executeQuery();

            while (set.next()) {

                ItalyPatent patent = new ItalyPatent();
                // Patents
                patent.setId(set.getLong(1));
                patent.setCountry(set.getString(5));
                patent.setPatentId(patent.getCountry() + set.getString(7));
                patent.setKind(set.getString(8).substring(0, 1));
                patent.setDate(set.getString(9));
                patent.setLanguage("-");

                // Inventors
                patent.setInventor(set.getString(10));

                patents.add(patent);
            }

            System.out.println("patents created");

            for (ItalyPatent patent : patents) {

                // Get patent title
                preparedStmt = conn.prepareStatement("select title from sapebinich_patent_title where id_patent = ?");
                preparedStmt.setLong(1, patent.getId());

                set = preparedStmt.executeQuery();

                if(set.next()) {

                    patent.setTitle(set.getString(1));
                }

                // Get classifications ids
                List<Long> ipcrs = new ArrayList<>();
                preparedStmt = conn.prepareStatement("select id_ipcr from sapebinich_patent_ipcr where id_patent = ?");
                preparedStmt.setLong(1, patent.getId());

                set = preparedStmt.executeQuery();

                while(set.next()) {

                    ipcrs.add(set.getLong(1));
                }

                // Get classifications
                for (Long ipcr : ipcrs) {

                    preparedStmt = conn.prepareStatement("select * from sapebinich_ipcr where id_ipcr = ?");
                    preparedStmt.setLong(1, ipcr);

                    set = preparedStmt.executeQuery();

                    while(set.next()) {

                        patent.addSection(set.getString(2));
                        patent.addSclass(set.getString(3));
                        patent.addSubclass(set.getString(4));
                    }
                }
            }

            // patents filled.

            for (ItalyPatent patent : patents) {

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    java.util.Date utilDate = format.parse(patent.getDate());
                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                    // Insert patent
                    preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                    preparedStmt.setString(1, patent.getPatentId());
                    preparedStmt.setString(2, patent.getTitle());
                    preparedStmt.setDate(3, sqlDate);
                    preparedStmt.setString(4, patent.getKind());
                    preparedStmt.setString(5, patent.getCountry());
                    preparedStmt.setString(6, patent.getLanguage());
                    preparedStmt.execute();

                    Long patentId = 0L;
                    preparedStmt = conn.prepareStatement("select last_insert_id()");
                    ResultSet rSet = preparedStmt.executeQuery();

                    while (rSet.next()) {

                        patentId = rSet.getLong(1);
                    }

                    // Insert classification
                    for (int i = 0; i < patent.getSections().size(); i++) {

                        preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                        preparedStmt.setLong(1, patentId);
                        preparedStmt.setString(2, patent.getSections().get(i));
                        preparedStmt.setString(3, patent.getsClasses().get(i));
                        preparedStmt.setString(4, patent.getSubclasses().get(i));
                        preparedStmt.execute();
                    }

                    // Insert author
                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                    preparedStmt.setLong(1, patentId);
                    preparedStmt.setString(2, patent.getInventor());
                    preparedStmt.execute();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void convertPortugal() throws FileNotFoundException {
        String name = "";
        try {

            // Instantiate the Factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Disable external DTDs as well
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse("D:\\PATENTY\\2.Data\\Portugalsko\\Portugal_Patentes.xml");
            doc.getDocumentElement().normalize();

            PreparedStatement preparedStmt = null;

            List<String> patentIDs = new ArrayList<>();

            NodeList entries = doc.getElementsByTagName("entry");
            for (int i = 0; i < entries.getLength(); i++) {

                Element entry = (Element) entries.item(i);
                Element content = (Element) entry.getElementsByTagName("content").item(0);

                Element properties = (Element) content.getElementsByTagName("m:properties").item(0);

                // Patents
                String country = "PT";
                String sPatentId = country + properties.getElementsByTagName("d:numero").item(0).getTextContent();
                String title = properties.getElementsByTagName("d:titulo").item(0).getTextContent();
                String date = properties.getElementsByTagName("d:data").item(0).getTextContent();
                String kind = "-";
                String language = "PT";

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                java.util.Date utilDate = format.parse(date);
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                if (patentIDs.contains(sPatentId)) continue;
                patentIDs.add(sPatentId);

                preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                preparedStmt.setString(1, sPatentId);
                preparedStmt.setString(2, title);
                preparedStmt.setDate(3, sqlDate);
                preparedStmt.setString(4, kind);
                preparedStmt.setString(5, country);
                preparedStmt.setString(6, language);
                preparedStmt.execute();

                Long patentId = 0L;
                preparedStmt = conn.prepareStatement("select last_insert_id()");
                ResultSet rSet = preparedStmt.executeQuery();

                while (rSet.next()) {

                    patentId = rSet.getLong(1);
                }

                // Classification
                String classification = properties.getElementsByTagName("d:classificacaoipc").item(0).getTextContent();
                classification = classification.replaceAll("\\[", "").replaceAll("\\]", "");
                String[] array = classification.split(",");

                List<String> list = new ArrayList<>();

                for (int j = 0; j < array.length; j++) {

                    String singleClas = array[j];

                    while (singleClas.charAt(0) == ' ') singleClas = singleClas.substring(1);

                    String section = singleClas.substring(0, 1);
                    String sClass = singleClas.substring(1, 3);
                    String subclass = singleClas.substring(3, 4);
                    String full = section + sClass + subclass;

                    if (list.contains(full)) continue;
                    list.add(full);

                    preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                    preparedStmt.setLong(1, patentId);
                    preparedStmt.setString(2, section);
                    preparedStmt.setString(3, sClass);
                    preparedStmt.setString(4, subclass);
                    preparedStmt.execute();
                }

                // Inventor
                String inventors = properties.getElementsByTagName("d:requerentes").item(0).getTextContent();
                inventors = inventors.replaceAll("\\[", "").replaceAll("\\]", "");
                String[] arrayInventors = inventors.split(",");

                for (int j = 0; j < arrayInventors.length; j++) {

                    String inventor = arrayInventors[j];

                    while (inventor.charAt(0) == ' ') inventor = inventor.substring(1);

                    if (j + 1 < arrayInventors.length) {

                        while (arrayInventors[j+1].charAt(0) == ' ') arrayInventors[j+1] = arrayInventors[j+1].substring(1);
                        if (arrayInventors[j+1].equals("S.A.") ||
                                arrayInventors[j+1].equals("LDA.") ||
                                arrayInventors[j+1].equals("S.L.") ||
                                arrayInventors[j+1].equals("S.L.U.") ||
                                arrayInventors[j+1].equals("LDª.") ||
                                arrayInventors[j+1].equals("SA") ||
                                arrayInventors[j+1].equals("S.R.L.")
                        ) {

                            inventor += ", " + arrayInventors[j+1];
                            j++;
                        }
                    }

                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                    preparedStmt.setLong(1, patentId);
                    preparedStmt.setString(2, inventor);
                    preparedStmt.execute();
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void convertRusko() throws FileNotFoundException {

        long start = System.currentTimeMillis();

        File dir = new File(inputFolder + "Rusko");

        long duplicates = 0;
        HashSet<String> set2000 = new HashSet<String>();
        HashSet<String> set = new HashSet<String>();
        long test = 0;

        File[] csvs = dir.listFiles();
        List<String[]> r = null;
        CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build(); // custom separator

        int te = 1;
        int testt = 0;


        CSVReader reader = null;

        for (File csv : csvs) {

            System.out.println(te + " " + csv.getName());
            te++;

            List<String> headers = new ArrayList<>();
            boolean headersParsed = false;

            String[] s = null;

            try {

                FileInputStream is = new FileInputStream(csv.getAbsolutePath());
                InputStreamReader isr = new InputStreamReader(is, "windows-1251");
                BufferedReader buffReader = new BufferedReader(isr);

                reader = new CSVReaderBuilder(buffReader)
                        .withCSVParser(csvParser)   // custom CSV parser
                        //.withSkipLines(1)           // skip the first line, header info
                        .build();
                //r = reader.readAll();



                while (true) {

                    s = reader.readNext();

                    if (s == null) break;

                    if (s.length <= 1) {

                        continue;
                    }

                    if (!headersParsed) {

                        for (String header : s) {

                            headers.add(header);
                        }

                        headersParsed = true;
                        continue;
                    }

                    String id = s[0];
                    String date = s[1];
                    String author = s[4];
                    String title = s[10];

                    if (set.contains(id) || set2000.contains(id)) {

                        duplicates++;
                    } else {

                        if (date.startsWith("20")) {

                            if (author.equals("") || title.equals("")) {
                                testt++;
                            }
                            else {
                                set.add(id);

                                try {

                                    PreparedStatement preparedStmt = null;
                                    // TODO:

                                    // patent_id ; title ; date ; kind ; country; language    -  patents
                                    // id_patent ; inventor     - inventors

                                    // Patents
                                    String country = "RU";
                                    id = "RU" + s[0];
                                    title = s[10];
                                    date = s[1];
                                    String kind = "-";
                                    String language = "RU";

                                    if (title.length() > 300) title = title.substring(0, 300);

                                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                    java.util.Date utilDate = format.parse(date);
                                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                    // Patents
                                    preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                    preparedStmt.setString(1, id);
                                    preparedStmt.setString(2, title);
                                    preparedStmt.setDate(3, sqlDate);
                                    preparedStmt.setString(4, kind);
                                    preparedStmt.setString(5, country);
                                    preparedStmt.setString(6, language);
                                    preparedStmt.execute();

                                    Long patentId = 0L;
                                    preparedStmt = conn.prepareStatement("select last_insert_id()");
                                    ResultSet rSet = preparedStmt.executeQuery();

                                    while (rSet.next()) {

                                        patentId = rSet.getLong(1);
                                    }

                                    // Inventors
                                    author = s[4];

                                    if (author.length() > 100) author = author.substring(0, 100);

                                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                    preparedStmt.setLong(1, patentId);
                                    preparedStmt.setString(2, author);
                                    preparedStmt.execute();
                                }catch (SQLException e) {

                                    if (e.getMessage().contains("Duplicate entry")) {


                                    } else {
                                        e.printStackTrace();
                                        exit(1);
                                    }
                                }
                                catch (Exception e) {

                                    e.printStackTrace();
                                    exit(1);
                                }
                            }
                        }
                        else set2000.add(id);
                    }
                }

                reader.close();
                reader = null;


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = (finish - start) / 1000;

        System.out.println("Rusko");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPatenty před 2000: " + set2000.size());
        System.out.println("\tDuplikáty: " + duplicates);
        System.out.println("\tBez autora/názvu: " + testt);
        System.out.println("\tČas: " + timeElapsed + " s");
        System.out.println("\n");
    }

    public static void convertPeru() {

        try
        {
            File file = new File("D:\\PATENTY\\2.Data\\Peru\\PERU-Solicitudes en dominio público al 31 de julio 2019.xlsx");   //creating a new file instance
            FileInputStream fis = new FileInputStream(file);   //obtaining bytes from the file
//creating Workbook instance that refers to .xlsx file
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);     //creating a Sheet object to retrieve object
            Iterator<Row> itr = sheet.iterator();    //iterating over excel file

            List<String> columns = new ArrayList<>();
            Map<Long, List<String>> values = new HashMap();

            List<String> patentIdss = new ArrayList<>();

            boolean skipFirstRow = false;
            boolean secondRow = false;

            long index = 0;

            while (itr.hasNext())
            {
                Row row = itr.next();

                if (!skipFirstRow) {

                    skipFirstRow = true;
                    continue;
                }

                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column

                boolean skipFirstColumn = false;
                int currentColumn = 1;
                List<String> vals = new ArrayList<>();

                while (cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();

                    if (!skipFirstColumn) {
                        skipFirstColumn = true;
                        continue;
                    }

                    CellType type = cell.getCellType();
                    String value = "";

                    switch (type)
                    {
                        case STRING:    //field that represents string cell type
                            value = cell.getStringCellValue();
                            break;
                        case NUMERIC:    //field that represents number cell type
                            Date date = cell.getDateCellValue();
                            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                            value = dateFormat.format(date);
                            break;
                        default:
                    }

                    if (!secondRow) {
                        columns.add(value);
                    }
                    else {

                        currentColumn++;

                        if (currentColumn <= columns.size()) {

                            vals.add(value);

                            if (currentColumn == 3) {

                                String val = value.substring(value.lastIndexOf("-") + 1);
                                if (!val.startsWith("20")) {
                                    break;
                                }
                            }
                        }
                        else {
                            value = value.replaceAll("\"", "\\\"");
                            vals.add(value);
                            values.put(index, vals);
                            index++;
                            break;
                        }
                    }
                }

                if (!secondRow) {

                    secondRow = true;
                }
            }

            // patent_id ; title ; date ; kind ; country    -  patents
            // id_patent ; section ; class ; subclass    - classification
            // id_patent ; language     - languages
            // id_patent ; inventor     - inventors
            // id_patent ; applicant    - applicants

            PreparedStatement preparedStmt = null;
            for (Long key : values.keySet()) {

                // Patents
                String sPatentId = "PE";
                String title = "";
                String date = "";
                String kind = "-";
                String country = "PE";

                // Classification
                List<String> section = new ArrayList<>();
                List<String> sClass = new ArrayList<>();
                List<String> subclass = new ArrayList<>();

                // Inventors
                List<String> iNames = new ArrayList<>();

                List<String> listValues = values.get(key);

                for (int k = 0; k < listValues.size(); k++) {

                    String columnName = columns.get(k);

                    if (columnName.equals("")) {

                    }

                    // Patents
                    if (columnName.equals("N° EXPEDIENTE")) {

                        String expediente = listValues.get(k);
                        expediente = expediente.split("-")[0];

                        int ind = 0;
                        for (int l = 0; l < expediente.length(); l++) {

                            if (expediente.charAt(l) == '0') continue;
                            else ind = l;
                            break;
                        }

                        sPatentId += expediente.substring(ind);
                    }

                    if (columnName.equals("TITULAR")) {

                        title = listValues.get(k);
                        if (title.length() > 300) title = title.substring(0, 300);
                    }

                    if (columnName.equals("FECHA DE INICIO")) {

                        date = listValues.get(k);
                    }

                    // Classification
                    if (columnName.equals("CLASIFICACIÓN")) {

                        String classString = listValues.get(k);
                        String[] classifications = classString.split(";");

                        List<String> listClass = new ArrayList<>();

                        for (int l = 0; l < classifications.length; l++) {

                            String classification = classifications[l];
                            if (classification.contains("-") || classification.length() < 1) continue;

                            if (classification.charAt(0) == ' ') classification = classification.substring(1);

                            String full = classification.substring(0, 1) + classification.substring(1, 3) + classification.substring(3, 4);

                            if (listClass.contains(full)) continue;;
                            listClass.add(full);

                            section.add(classification.substring(0, 1));
                            sClass.add(classification.substring(1, 3));
                            subclass.add(classification.substring(3, 4));
                        }
                    }

                    // Inventors
                    if (columnName.equals("SOLICITANTE")) {

                        String[] names = listValues.get(k).split(";");

                        for (int l = 0; l < names.length; l++) {

                            String iNamee = names[l];
                            if (iNamee.charAt(0) == ' ') iNamee = iNamee.substring(1);
                            if (iNamee.length() < 2) continue;
                            iNames.add(iNamee);
                        }

                    }
                }

                if (patentIdss.contains(sPatentId)) continue;
                patentIdss.add(sPatentId);

                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                java.util.Date utilDate = format.parse(date);
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                // Patents
                preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                preparedStmt.setString(1, sPatentId);
                preparedStmt.setString(2, title);
                preparedStmt.setDate(3, sqlDate);
                preparedStmt.setString(4, kind);
                preparedStmt.setString(5, country);
                preparedStmt.setString(6, "ES");
                preparedStmt.execute();

                Long patentId = 0L;
                preparedStmt = conn.prepareStatement("select last_insert_id()");
                ResultSet rSet = preparedStmt.executeQuery();

                while (rSet.next()) {

                    patentId = rSet.getLong(1);
                }

                // Classifications
                for (int ll = 0; ll < section.size(); ll++) {

                    preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                    preparedStmt.setLong(1, patentId);
                    preparedStmt.setString(2, section.get(ll));
                    preparedStmt.setString(3, sClass.get(ll));
                    preparedStmt.setString(4, subclass.get(ll));
                    preparedStmt.execute();
                }

                // Inventors
                for (int ll = 0; ll < iNames.size(); ll++) {

                    String inventor = iNames.get(ll);

                    if (inventor.length() > 100) inventor = inventor.substring(0, 100);
                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                    preparedStmt.setLong(1, patentId);
                    preparedStmt.setString(2, inventor);
                    preparedStmt.execute();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void convertCanada() throws ParserConfigurationException, IOException, SAXException {

        List<String> files;

        HashSet<String> set = new HashSet<String>();
        HashSet<String> set2000 = new HashSet<String>();

        File israelDir = new File(inputFolder + "Kanada");
        File[] years = israelDir.listFiles();

        List<Exception> exceptions = new ArrayList<>();

        for (int monn = years.length - 1; monn >= 0; monn-- ) {

            File year = years[monn];
            File[] folders = year.listFiles();
            System.out.println(year.getAbsolutePath());

            for (File month : folders) {

                File[] filess = month.listFiles();


                for (File file : filess) {

                    PreparedStatement preparedStmt = null;

                    // Old structure
                    if (file.isFile()) {


                        // CA-BFT-2064388-20190113.xml
                        String filename = file.getName();
                        filename = filename.replace("CA-BFT-", "");
                        filename = filename.substring(0, filename.indexOf("-"));

                        if (set.contains(filename) || set2000.contains(filename)) {

                        } else {

                            // Instantiate the Factory
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                            // optional, but recommended
                            // process XML securely, avoid attacks like XML External Entities (XXE)
                            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                            // Disable external DTDs as well
                            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                            // parse XML file
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(file);
                            doc.getDocumentElement().normalize();

                            Node documentId = doc.getElementsByTagName("document-id").item(0);
                            Element el = (Element) documentId;

                            String dateString = "";

                            try {

                                dateString = el.getElementsByTagName("date").item(0).getTextContent();
                            } catch (Exception e) {

                            }

                            if (!dateString.startsWith("20") && !dateString.equals("")) {
                                set2000.add(filename);
                                continue;
                            }

                            set.add(filename);

                            String newXmlContentString = "";

                            try {

                                // patent_id ; title ; date ; kind ; country    -  patents
                                // id_patent ; section ; class ; subclass    - classification
                                // id_patent ; language     - languages
                                // id_patent ; inventor     - inventors
                                // id_patent ; applicant    - applicants

                                Element elBiblio = (Element) doc.getElementsByTagName("ca-bibliographic-data").item(0);

                                if (elBiblio == null) continue;

                                // Patents
                                String language = "";

                                if (elBiblio.getElementsByTagName("language-of-filing") == null || elBiblio.getElementsByTagName("language-of-filing").item(0) == null) language = "-";
                                else language = elBiblio.getElementsByTagName("language-of-filing").item(0).getTextContent().toUpperCase();

                                String country = "";

                                if (el.getElementsByTagName("country") == null || el.getElementsByTagName("country").item(0) == null) country = "-";
                                else country = el.getElementsByTagName("country").item(0).getTextContent();

                                String sPatentId = "";

                                if (!country.equals("-")) sPatentId = country;

                                if (el.getElementsByTagName("doc-number") == null || el.getElementsByTagName("doc-number").item(0) == null) continue;
                                else sPatentId += el.getElementsByTagName("doc-number").item(0).getTextContent();

                                String title = "";
                                String date = "";

                                if (el.getElementsByTagName("date") == null || el.getElementsByTagName("date").item(0) == null) continue;
                                else date = el.getElementsByTagName("date").item(0).getTextContent();

                                String kind = "";

                                if (el.getElementsByTagName("kind") == null || el.getElementsByTagName("kind").item(0) == null) continue;
                                else kind = el.getElementsByTagName("kind").item(0).getTextContent().substring(0, 1);

                                NodeList titles = elBiblio.getElementsByTagName("invention-title");

                                for (int l = 0; l < titles.getLength(); l++) {

                                    Element elTitle = (Element) titles.item(l);

                                    if (elTitle.getAttribute("lang") == null) continue;

                                    if (elTitle.getAttribute("lang").equalsIgnoreCase(language)){

                                        title = elTitle.getTextContent();
                                        break;
                                    }
                                }
                                if (title.length() > 300) title = title.substring(0, 300);

                                if (title.length() == 0) continue;

                                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                java.util.Date utilDate = format.parse(date);
                                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                preparedStmt.setString(1, sPatentId);
                                preparedStmt.setString(2, title);
                                preparedStmt.setDate(3, sqlDate);
                                preparedStmt.setString(4, kind);
                                preparedStmt.setString(5, country);
                                preparedStmt.setString(6, language);
                                preparedStmt.execute();

                                Long patentId = 0L;
                                preparedStmt = conn.prepareStatement("select last_insert_id()");
                                ResultSet rSet = preparedStmt.executeQuery();

                                while (rSet.next()) {

                                    patentId = rSet.getLong(1);
                                }

                                // Classification
                                Element classIpcr = (Element) elBiblio.getElementsByTagName("classifications-ipcr").item(0);

                                if (classIpcr != null) {
                                    NodeList classIpcrs = classIpcr.getElementsByTagName("classification-ipcr");

                                    List<String> listClass = new ArrayList<>();

                                    for (int l = 0; l < classIpcrs.getLength(); l++) {

                                        Element cIpcr = (Element) classIpcrs.item(l);

                                        if (cIpcr.getElementsByTagName("section") == null || cIpcr.getElementsByTagName("class") == null || cIpcr.getElementsByTagName("subclass") == null)
                                            continue;
                                        if (cIpcr.getElementsByTagName("section").item(0) == null || cIpcr.getElementsByTagName("class").item(0) == null || cIpcr.getElementsByTagName("subclass").item(0) == null)
                                            continue;

                                        String section = cIpcr.getElementsByTagName("section").item(0).getTextContent();
                                        String sClass = cIpcr.getElementsByTagName("class").item(0).getTextContent();
                                        String subclass = cIpcr.getElementsByTagName("subclass").item(0).getTextContent();
                                        String full = section + sClass + subclass;

                                        if (listClass.contains(full)) continue;
                                        listClass.add(full);

                                        preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                        preparedStmt.setLong(1, patentId);
                                        preparedStmt.setString(2, section);
                                        preparedStmt.setString(3, sClass);
                                        preparedStmt.setString(4, subclass);
                                        preparedStmt.execute();
                                    }
                                }



                                // Inventors
                                Element elOffice = (Element) elBiblio.getElementsByTagName("ca-office-specific-bib-data").item(0);

                                if (elOffice != null) {
                                    Element parties = (Element) elOffice.getElementsByTagName("ca-parties").item(0);

                                    if (parties != null) {

                                        Element elInventors = (Element) parties.getElementsByTagName("inventors").item(0);

                                        if (elInventors != null) {
                                            NodeList inventors = elInventors.getElementsByTagName("inventor");

                                            for (int l = 0; l < inventors.getLength(); l++) {

                                                Element inventor = (Element) inventors.item(l);

                                                if (inventor.getElementsByTagName("addressbook") == null || inventor.getElementsByTagName("addressbook").item(0) == null) continue;

                                                Element addressbook = (Element) inventor.getElementsByTagName("addressbook").item(0);

                                                if (addressbook.getElementsByTagName("name").item(0) == null || addressbook.getElementsByTagName("name") == null) continue;

                                                String name = addressbook.getElementsByTagName("name").item(0).getTextContent();

                                                if (name.length() > 100) name = name.substring(0, 100);

                                                preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                preparedStmt.setLong(1, patentId);
                                                preparedStmt.setString(2, name);
                                                preparedStmt.execute();
                                            }
                                        }

                                        // Applicants
                                        Element elApplicants = (Element) parties.getElementsByTagName("applicants").item(0);

                                        if (elApplicants != null) {
                                            NodeList applicants = elApplicants.getElementsByTagName("applicant");

                                            for (int l = 0; l < applicants.getLength(); l++) {

                                                Element applicant = (Element) applicants.item(l);

                                                if (applicant.getElementsByTagName("addressbook") == null || applicant.getElementsByTagName("addressbook").item(0) == null)
                                                    continue;

                                                Element addressbook = (Element) applicant.getElementsByTagName("addressbook").item(0);

                                                if (addressbook.getElementsByTagName("name").item(0) == null || addressbook.getElementsByTagName("name") == null)
                                                    continue;

                                                String name = addressbook.getElementsByTagName("name").item(0).getTextContent();
                                                if (name.length() > 100) name = name.substring(0, 100);

                                                preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                                preparedStmt.setLong(1, patentId);
                                                preparedStmt.setString(2, name);
                                                preparedStmt.execute();
                                            }
                                        }
                                    }
                                }

                            } catch (SQLException e) {

                                if (e.getMessage().contains("Duplicate entry")) {


                                } else {
                                    e.printStackTrace();
                                    exit(1);
                                }
                            }
                            catch (Exception e) {


                                e.printStackTrace();
                                exit(1);
                            }
                        }
                    }
                    // New structure
                    else {

                        File[] xmls = file.listFiles();

                        for (File xml : xmls) {

                            // CA02611743-20200108.xml
                            String filename = xml.getName();
                            filename = filename.replace("CA", "");
                            filename = filename.substring(0, filename.indexOf("-"));

                            if (set.contains(filename) || set2000.contains(filename)) {


                            } else {

                                // Instantiate the Factory
                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                                // optional, but recommended
                                // process XML securely, avoid attacks like XML External Entities (XXE)
                                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                                // Disable external DTDs as well
                                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                                // parse XML file
                                DocumentBuilder db = dbf.newDocumentBuilder();
                                Document doc = db.parse(xml);
                                doc.getDocumentElement().normalize();

                                Node documentId = doc.getElementsByTagName("document-id").item(0);
                                Element el = (Element) documentId;
                                String dateString = "";

                                try {

                                    dateString = el.getElementsByTagName("date").item(0).getTextContent();
                                } catch (Exception e) {

                                }

                                if (!dateString.startsWith("20") && !dateString.equals("")) {
                                    set2000.add(filename);
                                    continue;
                                }

                                set.add(filename);

                                String newXmlContentString = "";

                                try {

                                    Element elBiblio = (Element) doc.getElementsByTagName("ca-bibliographic-data").item(0);

                                    if (elBiblio == null) continue;

                                    // Patents
                                    String language = "";

                                    if (elBiblio.getElementsByTagName("language-of-filing") == null || elBiblio.getElementsByTagName("language-of-filing").item(0) == null) language = "-";
                                    else language = elBiblio.getElementsByTagName("language-of-filing").item(0).getTextContent().toUpperCase();

                                    String country = "";

                                    if (el.getElementsByTagName("country") == null || el.getElementsByTagName("country").item(0) == null) country = "-";
                                    else country = el.getElementsByTagName("country").item(0).getTextContent();

                                    String sPatentId = "";

                                    if (!country.equals("-")) sPatentId = country;

                                    if (el.getElementsByTagName("doc-number") == null || el.getElementsByTagName("doc-number").item(0) == null) continue;
                                    else sPatentId += el.getElementsByTagName("doc-number").item(0).getTextContent();

                                    String title = "";
                                    String date = "";

                                    if (el.getElementsByTagName("date") == null || el.getElementsByTagName("date").item(0) == null) continue;
                                    else date = el.getElementsByTagName("date").item(0).getTextContent();

                                    String kind = "";

                                    if (el.getElementsByTagName("kind") == null || el.getElementsByTagName("kind").item(0) == null) continue;
                                    else kind = el.getElementsByTagName("kind").item(0).getTextContent().substring(0, 1);

                                    NodeList titles = elBiblio.getElementsByTagName("invention-title");

                                    for (int l = 0; l < titles.getLength(); l++) {

                                        Element elTitle = (Element) titles.item(l);

                                        if (elTitle.getAttribute("lang") == null) continue;

                                        if (elTitle.getAttribute("lang").equalsIgnoreCase(language)){

                                            title = elTitle.getTextContent();
                                            break;
                                        }
                                    }
                                    if (title.length() > 300) title = title.substring(0, 300);

                                    if (title.length() == 0) continue;

                                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                    java.util.Date utilDate = format.parse(date);
                                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                    preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                    preparedStmt.setString(1, sPatentId);
                                    preparedStmt.setString(2, title);
                                    preparedStmt.setDate(3, sqlDate);
                                    preparedStmt.setString(4, kind);
                                    preparedStmt.setString(5, country);
                                    preparedStmt.setString(6, language);
                                    preparedStmt.execute();

                                    Long patentId = 0L;
                                    preparedStmt = conn.prepareStatement("select last_insert_id()");
                                    ResultSet rSet = preparedStmt.executeQuery();

                                    while (rSet.next()) {

                                        patentId = rSet.getLong(1);
                                    }

                                    // Classification
                                    Element classIpcr = (Element) elBiblio.getElementsByTagName("classifications-ipcr").item(0);

                                    if (classIpcr != null) {
                                        NodeList classIpcrs = classIpcr.getElementsByTagName("classification-ipcr");

                                        List<String> listClass = new ArrayList<>();

                                        for (int l = 0; l < classIpcrs.getLength(); l++) {

                                            Element cIpcr = (Element) classIpcrs.item(l);

                                            if (cIpcr.getElementsByTagName("section") == null ||cIpcr.getElementsByTagName("class") == null || cIpcr.getElementsByTagName("subclass") == null) continue;
                                            if (cIpcr.getElementsByTagName("section").item(0) == null ||cIpcr.getElementsByTagName("class").item(0) == null || cIpcr.getElementsByTagName("subclass").item(0) == null) continue;

                                            String section = cIpcr.getElementsByTagName("section").item(0).getTextContent();
                                            String sClass = cIpcr.getElementsByTagName("class").item(0).getTextContent();
                                            String subclass = cIpcr.getElementsByTagName("subclass").item(0).getTextContent();
                                            String full = section + sClass + subclass;

                                            if (listClass.contains(full)) continue;
                                            listClass.add(full);

                                            preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                            preparedStmt.setLong(1, patentId);
                                            preparedStmt.setString(2, section);
                                            preparedStmt.setString(3, sClass);
                                            preparedStmt.setString(4, subclass);
                                            preparedStmt.execute();
                                        }
                                    }

                                    // Inventors
                                    Element elOffice = (Element) elBiblio.getElementsByTagName("ca-office-specific-bib-data").item(0);

                                    if (elOffice != null) {

                                        Element parties = (Element) elOffice.getElementsByTagName("ca-parties").item(0);

                                        if (parties != null) {
                                            Element elInventors = (Element) parties.getElementsByTagName("inventors").item(0);

                                            if (elInventors != null) {
                                                NodeList inventors = elInventors.getElementsByTagName("inventor");

                                                for (int l = 0; l < inventors.getLength(); l++) {

                                                    Element inventor = (Element) inventors.item(l);

                                                    if (inventor.getElementsByTagName("addressbook") == null || inventor.getElementsByTagName("addressbook").item(0) == null) continue;

                                                    Element addressbook = (Element) inventor.getElementsByTagName("addressbook").item(0);

                                                    if (addressbook.getElementsByTagName("name").item(0) == null || addressbook.getElementsByTagName("name") == null) continue;

                                                    String name = addressbook.getElementsByTagName("name").item(0).getTextContent();

                                                    if (name.length() > 100) name = name.substring(0, 100);

                                                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                    preparedStmt.setLong(1, patentId);
                                                    preparedStmt.setString(2, name);
                                                    preparedStmt.execute();
                                                }
                                            }

                                            // Applicants
                                            Element elApplicants = (Element) parties.getElementsByTagName("applicants").item(0);

                                            if (elApplicants != null) {
                                                NodeList applicants = elApplicants.getElementsByTagName("applicant");

                                                for (int l = 0; l < applicants.getLength(); l++) {

                                                    Element applicant = (Element) applicants.item(l);

                                                    if (applicant.getElementsByTagName("addressbook") == null || applicant.getElementsByTagName("addressbook").item(0) == null)
                                                        continue;

                                                    Element addressbook = (Element) applicant.getElementsByTagName("addressbook").item(0);

                                                    if (addressbook.getElementsByTagName("name").item(0) == null || addressbook.getElementsByTagName("name") == null)
                                                        continue;

                                                    String name = addressbook.getElementsByTagName("name").item(0).getTextContent();
                                                    if (name.length() > 100) name = name.substring(0, 100);

                                                    preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                                    preparedStmt.setLong(1, patentId);
                                                    preparedStmt.setString(2, name);
                                                    preparedStmt.execute();
                                                }
                                            }
                                        }
                                    }

                                }
                                catch (SQLException e) {

                                    if (e.getMessage().contains("Duplicate entry")) {


                                    } else {
                                        e.printStackTrace();
                                        exit(1);
                                    }
                                }
                                catch (Exception e) {


                                    e.printStackTrace();
                                    exit(1);
                                }
                            }
                        }
                    }
                }

            }
        }

        System.out.println("Kanada");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPřed 2000: " + set2000.size());
        System.out.println("\n");

        try {
            FileWriter myWriter = new FileWriter("D:\\PATENTY\\logs.txt");

            for (Exception e : exceptions) {

                myWriter.append("0 ").append(e.toString());
                myWriter.append("1 ").append(e.getMessage());
                myWriter.append("2 ").append(e.getLocalizedMessage());
                myWriter.append("\n");
            }

            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convertFrancie() throws ParserConfigurationException, IOException, SAXException {

        List<String> files;

        HashSet<String> set = new HashSet<String>();
        HashSet<String> set2000 = new HashSet<String>();

        File israelDir = new File(inputFolder + "Francie\\patents");
        File[] years = israelDir.listFiles();

        List<Exception> exceptions = new ArrayList<>();

        for (File f : years) {

            File[] ff = f.listFiles();

            for (int monn = ff.length - 1; monn >= 0; monn-- ) {
                //for (int monn = 0; monn < years.length; monn++) {

                File year = ff[monn];
                File[] folders = year.listFiles();
                System.out.println(year.getAbsolutePath());

                for (File month : folders) {

                    File[] filess = month.listFiles();

                    if (year.getAbsolutePath().contains("bibliograficke")) System.out.println(month.getAbsolutePath());

                    for (File file : filess) {

                        File[] docFiles = file.listFiles();

                        if (month.getAbsolutePath().contains("AMD\\")) System.out.println(file.getAbsolutePath());

                        if (docFiles == null) continue;

                        if (file.getName().contains("doc")) {

                            File[] allFiles = file.listFiles();

                            for (File allFile: allFiles) {

                                File[] xmls = allFile.listFiles();

                                for (File xml: xmls) {

                                    if (xml.getName().toLowerCase(Locale.ROOT).contains("toc")) {
                                        continue;
                                    }

                                    String filename = xml.getName();

                                    if (set.contains(filename)) {

                                    } else {

                                        // Instantiate the Factory
                                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                                        // optional, but recommended
                                        // process XML securely, avoid attacks like XML External Entities (XXE)
                                        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                                        // Disable external DTDs as well
                                        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                                        try {
                                            // parse XML file
                                            DocumentBuilder db = dbf.newDocumentBuilder();
                                            Document doc = db.parse(xml);
                                            doc.getDocumentElement().normalize();

                                            Node documentId = doc.getElementsByTagName("document-id").item(0);
                                            Element el = (Element) documentId;
                                            String dateString = "";

                                            try {
                                                dateString = el.getElementsByTagName("date").item(0).getTextContent();
                                            } catch (Exception e) {

                                            }

                                            if (!dateString.startsWith("20") && !dateString.equals("")) {
                                                set2000.add(filename);
                                                continue;
                                            }

                                            set.add(filename);

                                            // 2017
                                            String newXmlContentString = "";
                                            PreparedStatement preparedStmt = null;

                                            try {

                                                // patent_id ; title ; date ; kind ; country    -  patents
                                                // id_patent ; section ; class ; subclass    - classification
                                                // id_patent ; language     - languages
                                                // id_patent ; inventor     - inventors
                                                // id_patent ; applicant    - applicants

                                                Element elBiblio = (Element) doc.getElementsByTagName("fr-bibliographic-data").item(0);

                                                if (elBiblio != null) {

                                                    // Patents
                                                    String country = "";

                                                    if (el.getElementsByTagName("country") == null || el.getElementsByTagName("country").item(0) == null) country = "FR";
                                                    else country = el.getElementsByTagName("country").item(0).getTextContent();

                                                    String title = "";
                                                    if (elBiblio.getElementsByTagName("invention-title") == null || elBiblio.getElementsByTagName("invention-title").item(0) == null) continue;
                                                    else title = elBiblio.getElementsByTagName("invention-title").item(0).getTextContent();

                                                    String sPatentId = "";
                                                    if (el.getElementsByTagName("doc-number") == null || el.getElementsByTagName("doc-number").item(0) == null) continue;
                                                    else sPatentId = country + el.getElementsByTagName("doc-number").item(0).getTextContent();

                                                    String date = "";
                                                    if (el.getElementsByTagName("date") == null || el.getElementsByTagName("date").item(0) == null) continue;
                                                    else date = el.getElementsByTagName("date").item(0).getTextContent();

                                                    String kind = "";
                                                    if (el.getElementsByTagName("kind") == null || el.getElementsByTagName("kind").item(0) == null) kind = "-";
                                                    else kind = el.getElementsByTagName("kind").item(0).getTextContent().substring(0, 1);

                                                    String language = "FR";

                                                    if (title == null || title.equals("")) continue;

                                                    if (title.length() > 300) title = title.substring(0, 300);

                                                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                                    java.util.Date utilDate = format.parse(date);
                                                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                                    preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                                    preparedStmt.setString(1, sPatentId);
                                                    preparedStmt.setString(2, title);
                                                    preparedStmt.setDate(3, sqlDate);
                                                    preparedStmt.setString(4, kind);
                                                    preparedStmt.setString(5, country);
                                                    preparedStmt.setString(6, language);
                                                    preparedStmt.execute();

                                                    Long patentId = 0L;
                                                    preparedStmt = conn.prepareStatement("select last_insert_id()");
                                                    ResultSet rSet = preparedStmt.executeQuery();

                                                    while (rSet.next()) {

                                                        patentId = rSet.getLong(1);
                                                    }

                                                    // Classification
                                                    Element elClass = (Element) elBiblio.getElementsByTagName("classification-ipc").item(0);

                                                    if (elClass != null) {

                                                        NodeList list = elClass.getElementsByTagName("further-classification");

                                                        List<String> lists = new ArrayList<>();

                                                        Element mainNot = (Element) elClass.getElementsByTagName("main-classification").item(0);

                                                        if (mainNot != null) {

                                                            String full = mainNot.getTextContent();
                                                            full = full.substring(0, 4);

                                                            if (lists.contains(full)) continue;
                                                            lists.add(full);

                                                            String section = full.substring(0, 1);
                                                            String sClass = full.substring(1, 3);
                                                            String subclass = full.substring(3, 4);

                                                            preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                                            preparedStmt.setLong(1, patentId);
                                                            preparedStmt.setString(2, section);
                                                            preparedStmt.setString(3, sClass);
                                                            preparedStmt.setString(4, subclass);
                                                            preparedStmt.execute();
                                                        }

                                                        for (int l = 0; l < list.getLength(); l++) {

                                                            Element elClassS = (Element) list.item(0);

                                                            if (elClassS != null) {
                                                                String full = elClassS.getTextContent();
                                                                full = full.substring(0, 4);

                                                                if (lists.contains(full)) continue;
                                                                lists.add(full);

                                                                String section = full.substring(0, 1);
                                                                String sClass = full.substring(1, 3);
                                                                String subclass = full.substring(3, 4);

                                                                preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                                                preparedStmt.setLong(1, patentId);
                                                                preparedStmt.setString(2, section);
                                                                preparedStmt.setString(3, sClass);
                                                                preparedStmt.setString(4, subclass);
                                                                preparedStmt.execute();
                                                            }
                                                        }
                                                    }

                                                    // Inventors
                                                    Element elParties = (Element) elBiblio.getElementsByTagName("parties").item(0);

                                                    if (elParties != null) {
                                                        Element elInventors = (Element) elParties.getElementsByTagName("inventors").item(0);

                                                        if (elInventors != null) {
                                                            NodeList inventors = elInventors.getElementsByTagName("inventor");

                                                            for (int l = 0; l < inventors.getLength(); l++) {

                                                                Element inventor = (Element) inventors.item(l);
                                                                Element addressbook = (Element) inventor.getElementsByTagName("addressbook").item(0);

                                                                if (addressbook != null) {

                                                                    if (addressbook.getElementsByTagName("last-name").item(0) == null)
                                                                        continue;
                                                                    if (addressbook.getElementsByTagName("first-name").item(0) == null)
                                                                        continue;

                                                                    String lastName = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                                                    String firstName = addressbook.getElementsByTagName("first-name").item(0).getTextContent();

                                                                    String name = lastName + " " + firstName;

                                                                    if (name.length() > 100)
                                                                        name = name.substring(0, 100);

                                                                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                                    preparedStmt.setLong(1, patentId);
                                                                    preparedStmt.setString(2, name);
                                                                    preparedStmt.execute();
                                                                }
                                                            }
                                                        }

                                                        // Applicants
                                                        Element elApplicants = (Element) elParties.getElementsByTagName("applicants").item(0);

                                                        if (elApplicants != null) {
                                                            NodeList applicants = elApplicants.getElementsByTagName("applicant");

                                                            for (int l = 0; l < applicants.getLength(); l++) {

                                                                Element applicant = (Element) applicants.item(l);
                                                                Element addressbook = (Element) applicant.getElementsByTagName("addressbook").item(0);
                                                                if (addressbook != null) {

                                                                    if (addressbook.getElementsByTagName("last-name").item(0) == null)
                                                                        continue;
                                                                    if (addressbook.getElementsByTagName("first-name").item(0) == null)
                                                                        continue;

                                                                    String lastName = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                                                    String firstName = addressbook.getElementsByTagName("first-name").item(0).getTextContent();

                                                                    String name = lastName + " " + firstName;

                                                                    if (name.length() > 100)
                                                                        name = name.substring(0, 100);

                                                                    preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                                                    preparedStmt.setLong(1, patentId);
                                                                    preparedStmt.setString(2, name);
                                                                    preparedStmt.execute();
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            } catch (SQLException e) {

                                                if (e.getMessage().contains("Duplicate entry")) {


                                                } else {
                                                    e.printStackTrace();
                                                    exit(1);
                                                }
                                            }catch (Exception e) {

                                                e.printStackTrace();
                                                exit(1);
                                            }
                                        } catch (Exception e) {
                                            System.out.println(xml.getAbsolutePath());
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            continue;
                        }

                        for (File docFile : docFiles) {

                            if (docFile.getName().contains("doc")) {

                                File[] allFiles = docFile.listFiles();

                                for (File allFile: allFiles) {

                                    if (allFile.getAbsolutePath().contains("evropske")) {

                                        File[] xmls = allFile.listFiles();

                                        for (File xml: xmls) {

                                            if (xml.getName().toLowerCase(Locale.ROOT).contains("toc")) {
                                                continue;
                                            }

                                            String filename = xml.getName();

                                            if (set.contains(filename)) {

                                            } else {


                                                // Instantiate the Factory
                                                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                                                // optional, but recommended
                                                // process XML securely, avoid attacks like XML External Entities (XXE)
                                                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                                                // Disable external DTDs as well
                                                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                                                try {
                                                    // parse XML file
                                                    DocumentBuilder db = dbf.newDocumentBuilder();
                                                    Document doc = db.parse(xml);
                                                    doc.getDocumentElement().normalize();

                                                    Node documentId = doc.getElementsByTagName("document-id").item(0);
                                                    Element el = (Element) documentId;
                                                    String dateString = "";

                                                    try {
                                                        dateString = el.getElementsByTagName("date").item(0).getTextContent();
                                                    } catch (Exception e) {

                                                    }

                                                    if (!dateString.startsWith("20") && !dateString.equals("")) {
                                                        set2000.add(filename);
                                                        continue;
                                                    }

                                                    set.add(filename);

                                                    // EP + CCP
                                                    String newXmlContentString = "";
                                                    PreparedStatement preparedStmt = null;

                                                    try {
                                                        // patent_id ; title ; date ; kind ; country    -  patents
                                                        // id_patent ; section ; class ; subclass    - classification
                                                        // id_patent ; language     - languages
                                                        // id_patent ; inventor     - inventors
                                                        // id_patent ; applicant    - applicants

                                                        Element elBiblio = (Element) doc.getElementsByTagName("fr-bibliographic-data").item(0);
                                                        if (elBiblio != null) {
                                                            Element elPubliData = (Element) elBiblio.getElementsByTagName("fr-publication-data").item(0);
                                                            if (elPubliData != null) {
                                                                Element elPubliRef = (Element) elPubliData.getElementsByTagName("fr-publication-reference").item(0);
                                                                if (elPubliRef != null) {
                                                                    Element elPubliDocId = (Element) elPubliRef.getElementsByTagName("document-id").item(0);
                                                                    if (elPubliDocId != null) {

                                                                        // Patents
                                                                        String country = "";
                                                                        if (elPubliDocId.getElementsByTagName("country") == null || elPubliDocId.getElementsByTagName("country").item(0) == null) country = "FR";
                                                                        else country = elPubliDocId.getElementsByTagName("country").item(0).getTextContent();

                                                                        String sPatentId = country;
                                                                        if (elPubliDocId.getElementsByTagName("doc-number") == null || elPubliDocId.getElementsByTagName("doc-number").item(0) == null) continue;
                                                                        else sPatentId = elPubliDocId.getElementsByTagName("doc-number").item(0).getTextContent();

                                                                        Element elKind = (Element) elPubliDocId.getElementsByTagName("kind").item(0);
                                                                        String kind = "-";
                                                                        if (elKind != null)
                                                                            kind = elKind.getTextContent().substring(0, 1);

                                                                        Element elDate = (Element) elPubliDocId.getElementsByTagName("date").item(0);
                                                                        String date = "";
                                                                        if (elDate != null)
                                                                            date = elDate.getTextContent();

                                                                        if (date.equals("")) continue;

                                                                        String title = "";

                                                                        if (elBiblio.getElementsByTagName("invention-title") == null || elBiblio.getElementsByTagName("invention-title").item(0) == null) continue;
                                                                        else title = elBiblio.getElementsByTagName("invention-title").item(0).getTextContent();

                                                                        if (title == null || title.equals("")) continue;

                                                                        if (title.length() > 300)
                                                                            title = title.substring(0, 300);

                                                                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                                                        java.util.Date utilDate = format.parse(date);
                                                                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                                                        String language = elBiblio.getAttribute("lang").toUpperCase();

                                                                        preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                                                        preparedStmt.setString(1, sPatentId);
                                                                        preparedStmt.setString(2, title);
                                                                        preparedStmt.setDate(3, sqlDate);
                                                                        preparedStmt.setString(4, kind);
                                                                        preparedStmt.setString(5, country);
                                                                        preparedStmt.setString(6, language);
                                                                        preparedStmt.execute();

                                                                        Long patentId = 0L;
                                                                        preparedStmt = conn.prepareStatement("select last_insert_id()");
                                                                        ResultSet rSet = preparedStmt.executeQuery();

                                                                        while (rSet.next()) {

                                                                            patentId = rSet.getLong(1);
                                                                        }

                                                                        // Inventors
                                                                        Element elParties = (Element) elBiblio.getElementsByTagName("parties").item(0);
                                                                        Element elInventors = (Element) elBiblio.getElementsByTagName("fr-owners").item(0);

                                                                        if (elInventors != null) {
                                                                            NodeList inventors = elInventors.getElementsByTagName("fr-owner");

                                                                            for (int l = 0; l < inventors.getLength(); l++) {

                                                                                Element inventor = (Element) inventors.item(l);
                                                                                Element addressbook = (Element) inventor.getElementsByTagName("addressbook").item(0);

                                                                                if (addressbook == null) continue;

                                                                                if (addressbook.getElementsByTagName("last-name").item(0) == null)
                                                                                    continue;
                                                                                if (addressbook.getElementsByTagName("first-name").item(0) == null)
                                                                                    continue;

                                                                                String lastName = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                                                                String firstName = addressbook.getElementsByTagName("first-name").item(0).getTextContent();

                                                                                String name = lastName;
                                                                                if (firstName.length() > 0)
                                                                                    name += " " + firstName;

                                                                                if (name.length() > 100)
                                                                                    name = name.substring(0, 100);

                                                                                preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                                                preparedStmt.setLong(1, patentId);
                                                                                preparedStmt.setString(2, name);
                                                                                preparedStmt.execute();
                                                                            }
                                                                        }

                                                                        // Applicants
                                                                        if (elParties != null) {
                                                                            Element elApplicants = (Element) elParties.getElementsByTagName("applicants").item(0);
                                                                            if (elApplicants != null) {
                                                                                NodeList applicants = elApplicants.getElementsByTagName("applicant");

                                                                                for (int l = 0; l < applicants.getLength(); l++) {

                                                                                    Element applicant = (Element) applicants.item(l);
                                                                                    Element addressbook = (Element) applicant.getElementsByTagName("addressbook").item(0);

                                                                                    if (addressbook == null) continue;

                                                                                    if (addressbook.getElementsByTagName("last-name").item(0) == null)
                                                                                        continue;
                                                                                    if (addressbook.getElementsByTagName("first-name").item(0) == null)
                                                                                        continue;

                                                                                    String lastName = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                                                                    String firstName = addressbook.getElementsByTagName("first-name").item(0).getTextContent();

                                                                                    String name = lastName;
                                                                                    if (firstName.length() > 0)
                                                                                        name += " " + firstName;

                                                                                    if (name.length() > 100)
                                                                                        name = name.substring(0, 100);

                                                                                    preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                                                                    preparedStmt.setLong(1, patentId);
                                                                                    preparedStmt.setString(2, name);
                                                                                    preparedStmt.execute();
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                    } catch (SQLException e) {

                                                        if (e.getMessage().contains("Duplicate entry")) {


                                                        } else {
                                                            e.printStackTrace();
                                                            exit(1);
                                                        }
                                                    }catch (Exception e) {

                                                        e.printStackTrace();
                                                        exit(1);
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println(xml.getAbsolutePath());
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }

                                    else if (allFile.getName().contains(".xml")) {

                                        if (allFile.getName().toLowerCase(Locale.ROOT).contains("toc")) {
                                            continue;
                                        }

                                        String filename = allFile.getName();

                                        if (set.contains(filename)) {

                                        } else {



                                            // Instantiate the Factory
                                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                                            // optional, but recommended
                                            // process XML securely, avoid attacks like XML External Entities (XXE)
                                            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                                            // Disable external DTDs as well
                                            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                                            try {
                                                // parse XML file
                                                DocumentBuilder db = dbf.newDocumentBuilder();
                                                Document doc = db.parse(allFile);
                                                doc.getDocumentElement().normalize();

                                                Node documentId = doc.getElementsByTagName("document-id").item(0);
                                                Element el = (Element) documentId;
                                                String dateString = "";

                                                try {
                                                    dateString = el.getElementsByTagName("date").item(0).getTextContent();
                                                } catch (Exception e) {

                                                }

                                                if (!dateString.startsWith("20") && !dateString.equals("")) {
                                                    set2000.add(filename);
                                                    continue;
                                                }

                                                set.add(filename);

                                                // 2021
                                                String newXmlContentString = "";
                                                PreparedStatement preparedStmt = null;

                                                try {

                                                    // patent_id ; title ; date ; kind ; country    -  patents
                                                    // id_patent ; section ; class ; subclass    - classification
                                                    // id_patent ; language     - languages
                                                    // id_patent ; inventor     - inventors
                                                    // id_patent ; applicant    - applicants

                                                    Element elBiblio = (Element) doc.getElementsByTagName("fr-bibliographic-data").item(0);

                                                    if (elBiblio != null) {

                                                        // Patents
                                                        String country = "";

                                                        if (el.getElementsByTagName("country") == null || el.getElementsByTagName("country").item(0) == null)
                                                            country = "FR";
                                                        else
                                                            country = el.getElementsByTagName("country").item(0).getTextContent();

                                                        String title = "";
                                                        if (elBiblio.getElementsByTagName("invention-title") == null || elBiblio.getElementsByTagName("invention-title").item(0) == null)
                                                            continue;
                                                        else
                                                            title = elBiblio.getElementsByTagName("invention-title").item(0).getTextContent();

                                                        String sPatentId = "";
                                                        if (el.getElementsByTagName("doc-number") == null || el.getElementsByTagName("doc-number").item(0) == null)
                                                            continue;
                                                        else
                                                            sPatentId = country + el.getElementsByTagName("doc-number").item(0).getTextContent();

                                                        String date = "";
                                                        if (el.getElementsByTagName("date") == null || el.getElementsByTagName("date").item(0) == null)
                                                            continue;
                                                        else
                                                            date = el.getElementsByTagName("date").item(0).getTextContent();

                                                        String kind = "";
                                                        if (el.getElementsByTagName("kind") == null || el.getElementsByTagName("kind").item(0) == null)
                                                            kind = "-";
                                                        else
                                                            kind = el.getElementsByTagName("kind").item(0).getTextContent().substring(0, 1);

                                                        if (title == null || title.equals("")) continue;

                                                        if (title.length() > 300) title = title.substring(0, 300);

                                                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                                        java.util.Date utilDate = format.parse(date);
                                                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                                        String language = "FR";

                                                        preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                                        preparedStmt.setString(1, sPatentId);
                                                        preparedStmt.setString(2, title);
                                                        preparedStmt.setDate(3, sqlDate);
                                                        preparedStmt.setString(4, kind);
                                                        preparedStmt.setString(5, country);
                                                        preparedStmt.setString(6, language);
                                                        preparedStmt.execute();

                                                        Long patentId = 0L;
                                                        preparedStmt = conn.prepareStatement("select last_insert_id()");
                                                        ResultSet rSet = preparedStmt.executeQuery();

                                                        while (rSet.next()) {

                                                            patentId = rSet.getLong(1);
                                                        }

                                                        // Classification
                                                        Element elClass = (Element) elBiblio.getElementsByTagName("classifications-ipcr").item(0);
                                                        if (elClass != null) {
                                                            NodeList list = elClass.getElementsByTagName("classification-ipcr");

                                                            List<String> lists = new ArrayList<>();

                                                            for (int l = 0; l < list.getLength(); l++) {

                                                                Element elClassS = (Element) list.item(0);
                                                                Element text = (Element) elClassS.getElementsByTagName("text").item(0);

                                                                if (text == null) continue;

                                                                String full = text.getTextContent();
                                                                full = full.substring(0, 4);

                                                                if (lists.contains(full)) continue;
                                                                lists.add(full);

                                                                String section = full.substring(0, 1);
                                                                String sClass = full.substring(1, 3);
                                                                String subclass = full.substring(3, 4);

                                                                preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                                                preparedStmt.setLong(1, patentId);
                                                                preparedStmt.setString(2, section);
                                                                preparedStmt.setString(3, sClass);
                                                                preparedStmt.setString(4, subclass);
                                                                preparedStmt.execute();
                                                            }
                                                        }

                                                        // Inventors
                                                        Element elParties = (Element) elBiblio.getElementsByTagName("parties").item(0);

                                                        if (elParties != null) {
                                                            Element elInventors = (Element) elParties.getElementsByTagName("inventors").item(0);

                                                            if (elInventors != null) {
                                                                NodeList inventors = elInventors.getElementsByTagName("inventor");

                                                                for (int l = 0; l < inventors.getLength(); l++) {

                                                                    Element inventor = (Element) inventors.item(l);
                                                                    Element addressbook = (Element) inventor.getElementsByTagName("addressbook").item(0);

                                                                    if (addressbook == null) continue;

                                                                    if (addressbook.getElementsByTagName("last-name").item(0) == null)
                                                                        continue;
                                                                    if (addressbook.getElementsByTagName("first-name").item(0) == null)
                                                                        continue;

                                                                    String lastName = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                                                    String firstName = addressbook.getElementsByTagName("first-name").item(0).getTextContent();

                                                                    String name = lastName + " " + firstName;

                                                                    if (name.length() > 100)
                                                                        name = name.substring(0, 100);

                                                                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                                    preparedStmt.setLong(1, patentId);
                                                                    preparedStmt.setString(2, name);
                                                                    preparedStmt.execute();
                                                                }
                                                            }

                                                            // Applicants
                                                            Element elApplicants = (Element) elParties.getElementsByTagName("applicants").item(0);

                                                            if (elApplicants != null) {
                                                                NodeList applicants = elApplicants.getElementsByTagName("applicant");

                                                                for (int l = 0; l < applicants.getLength(); l++) {

                                                                    Element applicant = (Element) applicants.item(l);
                                                                    Element addressbook = (Element) applicant.getElementsByTagName("addressbook").item(0);

                                                                    if (addressbook == null) continue;

                                                                    if (addressbook.getElementsByTagName("last-name").item(0) == null)
                                                                        continue;
                                                                    if (addressbook.getElementsByTagName("first-name").item(0) == null)
                                                                        continue;

                                                                    String lastName = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                                                    String firstName = addressbook.getElementsByTagName("first-name").item(0).getTextContent();

                                                                    String name = lastName + " " + firstName;

                                                                    if (name.length() > 100)
                                                                        name = name.substring(0, 100);

                                                                    preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                                                    preparedStmt.setLong(1, patentId);
                                                                    preparedStmt.setString(2, name);
                                                                    preparedStmt.execute();
                                                                }
                                                            }
                                                        }
                                                    }

                                                } catch (SQLException e) {

                                                    if (e.getMessage().contains("Duplicate entry")) {


                                                    } else {
                                                        e.printStackTrace();
                                                        exit(1);
                                                    }
                                                }catch (Exception e) {

                                                    e.printStackTrace();
                                                    exit(1);
                                                }
                                            } catch (Exception e) {
                                                System.out.println(allFile.getAbsolutePath());
                                                e.printStackTrace();
                                            }
                                        }

                                        //System.out.println();

                                    }
                                }
                            }
                        }
                    }

                }
            }}

        System.out.println("Francie");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPřed 2000: " + set2000.size());
        System.out.println("\n");

        try {
            FileWriter myWriter = new FileWriter("D:\\PATENTY\\logs.txt");

            for (Exception e : exceptions) {

                myWriter.append("0 ").append(e.toString());
                myWriter.append("1 ").append(e.getMessage());
                myWriter.append("2 ").append(e.getLocalizedMessage());
                myWriter.append("\n");
            }

            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void convertSpanelsko() throws IOException, ParserConfigurationException, SAXException {

        List<String> files;

        HashSet<String> set = new HashSet<String>();
        HashSet<String> set2000 = new HashSet<String>();

        long test = 0;

        File israelDir = new File(inputFolder + "Spanelsko");
        File[] years = israelDir.listFiles();

        List<Exception> exceptions = new ArrayList<>();

        for (int monn = years.length - 1; monn >= 0; monn-- ) {

            File year = years[monn];
            File[] folders = year.listFiles();
            System.out.println(year.getAbsolutePath());

            for (File folder: folders) {

                File[] anotherFolders = folder.listFiles();

                for (File anotherFolder : anotherFolders) {

                    File[] xmls = anotherFolder.listFiles();

                    for (File xml : xmls) {

                        if (!xml.getAbsolutePath().contains(".xml")) continue;

                        String xmlName = xml.getName();

                        if (set.contains(xmlName)) continue;

                        // Instantiate the Factory
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                        // optional, but recommended
                        // process XML securely, avoid attacks like XML External Entities (XXE)
                        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                        // Disable external DTDs as well
                        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                        List<String> lines = null;
                        Charset charset = null;
                        try {
                            charset = Charset.forName("windows-1252");
                            lines = Files.readAllLines(Paths.get(xml.getAbsolutePath()), charset);
                        }
                        catch (Exception eee) {
                            try {
                                charset = StandardCharsets.UTF_8;
                                lines = Files.readAllLines(Paths.get(xml.getAbsolutePath()), charset);
                            } catch (Exception eeeee) {

                                System.out.println(xml.getAbsolutePath());
                                eeeee.printStackTrace();
                                continue;
                            }
                        }

                        String content = "";

                        for (String line: lines) {

                            content += line + "\n";
                        }

                        content = content.replaceAll("&break;", "").replaceAll("<i>", "").replaceAll("</i>", "");
                        content = content.replaceAll("ietdeclaa", "ietdecla");
                        content = content.replaceAll("&newline;", "");
                        content = content.replaceAll("</b></q>", "</q></b>");
                        content = content.replaceAll("text", "tex");

                        try (OutputStreamWriter writer =
                                     new OutputStreamWriter(new FileOutputStream(xml.getAbsolutePath()), charset))
                        // do stuff
                        {
                            writer.write(content);
                        }


                        try {
                            // parse XML file
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(xml);
                            doc.getDocumentElement().normalize();

                            Node documentId = doc.getElementsByTagName("document-id").item(0);
                            Element el = (Element) documentId;
                            String dateString = "";
                            String dateDate = "";

                            try {
                                dateString = el.getElementsByTagName("date").item(0).getTextContent();
                            } catch (Exception e) {

                                try {
                                    dateString = doc.getElementsByTagName("eappldate").item(0).getTextContent();
                                } catch (Exception ee) {

                                }
                            }

                            if (dateString.length() == 0) continue;

                            if (dateString.length() > 0) {

                                dateDate = dateString;
                                dateString = dateString.substring(dateString.lastIndexOf(".") + 1);
                            }

                            if (!dateString.startsWith("20") && !dateString.equals("")) {
                                set2000.add(xmlName);
                                continue;
                            }

                            set.add(xmlName);

                            String newXmlContentString = "";

                            try {

                                PreparedStatement preparedStmt = null;

                                // patent_id ; title ; date ; kind ; country    -  patents
                                // id_patent ; section ; class ; subclass    - classification
                                // id_patent ; language     - languages
                                // id_patent ; inventor     - inventors
                                // id_patent ; applicant    - applicants

                                // Patents
                                String sPatentId = el.getElementsByTagName("doc-number").item(0).getTextContent();
                                String title = "";
                                String date = dateDate;
                                String kind = "-";
                                String country = el.getElementsByTagName("country").item(0).getTextContent();;

                                NodeList titles = doc.getElementsByTagName("invention-title");
                                for (int i = 0; i < titles.getLength(); i++) {

                                    Element elTitle = (Element) titles.item(i);
                                    if (elTitle.getAttribute("lang").equalsIgnoreCase("es")) {
                                        title = elTitle.getTextContent();
                                    }
                                }

                                if (title.length() > 300) title = title.substring(0, 300);

                                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
                                java.util.Date utilDate = format.parse(date);
                                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                String language = "ES";

                                preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                preparedStmt.setString(1, sPatentId);
                                preparedStmt.setString(2, title);
                                preparedStmt.setDate(3, sqlDate);
                                preparedStmt.setString(4, kind);
                                preparedStmt.setString(5, country);
                                preparedStmt.setString(6, language);
                                preparedStmt.execute();

                                Long patentId = 0L;
                                preparedStmt = conn.prepareStatement("select last_insert_id()");
                                ResultSet rSet = preparedStmt.executeQuery();

                                while (rSet.next()) {

                                    patentId = rSet.getLong(1);
                                }

                                // Classification
                                String section = "";
                                String sClass = "";
                                String subclass = "";

                                Element elPatent =(Element) doc.getElementsByTagName("patent").item(0);
                                if (elPatent == null ) elPatent = (Element) doc.getElementsByTagName("Patent").item(0);
                                Element elIet = (Element) elPatent.getElementsByTagName("iet").item(0);
                                if (elIet != null) {
                                    Element classifications = (Element) elIet.getElementsByTagName("classifications").item(0);

                                    if (classifications != null && classifications.getAttribute("level").equals("basic")) {

                                        Element clasIpc = (Element) classifications.getElementsByTagName("classification1").item(0);

                                        if (clasIpc == null) continue;
                                        String sClassif = clasIpc.getTextContent();

                                        String[] array = sClassif.split(",");

                                        List<String> listClass = new ArrayList<>();

                                        for (int l = 0; l < array.length; l++) {

                                            String sCl = array[l];

                                            if (sCl.length() < 1) continue;
                                            if (sCl.charAt(0) == ' ') sCl = sCl.substring(1);

                                            section = sCl.substring(0, 1);
                                            sClass = sCl.substring(1, 3);
                                            subclass = sCl.substring(3, 4);

                                            String full = section + sClass + subclass;
                                            if (listClass.contains(full)) continue;
                                            listClass.add(full);

                                            preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                            preparedStmt.setLong(1, patentId);
                                            preparedStmt.setString(2, section);
                                            preparedStmt.setString(3, sClass);
                                            preparedStmt.setString(4, subclass);
                                            preparedStmt.execute();
                                        }

                                    } else if (classifications != null && classifications.getAttribute("level").equals("advanced")) {

                                        NodeList nodeList = classifications.getElementsByTagName("classificationipc");

                                        for (int l = 0; l < nodeList.getLength(); l++) {

                                            Element clasIpc = (Element) nodeList.item(l);
                                            Element symbols = (Element) clasIpc.getElementsByTagName("classsymbols").item(0);
                                            if (symbols == null) continue;
                                            String sSymbols = symbols.getTextContent();

                                            section = sSymbols.substring(0, 1);
                                            sClass = sSymbols.substring(1, 3);
                                            subclass = sSymbols.substring(3, 4);

                                            preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                            preparedStmt.setLong(1, patentId);
                                            preparedStmt.setString(2, section);
                                            preparedStmt.setString(3, sClass);
                                            preparedStmt.setString(4, subclass);
                                            preparedStmt.execute();
                                        }

                                    } else {
                                    }
                                }


                                // Inventors
                                Element parties = (Element) elPatent.getElementsByTagName("parties").item(0);
                                Element elInventors = (Element) parties.getElementsByTagName("inventors").item(0);

                                boolean inventorAdded = false;

                                if (elInventors != null) {

                                    NodeList lApplicants = elInventors.getElementsByTagName("inventor");

                                    for (int k = 0; k < lApplicants.getLength(); k++) {

                                        Element elApplicant = (Element) lApplicants.item(k);
                                        Element addressbook = (Element) elApplicant.getElementsByTagName("addressbook").item(0);
                                        Element lastName = (Element) addressbook.getElementsByTagName("last-name").item(0);
                                        Element firstName = (Element) addressbook.getElementsByTagName("first-name").item(0);

                                        if (lastName == null || firstName == null) continue;

                                        String name = lastName.getTextContent();
                                        name += " " + firstName.getTextContent();;

                                            if (name.length() > 100) {
                                                name = name.substring(0, 100);
                                            }

                                            preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                            preparedStmt.setLong(1, patentId);
                                            preparedStmt.setString(2, name);
                                            preparedStmt.execute();
                                    }
                                }

                                // Applicants
                                Element elApplicants = (Element) parties.getElementsByTagName("applicants").item(0);

                                if (elApplicants != null) {

                                    NodeList lApplicants = elApplicants.getElementsByTagName("applicant");

                                    for (int k = 0; k < lApplicants.getLength(); k++) {

                                        Element elApplicant = (Element) lApplicants.item(k);
                                        Element addressbook = (Element) elApplicant.getElementsByTagName("addressbook").item(0);
                                        Element lastName = (Element) addressbook.getElementsByTagName("last-name").item(0);
                                        Element firstName = (Element) addressbook.getElementsByTagName("first-name").item(0);

                                        if (lastName == null || firstName == null) continue;

                                        String name = addressbook.getElementsByTagName("last-name").item(0).getTextContent();
                                        name += " " + addressbook.getElementsByTagName("first-name").item(0).getTextContent();;

                                        if (name.length() > 100) {
                                            name = name.substring(0, 100);
                                        }

                                        preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                        preparedStmt.setLong(1, patentId);
                                        preparedStmt.setString(2, name);
                                        preparedStmt.execute();
                                    }
                                }

                            } catch (SQLException e) {

                                if (e.getMessage().contains("Duplicate entry")) {


                                } else {
                                    e.printStackTrace();
                                    exit(1);
                                }
                            }
                            catch (Exception e) {

                                e.printStackTrace();
                                exit(1);
                            }
                        } catch (Exception en) {
                            test++;
                            continue;
                        }
                    }
                }
            }
        }

        System.out.println("Španělsko");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\t2000: " + set2000.size());
        System.out.println("\tInvalid for breaks: " + test);
        System.out.println("Exceptions: " + exceptions.size());
        System.out.println("\n");

        FileWriter myWriter = new FileWriter("D:\\PATENTY\\logs.txt");

        for (Exception e : exceptions) {

            myWriter.append("0 ").append(e.toString());
            myWriter.append("1 ").append(e.getMessage());
            myWriter.append("2 ").append(e.getLocalizedMessage());
            myWriter.append("\n");
        }

        myWriter.close();
    }

    public static void convertLitva() throws IOException {

        List<String> files;

        HashSet<String> set = new HashSet<String>();

        File israelDir = new File(inputFolder + "Litva");
        File[] years = israelDir.listFiles();

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

        for (int monn = years.length - 1; monn >= 0; monn-- ) {

            File year = years[monn];
            File[] folders = year.listFiles();

            for (File folder: folders) {

                File[] xmls = folder.listFiles();

                for (File xml : xmls) {

                    String xmlName = xml.getName();
                    String substr = xmlName.substring(xmlName.lastIndexOf("_") + 1);

                    if (xml.getAbsolutePath().contains("TOC") || xml.getAbsolutePath().contains(".dtd")) continue;

                    if (set.contains(substr)) continue;

                    set.add(substr);
                    /*
                    Path path = Paths.get(xml.getAbsolutePath());

                    List<String> lines = Files.readAllLines(path);

                    String newXmlContentString = "";

                    for (String line: lines) {

                        newXmlContentString += line + "\n";
                    }


                    // Convert xml to json.
                    JSONObject json = XML.toJSONObject(newXmlContentString);
                    String jsonString = json.toString(4);

                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                    collection.insert(dbObject);

                     */

                    // patent_id ; title ; date ; kind ; country    -  patents
                    // id_patent ; section ; class ; subclass    - classification
                    // id_patent ; language     - languages
                    // id_patent ; inventor     - inventors
                    // id_patent ; applicant    - applicants

                    // Instantiate the Factory
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());



                    try {

                        // optional, but recommended
                        // process XML securely, avoid attacks like XML External Entities (XXE)
                        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                        // Disable external DTDs as well
                        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                        // optional, but recommended
                        // process XML securely, avoid attacks like XML External Entities (XXE)
                        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

                        // parse XML file
                        DocumentBuilder db = dbf.newDocumentBuilder();

                        Document doc = db.parse(xml);
                        doc.getDocumentElement().normalize();

                        NodeList patentDocuments = doc.getElementsByTagName("simple-patent-document");

                        for (int i = 0; i < patentDocuments.getLength(); i++) {

                            Element elPatent = (Element) patentDocuments.item(i);
                            NodeList publicationRefereceE = elPatent.getElementsByTagName("publication-reference");

                            if (publicationRefereceE != null && publicationRefereceE.getLength() > 0) {

                                Element publicationReference = (Element) publicationRefereceE.item(0);
                                Element elBiblio = (Element) elPatent.getElementsByTagName("bibliographic-data").item(0);

                                Node documentId = publicationReference.getElementsByTagName("document-id").item(0);
                                if (documentId.getNodeType() == Node.ELEMENT_NODE) {

                                    Element el = (Element) documentId;

                                    String docNumberString = el.getElementsByTagName("doc-number").item(0).getTextContent();

                                    NodeList dateElements = el.getElementsByTagName("date");
                                    String date = "";

                                    if (dateElements.getLength() > 0) {
                                        date = el.getElementsByTagName("date").item(0).getTextContent();
                                    }
                                        try {
                                            PreparedStatement preparedStmt = null;

                                            // Patents
                                            String strPatentId = "LT" + docNumberString;

                                            String title = "";
                                            NodeList inventionTitles = elBiblio.getElementsByTagName("invention-title");
                                            for (int k = 0; k < inventionTitles.getLength(); k++) {

                                                Element inventionTitle = (Element) inventionTitles.item(k);

                                                if (inventionTitle.getAttribute("lang").equalsIgnoreCase("en")) {

                                                    title = inventionTitle.getTextContent();
                                                    break;
                                                }
                                            }

                                            if (title.length() > 300) title = title.substring(0, 300);

                                            String kind = elPatent.getAttribute("kind");
                                            String country = "LT";

                                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                            java.util.Date utilDate = format.parse(date);
                                            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                            String language = elBiblio.getElementsByTagName("language-of-publication").item(0).getTextContent().toUpperCase();

                                            preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                            preparedStmt.setString(1, strPatentId);
                                            preparedStmt.setString(2, title);
                                            preparedStmt.setDate(3, sqlDate);
                                            preparedStmt.setString(4, kind);
                                            preparedStmt.setString(5, country);
                                            preparedStmt.setString(6, language);
                                            preparedStmt.execute();

                                            Long patentId = 0L;
                                            preparedStmt = conn.prepareStatement("select last_insert_id()");
                                            ResultSet rSet = preparedStmt.executeQuery();

                                            while (rSet.next()) {

                                                patentId = rSet.getLong(1);
                                            }

                                            // Classification
                                            Element elClassifications = (Element) elBiblio.getElementsByTagName("classifications-ipcr").item(0);

                                            if (elClassifications != null) {
                                                NodeList lClassifications = elClassifications.getElementsByTagName("classification-ipcr");

                                                List<String> classis = new ArrayList<>();

                                                for (int j = 0; j < lClassifications.getLength(); j++) {

                                                    Element elClass = (Element) lClassifications.item(j);
                                                    String section = elClass.getElementsByTagName("section").item(0).getTextContent();
                                                    String sClass = elClass.getElementsByTagName("class").item(0).getTextContent();
                                                    String subClass = elClass.getElementsByTagName("subclass").item(0).getTextContent();

                                                    String fullClassification = section + sClass + subClass;

                                                    if (classis.contains(fullClassification)) continue;

                                                    classis.add(fullClassification);

                                                    preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                                    preparedStmt.setLong(1, patentId);
                                                    preparedStmt.setString(2, section);
                                                    preparedStmt.setString(3, sClass);
                                                    preparedStmt.setString(4, subClass);
                                                    preparedStmt.execute();
                                                }
                                            }


                                            // Inventors
                                            Element parties = (Element) elBiblio.getElementsByTagName("parties").item(0);
                                            Element elApplicants = (Element) parties.getElementsByTagName("applicants").item(0);

                                            boolean inventorAdded = false;

                                            if (elApplicants != null) {

                                                NodeList lApplicants = elApplicants.getElementsByTagName("applicant");

                                                for (int k = 0; k < lApplicants.getLength(); k++) {

                                                    Element elApplicant = (Element) lApplicants.item(k);
                                                    Element addressbook = (Element) elApplicant.getElementsByTagName("addressbook").item(0);

                                                    NodeList nameList = addressbook.getElementsByTagName("orgname");

                                                    if (nameList != null && nameList.getLength() > 0) {

                                                        String name = addressbook.getElementsByTagName("orgname").item(0).getTextContent();

                                                        if (name.length() > 100) {
                                                            name = name.substring(0, 100);
                                                        }

                                                        preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                        preparedStmt.setLong(1, patentId);
                                                        preparedStmt.setString(2, name);
                                                        preparedStmt.execute();

                                                        inventorAdded = true;
                                                    }
                                                }
                                            }

                                            if (!inventorAdded) {

                                                Element elInventors = (Element) parties.getElementsByTagName("inventors").item(0);

                                                if (elInventors != null) {

                                                    NodeList lInventors = elInventors.getElementsByTagName("inventor");

                                                    for (int k = 0; k < lInventors.getLength(); k++) {

                                                        Element elApplicant = (Element) lInventors.item(k);
                                                        Element addressbook = (Element) elApplicant.getElementsByTagName("addressbook").item(0);
                                                        Element address = (Element) addressbook.getElementsByTagName("address").item(0);

                                                        NodeList countryList = address.getElementsByTagName("country");

                                                        if (countryList != null && countryList.getLength() > 0) {

                                                            String name = address.getElementsByTagName("country").item(0).getTextContent();

                                                            if (name.length() > 100) {
                                                                name = name.substring(0, 100);
                                                            }

                                                            preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                            preparedStmt.setLong(1, patentId);
                                                            preparedStmt.setString(2, name);
                                                            preparedStmt.execute();

                                                            break;
                                                        }
                                                    }
                                                }
                                            }

                                        } catch (Exception e) {

                                            e.printStackTrace();
                                            exit(1);
                                        }
                                }
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("Litva");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\n");
    }

    private static void convertIsrael() throws IOException {

        long duplicates = 0;
        HashSet<String> set2000 = new HashSet<>();
        HashSet<String> set = new HashSet<String>();

        File israelDir = new File(inputFolder + "Izrael");
        File[] xmls = israelDir.listFiles();

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

        for (int j = xmls.length - 1; j >= 0; j--)
        {
            File xmlFile = xmls[j];


            // Instantiate the Factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {

                // optional, but recommended
                // process XML securely, avoid attacks like XML External Entities (XXE)
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

                // parse XML file
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document doc = db.parse(xmlFile);
                doc.getDocumentElement().normalize();

                NodeList patentDocuments = doc.getElementsByTagName("il-patent-document");

                for (int i = 0; i < patentDocuments.getLength(); i++) {

                    Element elPatent = (Element) patentDocuments.item(i);
                    NodeList publicationRefereceE = elPatent.getElementsByTagName("publication-reference");

                    if (publicationRefereceE != null && publicationRefereceE.getLength() > 0) {

                        Element publicationReference = (Element) publicationRefereceE.item(0);
                        Element elBiblio = (Element) elPatent.getElementsByTagName("bibliographic-data").item(0);

                        Node documentId = publicationReference.getElementsByTagName("document-id").item(0);
                        if (documentId.getNodeType() == Node.ELEMENT_NODE) {

                            Element el = (Element) documentId;

                            String docNumberString = el.getElementsByTagName("doc-number").item(0).getTextContent();

                            NodeList dateElements = el.getElementsByTagName("date");
                            String date = "";

                            if (dateElements.getLength() > 0) {
                                date = el.getElementsByTagName("date").item(0).getTextContent();
                            }

                            if (set.contains(docNumberString) || set2000.contains(docNumberString)) {

                                duplicates++;

                            } else {

                                if (date.equals("") || date.startsWith("20"))
                                {
                                    set.add(docNumberString);

                                /*
                                StringBuilder newXmlContent = new StringBuilder("<il-patents>\n");

                                Node node = publicationReferences.item(i);
                                Node parentNode = node.getParentNode().getParentNode();
                                StringWriter sw = new StringWriter();
                                Transformer t = TransformerFactory.newInstance().newTransformer();
                                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                t.setOutputProperty(OutputKeys.INDENT, "yes");
                                t.transform(new DOMSource(parentNode), new StreamResult(sw));

                                newXmlContent.append(sw.toString());
                                newXmlContent.append("</il-patents>");
                                String newXmlContentString = newXmlContent.toString();

                                // Convert xml to json.
                                JSONObject json = XML.toJSONObject(newXmlContentString);
                                String jsonString = json.toString(4);

                                DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                collection.insert(dbObject);
                                 */

                                    // patent_id ; title ; date ; kind ; country    -  patents
                                    // id_patent ; section ; class ; subclass    - classification
                                    // id_patent ; language     - languages
                                    // id_patent ; inventor     - inventors
                                    // id_patent ; applicant    - applicants

                                    try {
                                        PreparedStatement preparedStmt = null;

                                        // Patents
                                        String strPatentId = "IL" + docNumberString;

                                        String title = "";
                                        NodeList inventionTitles = elBiblio.getElementsByTagName("invention-title");
                                        for (int k = 0; k < inventionTitles.getLength(); k++) {

                                            Element inventionTitle = (Element) inventionTitles.item(k);

                                            if (inventionTitle.getAttribute("lang").equalsIgnoreCase("en")) {

                                                title = inventionTitle.getTextContent();
                                                break;
                                            }
                                        }

                                        if (title.length() > 300) title = title.substring(0, 300);

                                        String kind = elPatent.getAttribute("kind");
                                        String country = "IL";

                                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                        java.util.Date utilDate = format.parse(date);
                                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                        String language = elBiblio.getElementsByTagName("language-of-publication").item(0).getTextContent().toUpperCase();

                                        preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                        preparedStmt.setString(1, strPatentId);
                                        preparedStmt.setString(2, title);
                                        preparedStmt.setDate(3, sqlDate);
                                        preparedStmt.setString(4, kind);
                                        preparedStmt.setString(5, country);
                                        preparedStmt.setString(6, language);
                                        preparedStmt.execute();

                                        Long patentId = 0L;
                                        preparedStmt = conn.prepareStatement("select last_insert_id()");
                                        ResultSet rSet = preparedStmt.executeQuery();

                                        while (rSet.next()) {

                                            patentId = rSet.getLong(1);
                                        }

                                        // Inventors
                                        Element parties = (Element) elBiblio.getElementsByTagName("parties").item(0);
                                        Element elApplicants = (Element) parties.getElementsByTagName("applicants").item(0);

                                        if (elApplicants != null) {

                                            NodeList lApplicants = elApplicants.getElementsByTagName("applicant");

                                            for (int k = 0; k < lApplicants.getLength(); k++) {

                                                Element elApplicant = (Element) lApplicants.item(k);
                                                Element addressbook = (Element) elApplicant.getElementsByTagName("addressbook").item(0);

                                                NodeList nameList = addressbook.getElementsByTagName("name");

                                                if (nameList != null && nameList.getLength() > 0) {

                                                    String name = addressbook.getElementsByTagName("name").item(0).getTextContent();

                                                    if (name.length() > 100) {
                                                        name = name.substring(0, 100);
                                                    }

                                                    preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                    preparedStmt.setLong(1, patentId);
                                                    preparedStmt.setString(2, name);
                                                    preparedStmt.execute();
                                                }


                                            }
                                        }

                                    } catch (Exception e) {

                                        e.printStackTrace();
                                        exit(1);
                                    }
                                }
                                else set2000.add(docNumberString);
                            }
                        }
                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }



        }

        System.out.println("Izrael");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPatenty před 2000: " + set2000.size());
        System.out.println("\tDuplikáty: " + (duplicates / 2) + " - celková hodnota vydělena 2 - XML dokumenty obsahují číslo publikace a žádosti");
        System.out.println("\n");
    }

    private static void convertAnglie() throws IOException {
        long start = System.currentTimeMillis();

        long duplicates = 0;
        HashSet<String> set2000 = new HashSet<String>();
        HashSet<String> set = new HashSet<String>();

        File dir = new File(inputFolder + "Anglie");

        File[] years = dir.listFiles();

        List<String> wrongFilenames = new ArrayList<>();
        String currentFilenames = "";

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

        for (int monn = years.length - 1; monn >= 0; monn-- ) {

            File year = years[monn];
            File[] months = year.listFiles();

            for (File month : months) {

                File[] xmls = month.listFiles();

                for (File xml : xmls) {

                    StringBuilder newXmlContent = new StringBuilder("<Cases>\n");

                    currentFilenames = xml.getName();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    try {

                        // optional, but recommended
                        // process XML securely, avoid attacks like XML External Entities (XXE)
                        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

                        // parse XML file
                        DocumentBuilder db = dbf.newDocumentBuilder();

                        Document doc = db.parse(xml);
                        doc.getDocumentElement().normalize();

                        Element patentsGranted = null;

                        NodeList sections = doc.getElementsByTagName("Section");

                        for (int i = 0; i < sections.getLength(); i++) {

                            Element el = (Element) sections.item(i);

                            if (el.getAttribute("InternalName").equals("UkPatentsGranted")) {

                                patentsGranted = el;

                            }
                        }

                        NodeList patents = patentsGranted.getElementsByTagName("Case");

                        for (int i = 0; i < patents.getLength(); i++) {

                            Element caseEl = (Element) patents.item(i);

                            String publicationNo = caseEl.getElementsByTagName("PublicationNo").item(0).getTextContent();
                            String date = caseEl.getElementsByTagName("DateFiled").item(0).getTextContent();

                            if (set.contains(publicationNo) || set2000.contains(publicationNo)) {

                                duplicates++;

                            } else {

                                if (!date.equals("")) {

                                    try {
                                        date = date.split(" ")[2];
                                    }
                                    catch (Exception e) {

                                    }
                                }

                                if (date.equals("") || date.startsWith("20")) {

                                    set.add(publicationNo);

                                    String name = "";

                                    try {
                                        // patent_id ; title ; date ; kind ; country    -  patents
                                        // id_patent ; section ; class ; subclass    - classification
                                        // id_patent ; language     - languages
                                        // id_patent ; inventor     - inventors
                                        // id_patent ; applicant    - applicants

                                        PreparedStatement preparedStmt = null;

                                        // Patents
                                        String title = caseEl.getElementsByTagName("GrantTitle").item(0).getTextContent();
                                        date = caseEl.getElementsByTagName("DateFiled").item(0).getTextContent();
                                        String kind = "-";
                                        String country = "UK";

                                        SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
                                        java.util.Date utilDate = format.parse(date);
                                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                        String language = "EN";

                                        if (title.length() > 300) title = title.substring(0, 300);

                                        preparedStmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                                        preparedStmt.setString(1, publicationNo);
                                        preparedStmt.setString(2, title);
                                        preparedStmt.setDate(3, sqlDate);
                                        preparedStmt.setString(4, kind);
                                        preparedStmt.setString(5, country);
                                        preparedStmt.setString(6, language);
                                        preparedStmt.execute();

                                        Long patentId = 0L;
                                        preparedStmt = conn.prepareStatement("select last_insert_id()");
                                        ResultSet rSet = preparedStmt.executeQuery();

                                        while (rSet.next()) {

                                            patentId = rSet.getLong(1);
                                        }

                                        // Classification
                                        Element elClassifications = (Element) caseEl.getElementsByTagName("IpcClassifications").item(0);

                                        if (elClassifications != null) {
                                            NodeList lClassifications = elClassifications.getElementsByTagName("IpcClassification");

                                            List<String> classis = new ArrayList<>();

                                            for (int j = 0; j < lClassifications.getLength(); j++) {

                                                Element elClass = (Element) lClassifications.item(j);
                                                String fullClassification = elClass.getAttribute("SubClass");
                                                String section = fullClassification.substring(0, 1);
                                                String sClass = fullClassification.substring(1, 3);
                                                String subClass = fullClassification.substring(3, 4);

                                                if (classis.contains(fullClassification)) continue;

                                                classis.add(fullClassification);

                                                preparedStmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                                                preparedStmt.setLong(1, patentId);
                                                preparedStmt.setString(2, section);
                                                preparedStmt.setString(3, sClass);
                                                preparedStmt.setString(4, subClass);
                                                preparedStmt.execute();
                                            }
                                        }

                                        // Inventors
                                        Element elInventors = (Element) caseEl.getElementsByTagName("Inventors").item(0);

                                        if (elInventors != null) {

                                            NodeList lInventors = elInventors.getElementsByTagName("Inventor");

                                            for (int j = 0; j < lInventors.getLength(); j++) {

                                                Element elInventor = (Element) lInventors.item(j);
                                                String firstName = elInventor.getElementsByTagName("FirstName").item(0).getTextContent();
                                                String lastName = elInventor.getElementsByTagName("LastName").item(0).getTextContent();

                                                name = lastName;

                                                if (!firstName.equals("")) {
                                                    name += " " + firstName;
                                                }

                                                if (name.length() > 100) {
                                                    name = name.substring(0, 100);
                                                }

                                                preparedStmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                                                preparedStmt.setLong(1, patentId);
                                                preparedStmt.setString(2, name);
                                                preparedStmt.execute();
                                            }
                                        }

                                        // Applicants
                                        Element elApplicants = (Element) caseEl.getElementsByTagName("Applicants").item(0);

                                        if (elApplicants != null) {

                                            NodeList lApplicants = elApplicants.getElementsByTagName("Applicant");

                                            for (int j = 0; j < lApplicants.getLength(); j++) {

                                                Element elApplicant = (Element) lApplicants.item(j);
                                                String firstName = elApplicant.getElementsByTagName("FirstName").item(0).getTextContent();
                                                String lastName = elApplicant.getElementsByTagName("LastName").item(0).getTextContent();

                                                name = lastName;

                                                if (!firstName.equals("")) {
                                                    name += " " + firstName;
                                                }

                                                if (name.length() > 100) {
                                                    name = name.substring(0, 100);
                                                }

                                                preparedStmt = conn.prepareStatement("insert into applicants(id_patent, applicant) values (?, ?)");
                                                preparedStmt.setLong(1, patentId);
                                                preparedStmt.setString(2, name);
                                                preparedStmt.execute();
                                            }
                                        }
                                    } catch (Exception e) {

                                        e.printStackTrace();
                                        System.out.println("Name:" + name);
                                        exit(1);
                                    }

                                    /*
                                    // Convert "Case" to xml string.
                                    StringWriter sw = new StringWriter();
                                    Transformer t = TransformerFactory.newInstance().newTransformer();
                                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                                    t.transform(new DOMSource(patents.item(i)), new StreamResult(sw));
                                    newXmlContent.append(sw.toString());
                                     */
                                }
                                else {

                                    set2000.add(publicationNo);
                                }
                            }

                        }

                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                        wrongFilenames.add(currentFilenames);
                    }

                    /*
                    newXmlContent.append("</Cases>");
                    String newXmlContentString = newXmlContent.toString();

                    // Convert xml to json.
                    JSONObject json = XML.toJSONObject(newXmlContentString);
                    String jsonString = json.toString(4);

                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                    collection.insert(dbObject);
                     */
                }
            }
        }
        System.out.println("Anglie");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPatenty před 2000: " + set2000.size());
        System.out.println("\tDuplikáty: " + duplicates);
        System.out.println("\n");
    }

    public static void createConnection() {

        try {
            System.out.println("Creating connection");
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:6033/patents", "root", "password");
            if (conn != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {

        System.out.println("Closing connection");
        try {
            if (conn != null)
            conn.close();
            System.out.println("Connection closed");
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }
    }
}

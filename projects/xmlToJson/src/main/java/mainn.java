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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class mainn {

    static final String inputFolder = "D:\\PATENTY\\2.Data\\";
    static final String outputFolder = "D:\\PATENTY\\3.DataJSON\\";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        System.out.println("Anglie");
        //convertAnglie();

        System.out.println("\n\nIsrael");
        convertIsrael();

        System.out.println("\n\nLitva");
        //convertLitva();

        System.out.println("\n\nPeru");
        //convertPeru();

        System.out.println("\n\nRusko");
        //convertRusko();

        System.out.println("\n\nŠpanělsko");
        // TODO
        //convertSpanelsko();

        System.out.println("\n\nKanada");
        //convertCanada();

        System.out.println("\n\nFrancie");
        //convertFrancie();
    }

    private static void convertRusko() throws FileNotFoundException {

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

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

                        if (date.equals("") || date.startsWith("20")) {

                            if (author.equals("") || title.equals("")) {
                                testt++;
                            }
                            else {
                                set.add(id);

                                StringBuilder sb = new StringBuilder();
                                sb.append("{\n");

                                int fix = headers.size() > s.length ? 1 : 0;

                                for (int i = 0; i < headers.size(); i++) {

                                    if (fix == 1 && i > 20) {

                                        String text = s[i - fix];
                                        text = text.replaceAll("\"", "");
                                        text = text.replaceAll("\\r\\n", "");

                                        sb.append("\"" + headers.get(i) + "\": " + "\"" + text + "\"");
                                    }
                                    else {

                                        String text = s[i];
                                        text = text.replaceAll("\"", "");
                                        text = text.replaceAll("\\r\\n", "");

                                        sb.append("\"" + headers.get(i) + "\": " + "\"" + text + "\"");
                                    }

                                    if (i < (headers.size() - 1)) {

                                        sb.append(",\n");
                                    }
                                }

                                sb.append("\n}");

                                String jsonString = sb.toString();

                                try {
                                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                    collection.insert(dbObject);
                                }
                                catch (Exception e) {
                                    System.out.println("kokot");
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

            boolean skipFirstRow = false;
            boolean secondRow = false;

            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            DB database = mongoClient.getDB("patents");
            DBCollection collection = database.getCollection("test");

            StringBuilder json = new StringBuilder("{\"Cases\": [\n");

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

            for (long j = 0; j < values.size(); j++) {

                List<String> vals = values.get(j);
                json.append("{");
                for (int i = 0; i < columns.size(); i++) {

                    try {
                        json.append("\"").append(columns.get(i)).append("\": \"").append(vals.get(i)).append("\"");
                    }
                    catch
                    (Exception e) {

                    }
                    if (i < columns.size() - 1) {

                        json.append(",\n");
                    }
                }
                json.append("\n},\n");
            }

            json.append("\n]}");

            String jsonString = json.toString();
            DBObject dbObject = (DBObject) JSON.parse(jsonString);
            collection.insert(dbObject);
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

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

        List<Exception> exceptions = new ArrayList<>();

        for (int monn = years.length - 1; monn >= 0; monn-- ) {

            File year = years[monn];
            File[] folders = year.listFiles();
            System.out.println(year.getAbsolutePath());

            for (File month : folders) {

                File[] filess = month.listFiles();


                for (File file : filess) {

                    // Old structure
                    if (file.isFile()) {

                        // TODO: count xmls
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
                                StringWriter sw = new StringWriter();
                                Transformer t = TransformerFactory.newInstance().newTransformer();
                                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                t.setOutputProperty(OutputKeys.INDENT, "yes");
                                t.transform(new DOMSource(doc), new StreamResult(sw));

                                newXmlContentString = sw.toString();

                                // Convert xml to json.
                                JSONObject json = XML.toJSONObject(newXmlContentString);
                                String jsonString = json.toString(4);

                                DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                collection.insert(dbObject);
                            } catch (Exception e) {

                                exceptions.add(e);
                                e.printStackTrace();
                            }
                        }
                    }
                    // New structure
                    else {

                        File[] xmls = file.listFiles();

                        for (File xml : xmls) {

                            // TODO: count xmls
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

                                    StringWriter sw = new StringWriter();
                                    Transformer t = TransformerFactory.newInstance().newTransformer();
                                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                                    t.transform(new DOMSource(doc), new StreamResult(sw));

                                    newXmlContentString = sw.toString();

                                    // Convert xml to json.
                                    JSONObject json = XML.toJSONObject(newXmlContentString);
                                    String jsonString = json.toString(4);

                                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                    collection.insert(dbObject);
                                } catch (Exception e) {

                                    exceptions.add(e);
                                    e.printStackTrace();
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

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

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

                for (File file : filess) {

                    File[] docFiles = file.listFiles();

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

                                        // TODO
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

                                        String newXmlContentString = "";

                                        try {
                                            StringWriter sw = new StringWriter();
                                            Transformer t = TransformerFactory.newInstance().newTransformer();
                                            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                            t.setOutputProperty(OutputKeys.INDENT, "yes");
                                            t.transform(new DOMSource(doc), new StreamResult(sw));

                                            newXmlContentString = sw.toString();
                                            JSONObject json = XML.toJSONObject(newXmlContentString);
                                            String jsonString = json.toString(4);

                                            DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                            collection.insert(dbObject);
                                        } catch (Exception e) {

                                            exceptions.add(e);
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

                                            // TODO
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

                                                String newXmlContentString = "";

                                                try {
                                                    StringWriter sw = new StringWriter();
                                                    Transformer t = TransformerFactory.newInstance().newTransformer();
                                                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                                                    t.transform(new DOMSource(doc), new StreamResult(sw));

                                                    newXmlContentString = sw.toString();
                                                    // Convert xml to json.
                                                    JSONObject json = XML.toJSONObject(newXmlContentString);
                                                    String jsonString = json.toString(4);

                                                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                                    collection.insert(dbObject);
                                                } catch (Exception e) {

                                                    exceptions.add(e);
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

                                        // TODO

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

                                            String newXmlContentString = "";

                                            try {
                                                StringWriter sw = new StringWriter();
                                                Transformer t = TransformerFactory.newInstance().newTransformer();
                                                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                                t.setOutputProperty(OutputKeys.INDENT, "yes");
                                                t.transform(new DOMSource(doc), new StreamResult(sw));

                                                newXmlContentString = sw.toString();
                                                // Convert xml to json.
                                                JSONObject json = XML.toJSONObject(newXmlContentString);
                                                String jsonString = json.toString(4);

                                                DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                                collection.insert(dbObject);
                                            } catch (Exception e) {

                                                exceptions.add(e);
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

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

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

                            try {
                                dateString = el.getElementsByTagName("date").item(0).getTextContent();
                            } catch (Exception e) {

                                try {
                                    dateString = doc.getElementsByTagName("eappldate").item(0).getTextContent();
                                } catch (Exception ee) {

                                }
                            }

                            if (dateString.length() > 0) {

                                dateString = dateString.substring(dateString.lastIndexOf(".") + 1);
                            }

                            if (!dateString.startsWith("20") && !dateString.equals("")) {
                                set2000.add(xmlName);
                                continue;
                            }

                            set.add(xmlName);

                            String newXmlContentString = "";

                            try {
                                StringWriter sw = new StringWriter();
                                Transformer t = TransformerFactory.newInstance().newTransformer();
                                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                t.setOutputProperty(OutputKeys.INDENT, "yes");
                                t.transform(new DOMSource(doc), new StreamResult(sw));

                                newXmlContentString = sw.toString();

                                // Convert xml to json.
                                JSONObject json = XML.toJSONObject(newXmlContentString);
                                String jsonString = json.toString(4);

                                DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                collection.insert(dbObject);
                            } catch (Exception e) {

                                exceptions.add(e);
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

                    if (xml.getAbsolutePath().contains("TOC")) continue;

                    if (set.contains(substr)) continue;

                    set.add(substr);

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

                NodeList publicationReferences = doc.getElementsByTagName("publication-reference");

                for (int i = 0; i < publicationReferences.getLength(); i++) {

                    Element publicationReference = (Element) publicationReferences.item(i);

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
                            }
                            else set2000.add(docNumberString);
                        }
                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
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

                                    // Convert "Case" to xml string.
                                    StringWriter sw = new StringWriter();
                                    Transformer t = TransformerFactory.newInstance().newTransformer();
                                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                                    t.transform(new DOMSource(patents.item(i)), new StreamResult(sw));
                                    newXmlContent.append(sw.toString());
                                }
                                else {

                                    set2000.add(publicationNo);
                                }
                            }

                        }

                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                        wrongFilenames.add(currentFilenames);
                    } catch (TransformerConfigurationException e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }

                    newXmlContent.append("</Cases>");
                    String newXmlContentString = newXmlContent.toString();

                    // Convert xml to json.
                    JSONObject json = XML.toJSONObject(newXmlContentString);
                    String jsonString = json.toString(4);

                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                    collection.insert(dbObject);
                }
            }
        }
        System.out.println("Anglie");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPatenty před 2000: " + set2000.size());
        System.out.println("\tDuplikáty: " + duplicates);
        System.out.println("\n");
    }
}

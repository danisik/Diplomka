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

import static java.lang.System.exit;

public class mainn {

    static final String inputFolder = "D:\\PATENTY\\2.Data\\";
    static final String outputFolder = "D:\\PATENTY\\3.DataJSON\\";

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {

        System.out.println("\n\nLitva");
        // TODO: HOTOVO OSETRENI
        //convertLitva();

        System.out.println("Portugal");
        // TODO: HOTOVO OSETRENI
        //convertPortugal();

        System.out.println("Anglie");
        // TODO: HOTOVO OSETRENI
        //convertAnglie();

        System.out.println("\n\nIsrael");
        // TODO: HOTOVO OSETRENI
        //convertIsrael();

        System.out.println("\n\nPeru");
        // TODO: HOTOVO OSETRENI
        //convertPeru();

        System.out.println("\n\nRusko");
        // TODO: HOTOVO OSETRENI
        //convertRusko();

        System.out.println("\n\nŠpanělsko");
        // TODO: HOTOVO OSETRENI
        //convertSpanelsko();

        System.out.println("\n\nFrancie");
        // TODO: HOTOVO OSETRENI
        //convertFrancie();

        System.out.println("\n\nKanada");
        // TODO: HOTOVO OSETRENI
        //convertCanada();
    }


    private static void convertPortugal() throws FileNotFoundException {

        String name = "";
        try {

            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            DB database = mongoClient.getDB("patents");
            DBCollection collection = database.getCollection("patents");

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

                StringWriter sw = new StringWriter();
                Transformer t = TransformerFactory.newInstance().newTransformer();
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.transform(new DOMSource(doc), new StreamResult(sw));

                String newXmlContentString = sw.toString();

                newXmlContentString = newXmlContentString.replace("m:type=\"Edm.DateTime\"", "");
                newXmlContentString = newXmlContentString.replace("m:type=\"Edm.Guid\"", "");
                newXmlContentString = newXmlContentString.replace("m:type=\"Edm.Int32\"", "");
                newXmlContentString = newXmlContentString.replace("type=\"application/xml\"", "");
                newXmlContentString = newXmlContentString.replace("type=\"text\"", "");

                // Convert xml to json.
                JSONObject json = XML.toJSONObject(newXmlContentString, true);
                String jsonString = json.toString(4);

                DBObject dbObject = (DBObject) JSON.parse(jsonString);
                collection.insert(dbObject);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void convertRusko() throws FileNotFoundException {

        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        DB database = mongoClient.getDB("patents");
        DBCollection collection = database.getCollection("patents");

        long start = System.currentTimeMillis();

        File dir = new File(inputFolder + "Rusko");

        long duplicates = 0;
        long countt = 0;
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

                                if (title.equals("")) continue;
                                if (date.equals("")) continue;
                                if (author.equals("")) continue;
                                if (id.equals("")) continue;

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
                                    countt++;
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
        System.out.println("\tPatenty: " + countt);
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

            long patentsCount = 0;
            List<String> patentIdss = new ArrayList<>();

            List<String> columns = new ArrayList<>();
            Map<Long, List<String>> values = new HashMap();

            int count = 0;

            boolean skipFirstRow = false;
            boolean secondRow = false;

            MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
            DB database = mongoClient.getDB("patents");
            DBCollection collection = database.getCollection("patents");

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

            for (Long key : values.keySet()) {

                // Patents
                String sPatentId = "";
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

                            if (listClass.contains(full)) continue;
                            ;
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

                if (sPatentId.equals("")) {

                    continue;
                }

                if (title.equals("")) {

                    continue;
                }

                if (date.equals("")) {

                    continue;
                }

                if (iNames.size() == 0) {

                    continue;
                } else {
                    List<String> nanames = new ArrayList<>();
                    for (String name : iNames) {

                        if (name.equals("")) continue;
                        nanames.add(name);
                    }

                    if (nanames.size() == 0) {

                        continue;
                    }
                }

                patentsCount++;

                StringBuilder json = new StringBuilder("{\"Cases\": [\n");
                List<String> vals = values.get(key);
                json.append("{");
                for (int i = 0; i < columns.size(); i++) {

                    try {
                        json.append("\"").append(columns.get(i).replaceAll("\\.", "")).append("\": \"").append(vals.get(i)).append("\"");
                    }
                    catch
                    (Exception e) {

                    }
                    if (i < columns.size() - 1) {

                        json.append(",\n");
                    }
                }
                json.append("\n}");
                json.append("\n]}");

                String jsonString = json.toString();
                jsonString = jsonString.replaceAll("\"Y\" TIPO MOLINETE", "Y TIPO MOLINETE");
                jsonString = jsonString.replaceAll("COOPERATIVA INDUSTRIAL\"MANUFACTURAS DEL CENTRO LTDA.; ", "COOPERATIVA INDUSTRIAL MANUFACTURAS DEL CENTRO LTDA.; ");

                DBObject dbObject = (DBObject) JSON.parse(jsonString);
                collection.insert(dbObject);
            }

            System.out.println(patentsCount);


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

        long counterr = 0;

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

                            if (dateString.equals("")) continue;

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

                                boolean inventorTRue = false;

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

                                                if (name.equals("")) continue;
                                                inventorTRue = true;
                                            }
                                        }
                                    }
                                }

                                if (!inventorTRue) continue;

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
                                counterr++;

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
                                if (dateString.equals("")) continue;

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

                                    boolean invetorTrue = false;

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

                                                    if (name.equals("")) continue;
                                                    invetorTrue = true;
                                                }
                                            }
                                        }
                                    }

                                    if (!invetorTrue) continue;

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
                                    counterr++;
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
        System.out.println("\tPatenty: " + counterr);
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

        long counterr = 0;

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

                                        if (dateString.equals("")) continue;

                                        set.add(filename);

                                        String newXmlContentString = "";

                                        try {

                                            Element elBiblio = (Element) doc.getElementsByTagName("fr-bibliographic-data").item(0);

                                            if (elBiblio != null) {

                                                // Patents
                                                String country = "";

                                                if (el.getElementsByTagName("country") == null || el.getElementsByTagName("country").item(0) == null)
                                                    country = "FR";
                                                else
                                                    country = el.getElementsByTagName("country").item(0).getTextContent();

                                                String title = "";
                                                if (elBiblio.getElementsByTagName("invention-title") == null || elBiblio.getElementsByTagName("invention-title").item(0) == null) {
                                                    continue;
                                                } else
                                                    title = elBiblio.getElementsByTagName("invention-title").item(0).getTextContent();

                                                String sPatentId = "";
                                                if (el.getElementsByTagName("doc-number") == null || el.getElementsByTagName("doc-number").item(0) == null) {
                                                    continue;
                                                } else
                                                    sPatentId = country + el.getElementsByTagName("doc-number").item(0).getTextContent();

                                                String date = "";
                                                if (el.getElementsByTagName("date") == null || el.getElementsByTagName("date").item(0) == null) {
                                                    continue;
                                                } else date = el.getElementsByTagName("date").item(0).getTextContent();

                                                String kind = "";
                                                if (el.getElementsByTagName("kind") == null || el.getElementsByTagName("kind").item(0) == null)
                                                    kind = "-";
                                                else
                                                    kind = el.getElementsByTagName("kind").item(0).getTextContent().substring(0, 1);

                                                String language = "FR";

                                                if (title == null || title.equals("")) continue;

                                                if (title.length() > 300) title = title.substring(0, 300);

                                                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                                java.util.Date utilDate = format.parse(date);
                                                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                                // Inventors
                                                Element elParties = (Element) elBiblio.getElementsByTagName("parties").item(0);
                                                List<String> names = new ArrayList<>();
                                                List<String> aNames = new ArrayList<>();

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

                                                                names.add(name);
                                                            }
                                                        }

                                                        if (inventors.getLength() == 0 || names.size() == 0) {
                                                            continue;
                                                        }
                                                    } else {
                                                        continue;
                                                    }
                                                } else {
                                                    continue;
                                                }

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
                                                counterr++;
                                            }
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

                                                if (dateString.equals("")) continue;

                                                set.add(filename);

                                                String newXmlContentString = "";

                                                try {

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
                                                                    if (elPubliDocId.getElementsByTagName("doc-number") == null || elPubliDocId.getElementsByTagName("doc-number").item(0) == null) {
                                                                        continue;
                                                                    }
                                                                    else sPatentId = elPubliDocId.getElementsByTagName("doc-number").item(0).getTextContent();

                                                                    Element elKind = (Element) elPubliDocId.getElementsByTagName("kind").item(0);
                                                                    String kind = "-";
                                                                    if (elKind != null)
                                                                        kind = elKind.getTextContent().substring(0, 1);

                                                                    Element elDate = (Element) elPubliDocId.getElementsByTagName("date").item(0);
                                                                    String date = "";
                                                                    if (elDate != null)
                                                                        date = elDate.getTextContent();
                                                                    else {
                                                                        continue;
                                                                    }

                                                                    if (date.equals("")) {
                                                                        continue;
                                                                    }

                                                                    String title = "";

                                                                    if (elBiblio.getElementsByTagName("invention-title") == null || elBiblio.getElementsByTagName("invention-title").item(0) == null) {
                                                                        continue;
                                                                    }
                                                                    else title = elBiblio.getElementsByTagName("invention-title").item(0).getTextContent();

                                                                    if (title == null || title.equals("")) {
                                                                        continue;
                                                                    }

                                                                    if (title.length() > 300)
                                                                        title = title.substring(0, 300);

                                                                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
                                                                    java.util.Date utilDate = format.parse(date);
                                                                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                                                                    String language = elBiblio.getAttribute("lang").toUpperCase();

                                                                    // Inventors
                                                                    Element elParties = (Element) elBiblio.getElementsByTagName("parties").item(0);
                                                                    Element elInventors = (Element) elBiblio.getElementsByTagName("fr-owners").item(0);

                                                                    List<String> names = new ArrayList<>();
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

                                                                            names.add(name);

                                                                        }

                                                                        if (inventors.getLength() == 0 || names.size() == 0) {
                                                                            continue;
                                                                        }
                                                                    }
                                                                    else {
                                                                        continue;
                                                                    }

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
                                                                    // TODO
                                                                    collection.insert(dbObject);
                                                                    counterr++;
                                                                }
                                                                else {
                                                                    continue;
                                                                }
                                                            }
                                                            else {
                                                                continue;
                                                            }
                                                        }
                                                        else {
                                                            continue;
                                                        }
                                                    }
                                                    else {
                                                        continue;
                                                    }


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

                                            if (dateString.equals("")) continue;

                                            set.add(filename);

                                            String newXmlContentString = "";

                                            try {

                                                Element elBiblio = (Element) doc.getElementsByTagName("fr-bibliographic-data").item(0);

                                                if (elBiblio != null) {

                                                    // Patents
                                                    String country = "";

                                                    if (el.getElementsByTagName("country") == null || el.getElementsByTagName("country").item(0) == null)
                                                        country = "FR";
                                                    else
                                                        country = el.getElementsByTagName("country").item(0).getTextContent();

                                                    String title = "";
                                                    if (elBiblio.getElementsByTagName("invention-title") == null || elBiblio.getElementsByTagName("invention-title").item(0) == null) {
                                                        continue;
                                                    } else
                                                        title = elBiblio.getElementsByTagName("invention-title").item(0).getTextContent();

                                                    String sPatentId = "";
                                                    if (el.getElementsByTagName("doc-number") == null || el.getElementsByTagName("doc-number").item(0) == null) {
                                                        continue;
                                                    } else
                                                        sPatentId = country + el.getElementsByTagName("doc-number").item(0).getTextContent();

                                                    String date = "";
                                                    if (el.getElementsByTagName("date") == null || el.getElementsByTagName("date").item(0) == null) {
                                                        continue;
                                                    } else
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

                                                    List<String> names = new ArrayList<>();
                                                    List<String> aNames = new ArrayList<>();

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

                                                                names.add(name);
                                                            }

                                                            if (inventors.getLength() == 0 || names.size() == 0) {

                                                                continue;
                                                            }
                                                        } else {

                                                            continue;
                                                        }


                                                    } else {
                                                        continue;
                                                    }

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
                                                    // TODO
                                                    collection.insert(dbObject);
                                                    counterr++;
                                                }


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
        System.out.println("\tPatenty: " + counterr);
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

        long counterr = 0;

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

                            String dateDate = "";

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

                                if (sPatentId.equals("") || date.equals("") || title.equals("")) continue;

                                // Inventors
                                Element elPatent =(Element) doc.getElementsByTagName("patent").item(0);
                                if (elPatent == null ) elPatent = (Element) doc.getElementsByTagName("Patent").item(0);
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

                                        if (lastName == null && firstName == null) continue;

                                        if (lastName.getTextContent().equals("") && firstName.getTextContent().equals("")) continue;

                                        inventorAdded = true;
                                    }
                                }

                                if (!inventorAdded) continue;

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
                                counterr++;

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
        System.out.println("\tPatenty: " + counterr);
    }

    public static void convertLitva() throws IOException, ParserConfigurationException, SAXException, TransformerException {

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

                    if (xml.getAbsolutePath().contains("dtd")) continue;

                    if (set.contains(substr)) continue;

                    set.add(substr);

                    Path path = Paths.get(xml.getAbsolutePath());

                    List<String> lines = Files.readAllLines(path);

                    String newXmlContentString = "";

                    for (String line: lines) {

                        newXmlContentString += line + "\n";
                    }

                    // Instantiate the Factory
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", ClassLoader.getSystemClassLoader());

                    // optional, but recommended
                    // process XML securely, avoid attacks like XML External Entities (XXE)
                    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    // Disable external DTDs as well
                    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                    DocumentBuilder db = dbf.newDocumentBuilder();

                    Document doc = db.parse(xml);
                    doc.getDocumentElement().normalize();

                    StringWriter sw = new StringWriter();
                    Transformer t = TransformerFactory.newInstance().newTransformer();
                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                    t.transform(new DOMSource(doc), new StreamResult(sw));

                    newXmlContentString = doc.toString();

                    // Convert xml to json.
                    JSONObject json = XML.toJSONObject(newXmlContentString, true);
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

        int ccount = 0;

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

                                if (date.startsWith("20"))
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

                                        if (strPatentId.equals("")) continue;
                                        if (title.equals("")) continue;
                                        if (date.equals("")) continue;

                                        // Inventors
                                        Element parties = (Element) elBiblio.getElementsByTagName("parties").item(0);
                                        Element elApplicants = (Element) parties.getElementsByTagName("applicants").item(0);

                                        boolean noAuthor = true;

                                        if (elApplicants != null) {

                                            NodeList lApplicants = elApplicants.getElementsByTagName("applicant");

                                            for (int k = 0; k < lApplicants.getLength(); k++) {

                                                Element elApplicant = (Element) lApplicants.item(k);
                                                Element addressbook = (Element) elApplicant.getElementsByTagName("addressbook").item(0);

                                                NodeList nameList = addressbook.getElementsByTagName("name");

                                                if (nameList != null && nameList.getLength() > 0) {

                                                    String name = addressbook.getElementsByTagName("name").item(0).getTextContent();

                                                    if (name.equals("")) continue;

                                                    noAuthor = false;
                                                }


                                            }
                                        }

                                        if (noAuthor == true) continue;

                                        Node parentNode = publicationReference.getParentNode().getParentNode();
                                        StringWriter sw = new StringWriter();
                                        Transformer t = TransformerFactory.newInstance().newTransformer();
                                        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                        t.setOutputProperty(OutputKeys.INDENT, "yes");
                                        t.transform(new DOMSource(parentNode), new StreamResult(sw));

                                        String newXmlContentString = sw.toString();
                                        ccount++;

                                        // Convert xml to json.
                                        JSONObject json = XML.toJSONObject(newXmlContentString);
                                        String jsonString = json.toString(4);

                                        DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                        collection.insert(dbObject);

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
        System.out.println("\tPatenty: " + ccount);
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

                            String title = caseEl.getElementsByTagName("GrantTitle").item(0).getTextContent();
                            if (title.equals("")) continue;
                            if (publicationNo.equals("")) continue;

                            // Inventors
                            Element elInventors = (Element) caseEl.getElementsByTagName("Inventors").item(0);

                            if (elInventors == null) continue;

                            boolean noAuthor = true;

                            if (elInventors != null) {

                                NodeList lInventors = elInventors.getElementsByTagName("Inventor");

                                if (lInventors == null || lInventors.getLength() == 0) continue;

                                for (int j = 0; j < lInventors.getLength(); j++) {

                                    Element elInventor = (Element) lInventors.item(j);

                                    if (elInventor.getElementsByTagName("FirstName") == null || elInventor.getElementsByTagName("FirstName").item(0) == null) continue;
                                    if (elInventor.getElementsByTagName("LastName") == null || elInventor.getElementsByTagName("LastName").item(0) == null) continue;

                                    String firstName = elInventor.getElementsByTagName("FirstName").item(0).getTextContent();
                                    String lastName = elInventor.getElementsByTagName("LastName").item(0).getTextContent();

                                    if (firstName.equals("") && lastName.equals("")) continue;

                                    noAuthor = false;
                                }

                                if (noAuthor == true) continue;
                            }

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

                                if (date.startsWith("20")) {

                                    set.add(publicationNo);

                                    // Convert "Case" to xml string.
                                    StringWriter sw = new StringWriter();
                                    Transformer t = TransformerFactory.newInstance().newTransformer();
                                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                    t.setOutputProperty(OutputKeys.INDENT, "yes");
                                    t.transform(new DOMSource(patents.item(i)), new StreamResult(sw));

                                    String newXmlContentString = sw.toString();

                                    // Convert xml to json.
                                    JSONObject json = XML.toJSONObject(newXmlContentString, true);
                                    String jsonString = json.toString(4);

                                    DBObject dbObject = (DBObject) JSON.parse(jsonString);
                                    collection.insert(dbObject);
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

                    /*
                    newXmlContent.append("</Cases>");
                    String newXmlContentString = newXmlContent.toString();

                    // Convert xml to json.
                    JSONObject json = XML.toJSONObject(newXmlContentString, true);
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
}

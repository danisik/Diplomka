import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.lang.model.util.Elements;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class mainn {

    static final String folder = "D:\\PATENTY\\2. Data\\";

    public static void main(String[] args) throws IOException {

        // Litva
        //printCount(folder + "Litva", "Litva");

        // Španělsko
        //printCount(folder + "Spanelsko", "Španělsko");

        // Francie
        //printCount(folder + "Francie\\patents", "Francie");

        // Kanada
        //loadXMLsCanada();

        // Israel
        //loadXMLsIsrael();

        // Anglie
        //loadXMLsAnglie();
        //anglieUTF16toUTF8();

        // Peru

        // Rusko
        loadCSVRusko();

        // Mexiko

    }

    private static void loadCSVRusko() throws FileNotFoundException {

        long start = System.currentTimeMillis();

        File dir = new File(folder + "Rusko");

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

            try {

                reader = new CSVReaderBuilder(
                    new FileReader(csv.getAbsolutePath()))
                    .withCSVParser(csvParser)   // custom CSV parser
                    .withSkipLines(1)           // skip the first line, header info
                    .build();
                //r = reader.readAll();

                String[] s = null;

                while (true) {

                    s = reader.readNext();

                    if (s == null) break;

                    if (s.length <= 1) {

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

                            if (author.equals("") || title.equals("")) testt++;
                            set.add(id);
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

    private static void anglieUTF16toUTF8() throws IOException {

        String[] arr = {"6393.xml",
                "6392.xml",
                "6394.xml",
                "6395.xml",
                "6396.xml",
                "6397.xml",
                "6398.xml",
                "6399.xml",
                "6400.xml",
                "6401.xml",
                "6437.xml",
                "6438.xml",
                "6439.xml",
                "6440.xml",
                "6441.xml",
                "6442.xml",
                "6443.xml",
                "6444.xml",
                "6445.xml",
                "6446.xml",
                "6447.xml",
                "6448.xml",
                "6449.xml",
                "6402.xml",
                "6403.xml",
                "6404.xml",
                "6405.xml",
                "6406.xml",
                "6407.xml",
                "6408.xml",
                "6409.xml",
                "6410.xml",
                "6411.xml",
                "6412.xml",
                "6413.xml",
                "6414.xml",
                "6415.xml",
                "6416.xml",
                "6417.xml",
                "6418.xml",
                "6419.xml",
                "6420.xml",
                "6421.xml",
                "6422.xml",
                "6423.xml",
                "6424.xml",
                "6425.xml",
                "6426.xml",
                "6427.xml",
                "6428.xml",
                "6429.xml",
                "6430.xml",
                "6431.xml",
                "6432.xml",
                "6433.xml",
                "6434.xml",
                "6435.xml",
                "6436.xml",
                "6450.xml",
                "6451.xml",
                "6452.xml",
                "6453.xml",
                "6454.xml",
                "6489.xml",
                "6490.xml",
                "6491.xml",
                "6492.xml",
                "6493.xml",
                "6494.xml",
                "6495.xml",
                "6496.xml",
                "6497.xml",
                "6498.xml",
                "6455.xml",
                "6456.xml",
                "6457.xml",
                "6458.xml",
                "6459.xml",
                "6460.xml",
                "6461.xml",
                "6462.xml",
                "6463.xml",
                "6464.xml",
                "6465.xml",
                "6466.xml",
                "6467.xml",
                "6468.xml",
                "6469.xml",
                "6470.xml",
                "6471.xml",
                "6472.xml",
                "6473.xml",
                "6474.xml",
                "6475.xml",
                "6476.xml",
                "6477.xml",
                "6478.xml",
                "6479.xml",
                "6480.xml",
                "6481.xml",
                "6482.xml",
                "6483.xml",
                "6484.xml",
                "6485.xml",
                "6486.xml",
                "6487.xml",
                "6488.xml",
                "6502.xml",
                "6503.xml",
                "6504.xml"
        };

        List<String> wrongXMLs = Arrays.asList(arr);

        File dir = new File(folder + "Anglie");

        File[] years = dir.listFiles();

        List<String> wrongFilenames = new ArrayList<>();
        String currentFilenames = "";

        for (File yearDir : years) {

            File[] months = yearDir.listFiles();

            for (File month : months) {

                File[] xmls = month.listFiles();

                for (File xml : xmls) {

                    currentFilenames = xml.getName();

                    if (wrongXMLs.contains(currentFilenames)) {

                        List<String> lines = Files.readAllLines(Paths.get(xml.getAbsolutePath()), StandardCharsets.UTF_8);

                        String tt = lines.get(0);
                        tt = tt.replace("encoding=\"utf-16\"", "encoding=\"utf-8\"");

                        lines.set(0, tt);


                        BufferedWriter writer = new BufferedWriter(new FileWriter(xml.getAbsolutePath()));

                        for (String s : lines) {

                            writer.write(s);
                        }

                        writer.close();
                    }
                }
            }
        }
    }

    private static void loadXMLsAnglie()
    {
        long start = System.currentTimeMillis();

        long duplicates = 0;
        HashSet<String> set2000 = new HashSet<String>();
        HashSet<String> set = new HashSet<String>();

        File dir = new File(folder + "Anglie");

        File[] years = dir.listFiles();

        List<String> wrongFilenames = new ArrayList<>();
        String currentFilenames = "";

        for (File yearDir : years) {

            File[] months = yearDir.listFiles();

            for (File month : months) {

                File[] xmls = month.listFiles();

                for (File xml : xmls) {

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

                                if (date.equals("") || date.startsWith("20")) set.add(publicationNo);
                                else set2000.add(publicationNo);
                            }

                        }

                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        e.printStackTrace();
                        wrongFilenames.add(currentFilenames);
                    }
                }
            }

        }

        long finish = System.currentTimeMillis();
        long timeElapsed = (finish - start) / 1000;

        System.out.println("Anglie");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPatenty před 2000: " + set2000.size());
        System.out.println("\tDuplikáty: " + duplicates);
        System.out.println("\tČas: " + timeElapsed + " s");
        System.out.println("\n");
    }

    private static void loadXMLsCanada() {

        long start = System.currentTimeMillis();

        long duplicates = 0;
        HashSet<String> set = new HashSet<String>();

        File canadaDir = new File(folder + "Kanada");
        File[] years = canadaDir.listFiles();

        for (File yearDir : years) {

            File[] months = yearDir.listFiles();

            for (File month : months) {

                File[] files = month.listFiles();

                for (File file : files) {

                    // Old structure
                    if (file.isFile()) {

                        // TODO: count xmls
                        // CA-BFT-2064388-20190113.xml
                        String filename = file.getName();
                        filename = filename.replace("CA-BFT-", "");
                        filename = filename.substring(0, filename.indexOf("-"));

                        if (set.contains(filename)) {

                            duplicates++;

                        } else {

                            set.add(filename);
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

                            if (set.contains(filename)) {

                                duplicates++;

                            } else {

                                set.add(filename);
                            }
                        }
                    }
                }

            }
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = (finish - start) / 1000;

        System.out.println("Kanada");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tDuplikáty: " + duplicates);
        System.out.println("\tČas: " + timeElapsed + " s");
        System.out.println("\n");
    }

    private static void loadXMLsIsrael() {

        long start = System.currentTimeMillis();

        long duplicates = 0;
        HashSet<String> set2000 = new HashSet<>();
        HashSet<String> set = new HashSet<String>();

        File israelDir = new File(folder + "Izrael");
        File[] xmls = israelDir.listFiles();

        for (File xmlFile : xmls)
        {
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
                NodeList applicationReferences = doc.getElementsByTagName("application-reference");

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

                            if (date.equals("") || date.startsWith("20")) set.add(docNumberString);
                            else set2000.add(docNumberString);
                        }
                    }
                }

                for (int i = 0; i < applicationReferences.getLength(); i++) {

                    Element applicationReference = (Element) applicationReferences.item(i);

                    Node documentId = applicationReference.getElementsByTagName("document-id").item(0);
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

                            if (date.equals("") || date.startsWith("20")) set.add(docNumberString);
                            else set2000.add(docNumberString);
                        }
                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = (finish - start) / 1000;

        System.out.println("Izrael");
        System.out.println("\tPatenty: " + set.size());
        System.out.println("\tPatenty před 2000: " + set2000.size());
        System.out.println("\tDuplikáty: " + (duplicates / 2) + " - celková hodnota vydělena 2 - XML dokumenty obsahují číslo publikace a žádosti");
        System.out.println("\tČas: " + timeElapsed + " s");
        System.out.println("\n");
    }

    private static void printCount(String folder, String zeme) throws IOException {

        long start = System.currentTimeMillis();

        List<String> files;
        try (Stream<Path> walk = Files.walk(Paths.get(folder))) {
            files = walk
                    .filter(p -> !Files.isDirectory(p))
                    // this is a path, not string,
                    // this only test if path end with a certain path
                    //.filter(p -> p.endsWith(fileExtension))
                    // convert path to string first
                    .map(p -> p.getFileName().toString())
                    .filter(f -> f.endsWith("xml"))
                    .collect(Collectors.toList());
        }

        files.removeIf(s -> s.equals("TOC.xml"));

        Set<String> filesSet = new HashSet<>(files);

        long finish = System.currentTimeMillis();
        long timeElapsed = (finish - start) / 1000;

        System.out.println(zeme);
        System.out.println("\tPatenty: " + filesSet.size());
        System.out.println("\tDuplikáty: " + (files.size() - filesSet.size()));
        System.out.println("\tČas: " + timeElapsed + " s");
        System.out.println("\n");
    }
}

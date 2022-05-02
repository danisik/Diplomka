import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class mainn {

    private static Connection conn = null;
    private static List<Patent> patents = new ArrayList<>();

    public static void main(String[] args) {

        createConnection();

        // sapebinich_country
        // ID_COUNTRY, COUNTRY

        // sapebinich_ipcr
        // ID_IPCR, SECTION, CLASS, SUBCLASS

        // sapebinich_patent
        // ID_PATENT, DOCDB_KIND, INSTITUTE_TEXT

        // sapebinich_patent_ipcr
        // ID_PATENT, ID_IPCR

        // sapebinich_patent_priority
        // DOC_NUMBER (jen to kde je IT), DOC_DATE

        // sapebinich_patent_title
        // TITLE (vždy ten záznam s největším ID)



        // Postup:   (ID, date, title, author, IPCR, kind)
        // 1. Zjistit všechny patenty - kind, author
        getPatents();
        System.out.println("Patents count:" + patents.size());

        // 2. Zjistit titulek - title
        getTitles();
        System.out.println("Titles done");

        // 3. Zjistit prioritu - ID, date
        getPriorities();
        System.out.println("Priorities done");

        // 4. Zjistit ipcr - ipcr
        getIpcr();
        System.out.println("IPCR done");

        long noAuthor = 0;
        long noID = 0;
        long noDate = 0;
        long noTitle = 0;
        long success = 0;

        for (Patent patent : patents) {

            PreparedStatement stmt = null;
            ResultSet set = null;
            long pat_id = 0;

            if (patent.getTitle().equals("")) {
                noTitle++;
                continue;
            }

            if (patent.getId().equals("")) {
                noID++;
                continue;
            }

            if (patent.getDate() == null || patent.getDate().toString().equals("")) {
                noDate++;
                continue;
            }

            if (patent.getAuthor().size() == 0) {
                noAuthor++;
                continue;
            }

            try {

                stmt = conn.prepareStatement("insert into patents(patent_id, title, patent_date, kind, country, language) values (?, ?, ?, ?, ?, ?)");
                stmt.setString(1, patent.getId());
                stmt.setString(2, patent.getTitle());
                stmt.setDate(3, patent.getDate());
                stmt.setString(4, patent.getKind());
                stmt.setString(5, patent.getCountry());
                stmt.setString(6, "-");
                stmt.execute();

                Long patentId = 0L;
                stmt = conn.prepareStatement("select last_insert_id()");
                ResultSet rSet = stmt.executeQuery();

                while (rSet.next()) {

                    patentId = rSet.getLong(1);
                }

                for (String author : patent.getAuthor()) {

                    stmt = conn.prepareStatement("insert into inventors(id_patent, inventor) values (?, ?)");
                    stmt.setLong(1, patentId);
                    stmt.setString(2, author);
                    stmt.execute();
                }

                for (int j = 0; j < patent.getSections().size(); j++) {

                    String section = patent.getSections().get(j);
                    String sClass = patent.getClasses().get(j);
                    String subclass = patent.getSubclasses().get(j);

                    stmt = conn.prepareStatement("insert into classification(id_patent, section, class, subclass) values (?, ?, ?, ?)");
                    stmt.setLong(1, patentId);
                    stmt.setString(2, section);
                    stmt.setString(3, sClass);
                    stmt.setString(4, subclass);
                    stmt.execute();
                }

                success++;

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        System.out.println("No ID: " + noID);
        System.out.println("No AUTHOR: " + noAuthor);
        System.out.println("No DATE: " + noDate);
        System.out.println("No TITLE: " + noTitle);
        System.out.println("SUCCESS: " + success);

        closeConnection();
    }

    private static void getPatents() {

        try {

            PreparedStatement stmt = conn.prepareStatement("select ID_PATENT, DOCDB_KIND, INSTITUTE_TEXT from sapebinich_patent");
            ResultSet set = stmt.executeQuery();

            while (set.next()) {

                Patent patent = new Patent();
                patent.setIdTable(set.getLong(1));
                patent.setKind(set.getString(2).substring(0, 1));

                String author = set.getString(3);
                String[] authors = author.split(";");

                for (String at : authors) {

                    patent.addAuthor(at);
                }

                patents.add(patent);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void getTitles() {

        try {

            for (Patent patent: patents) {

                PreparedStatement stmt = conn.prepareStatement("SELECT title FROM `sapebinich_patent_title` where id_patent = ? and ID_PATENT_TITLE = (select id_patent_title from sapebinich_patent_title where id_patent = ? order by id_patent_title desc LIMIT 1)");
                stmt.setLong(1, patent.getIdTable());
                stmt.setLong(2, patent.getIdTable());
                ResultSet set = stmt.executeQuery();

                while (set.next()) {

                    patent.setTitle(set.getString(1));
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void getPriorities() {

        try {

            for (Patent patent: patents) {

                PreparedStatement stmt = conn.prepareStatement("SELECT DOC_NUMBER, DOC_DATE FROM `sapebinich_patent_priority` where id_patent = ? and YEAR(doc_date) >= 2000");
                stmt.setLong(1, patent.getIdTable());
                ResultSet set = stmt.executeQuery();

                while (set.next()) {

                    if (!patent.getId().equals("")) break;
                    patent.setId(set.getString(1));
                    patent.setDate(set.getDate(2));
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void getIpcr() {

        try {

            for (Patent patent: patents) {

                long id_ipcr = 0;

                PreparedStatement stmt = conn.prepareStatement("SELECT ID_IPCR FROM `sapebinich_patent_ipcr` where id_patent = ?");
                stmt.setLong(1, patent.getIdTable());
                ResultSet set = stmt.executeQuery();

                while (set.next()) {

                    id_ipcr = set.getLong(1);
                }

                stmt = conn.prepareStatement("SELECT SECTION, CLASS, SUBCLASS FROM `sapebinich_ipcr` where id_ipcr = ?");
                stmt.setLong(1, id_ipcr);
                set = stmt.executeQuery();

                while (set.next()) {

                    patent.addSection(set.getString(1));
                    patent.addClass(set.getString(2));
                    patent.addSubclass(set.getString(3));
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
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

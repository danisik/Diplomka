import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class mainclass {

    public static void main(String[] args) {

        /*
        // auto close connection
        try {
            Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:6033/patents", "root", "password");

            if (conn != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }

            conn.close();

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
         */

        String dataPath = "D:\\mongodb\\data.json";

        try {
            JsonFactory jsonfactory = new JsonFactory();
            File source = new File(dataPath);
            JsonParser parser = jsonfactory.createJsonParser(source);

            long test = 0;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String token = parser.getCurrentName();
                test++;
            }

            parser.close();
            System.out.println("test: " + test);

        } catch (JsonGenerationException jge) { jge.printStackTrace(); }
        catch (JsonMappingException jme) { jme.printStackTrace(); }
        catch (IOException ioex) { ioex.printStackTrace(); }


    }

}

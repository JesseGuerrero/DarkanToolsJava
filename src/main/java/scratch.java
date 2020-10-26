import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

class Scratch {

    /*
    * Returns all player names as an array
    */
    public ArrayList<String> getPlayersArray() {
        ArrayList<String> players = new ArrayList<String>();

        /*
        * Get player listing from API
        * */

        return players;
    }

    /*
    * Returns 1 request
    * */
    public static String getAPIReq(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    /*
    * Performs an SQL query on our VPS
    * */
    public void commitSQL(String sql, String database) {

//        // SQLite connection string
//        String url = "jdbc:mysql://51.79.66.9:3306/testdb";
//        String username = "jesse";
//        String password= "";
//
//        // SQL statement for creating a new table
//        String SQL = "SELECT * FROM friends_list;";
//
//        try (Connection conn = DriverManager.getConnection(url, username, password);
//             Statement stmt = conn.createStatement()) {
//
//            //Query
//            ResultSet myResults = stmt.executeQuery(SQL);
//
//            while(myResults.next()) {
//                System.out.println("Friend name: " + myResults.getString("name"));
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getAPIReq("https://darkan.org/api/highscores?page=1"));
    }
}
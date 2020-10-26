package libs;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DarkanTools {
    static String urlPartial = "jdbc:mysql://51.79.66.9:3306/";//you have to add the database at the end
    static String usernameDB = "jesse";
    static String passDB = "";

    enum SKILLS {
        ATTACK(0), DEFENCE(1), STRENGTH(2), HITPOINTS(3), RANGED(4), PRAYER(5), MAGIC(6), COOKING(7), WOODCUTTING(8), FLETCHING(9), FISHING(10),
        FIREMAKING(11), CRAFTING(12), SMITHING(13), MINING(14), HERBLORE(15), AGILITY(16), THIEVING(17), SLAYER(18), FARMING(19), RUNECRAFTING(20),
        HUNTER(21), CONSTRUCTION(22), SUMMONING(23), DUNGEONEERING(24);

        private int skill;

        private SKILLS(int skill) {
            this.skill = skill;
        }

        public int getValue() {
            return this.skill;
        }
    }

    /*
    * Perhaps make a executeSQL to push information. Request SQL could be for fulling info
    * */
    protected static void updateUsernamesDB() {

        String db = "xp_profiles";

        //Set full URL
        String url = urlPartial + db;

        try {
            ArrayList<String> playerNames = getPlayersArray();
            Connection conn = DriverManager.getConnection(url, usernameDB, passDB);
            Statement stmt = conn.createStatement();

            stmt.execute("TRUNCATE TABLE players;");
            //Query
            for(String player : playerNames) {
                String sql = "INSERT INTO players (name) VALUES (\"";
                sql = sql+player+"\");";
                stmt.execute(sql);
                System.out.println(sql);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    protected static ArrayList<String> getPlayersDB() {
        String url = urlPartial + "xp_profiles";
        ArrayList<String> players = new ArrayList<String>();

        try {
            Connection conn = DriverManager.getConnection(url, usernameDB, passDB);
            Statement stmt = conn.createStatement();

            ResultSet result = stmt.executeQuery("SELECT * FROM players;");
            while(result.next()) {
                players.add(result.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return players;
    }

    /*
     * Returns all player names as an array
     */
    private static ArrayList<String> getPlayersArray() throws Exception {
        ArrayList<String> players = new ArrayList<String>();
        JSONParser parse = new JSONParser();

        JSONArray pageArray;
        int page = 0;
        do {
            String response = DarkanTools.getAPIReq("https://darkan.org/api/highscores?page=" + page++);
            pageArray = (JSONArray) parse.parse(response);
            for(int i = 0; i<pageArray.size(); i++) {
                String name = ((JSONObject) (pageArray.get(i))).get("username").toString();
                if(name.equalsIgnoreCase("null")) {
                    continue;
                }
                players.add(name);
            }
        } while(pageArray.size() != 0);

        return players;
    }

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

    public static HashMap<String, Object> getSkillsMap(String username) {
        HashMap<String, Object> skillsMap = new HashMap<String, Object>();

        JSONParser parse = new JSONParser();
        String response = "";
        try {
            response = getAPIReq("https://darkan.org/api/player/" + username);

            JSONObject playerObject = (JSONObject) parse.parse(response);
            JSONObject statsObject = (((JSONObject)playerObject.get("stats")));
            JSONArray skillsArr = ((JSONArray)((JSONObject)playerObject.get("stats")).get("skills"));

            int totalLvl =  Integer.parseInt(statsObject.get("totalLevel").toString());
            int totalXp = Integer.parseInt(statsObject.get("totalXp").toString());


            //Saves skills into a map/dictionary using an enum data type
            skillsMap.put("name", username);
            skillsMap.put("totallvl", totalLvl);
            skillsMap.put("totalxp", totalXp);
            SKILLS[] skills = SKILLS.values();
            for(SKILLS skill : skills) {
                int skillXp = Integer.parseInt(((JSONObject)skillsArr.get(skill.getValue())).get("xp").toString());
                skillsMap.put(skill.name().toLowerCase(), skillXp);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }


        return skillsMap;
    }

    public static void updateSkillsDB() {
        String url = urlPartial + "xp_profiles";
        ArrayList<String> players = getPlayersDB();
        HashMap<String, Object> skillsMap;

        try {
            Connection conn = DriverManager.getConnection(url, usernameDB, passDB);
            Statement stmt = conn.createStatement();
            for(String player : players) {
                skillsMap = getSkillsMap(player);
                String sql = "INSERT IGNORE INTO player_skills (name, totallvl, totalxp, record_date, ";
                for (SKILLS skill : SKILLS.values()) {
                    sql = sql + skill.toString().toLowerCase() + ", ";
                }

                //Take off the last comma
                sql = sql.substring(0, sql.length() - 2);

                //Add the unique SQLs
                sql = sql + ") VALUES (";
                sql = sql + "\"" + skillsMap.get("name") + "\"" + ", ";
                sql = sql + skillsMap.get("totallvl") + ", ";
                sql = sql + skillsMap.get("totalxp") + ", ";
                sql = sql + "CURDATE()" + ", ";

                for (SKILLS skill : SKILLS.values()) {
                    //from the map your just getting the integer as xp using the SKILLS enum for the string values
                    sql = sql + skillsMap.get(skill.toString().toLowerCase()).toString() + ", ";
                }

                //Take off the last comma
                sql = sql.substring(0, sql.length() - 2);
                sql = sql + ");";
                stmt.execute(sql);
                System.out.println(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        /*
        * This area is used for debugging tools
        * */
//        updateUsernamesDB();
//        updateSkillsDB();


    }
}

/* Original SQL data
CREATE TABLE player_skills(id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(30), totallvl int, totalxp int, record_date DATE, attack int, defence int, strength int, hitpoints int, ranged int, prayer int, magic int, cooking int, woodcutting int, fletching int, fishing int, firemaking int, crafting int, smithing int, mining int, herblore int, agility int, thieving int, slayer int, farming int, runecrafting int, hunter int, construction int, summoning int, dungeoneering int, UNIQUE KEY uniques(name, record_date));
INSERT IGNORE INTO player_skills (name, totallvl, totalxp, record_date, attack, defence, strength, hitpoints, ranged, prayer, magic, cooking, woodcutting, fletching, fishing, firemaking, crafting, smithing, mining, herblore, agility, thieving, slayer, farming, runecrafting, hunter, construction, summoning, dungeoneering) VALUES ("jawarrior1", 292, 760784, CURDATE(), 37421, 13914, 13533, 22807, 0, 15, 0, 0, 286132, 0, 384060, 0, 0, 138, 45, 0, 0, 15, 0, 0, 25, 0, 45, 0, 2634);
* */
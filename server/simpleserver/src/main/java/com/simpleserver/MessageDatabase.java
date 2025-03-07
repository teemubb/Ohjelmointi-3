package com.simpleserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sqlite.SQLiteException;


public class MessageDatabase{

    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;

	public static synchronized MessageDatabase getInstance() {
		if (null == dbInstance) {
			dbInstance = new MessageDatabase();
		}
        return dbInstance;
    }

    private MessageDatabase(){

        try {
            open("MyDatabase");
        } catch (SQLException e) {
            System.out.println("Log - SQLexception");
        }

    }

    // Opening the database
    public void open(String dbName) throws SQLException{
        File dbFile = new File(dbName);
        boolean Exists = dbFile.exists() && !dbFile.isDirectory();

        // Initialize database file if it doesn't exist
        if (!Exists) {
            init();
        }
        // Combining database path and filename into a JDBC connection address string
        String database = "jdbc:sqlite:" + dbName;
        // Connection to the database
        dbConnection = DriverManager.getConnection(database);
        
    }

    // Database creation
    private boolean init() throws SQLException{

        String dbName = "MyDatabase";
        String database = "jdbc:sqlite:" + dbName;
        dbConnection = DriverManager.getConnection(database);

        if (null != dbConnection) {
            // User table creation
            String createUsertable = "create table users (" +
            "username char(50), " +
            "password char(50) NOT NULL, " +
            "email char(50) NOT NULL, " +
            "userNickname char(50), " +
            "PRIMARY KEY (username)" +
            ")";
            
            // Usermessage table creation
            String createUsermessagetable = "create table usermessage (" + 
            "locationID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "locationName varchar(512), " +
            "locationDescription varchar(512), " +
            "locationCity varchar(50), " +
            "locationCountry varchar(50), " +
            "locationStreetAddress varchar(50), " +
            "originalPostingTime int(255) NOT NULL, " +
            "originalPoster char(50), " +
            "originalPosterTag char(50), " +
            "latitude double, " +
            "longitude double, " +
            "updatereason varchar(50), " +
            "modified int(255), " +
            "timesVisited int default 0, " +
            "weather double, " +
            "FOREIGN KEY (originalPosterTag) references users(username)" + ")"; 

            //feature 6 tour table creation
            String createTourtable = "CREATE TABLE tours (" + 
            "tourID INTEGER PRIMARY KEY AUTOINCREMENT, " + 
            "tour_name VARCHAR, " + 
            "tourDescription VARCHAR, "+   
            "locations TEXT" + ")";

            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUsertable);
            createStatement.executeUpdate(createUsermessagetable);
            createStatement.executeUpdate(createTourtable);
            createStatement.close();

            System.out.println("DB creation successful");
            return true;
        }
        System.out.println("DB creation failed");
        return false;

    }
    
    // Closes the database connection
    public void closeDB() throws SQLException {
		if (null != dbConnection) {
			dbConnection.close();
            System.out.println("Closed connection");
			dbConnection = null;
		}
    }

    // Saving users to the database
    public void setUsers(JSONObject user) throws SQLException{
        try{
            // Saves user details with password encrypted to the database
            String setUsersString = "insert into users VALUES('" + user.getString("username") + "','" + Salaaja.SalaaSalasana(user.getString("password")) + "','" + user.getString("email") + "','" + user.getString("username")+ "')"; //kai oikein?
            Statement createStatement;
            createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(setUsersString);
            createStatement.close();
        }
        catch (SQLiteException e){
                System.err.println("Username is taken");
        }
    }

     // Getting user data from the database
     public JSONObject getUser(String username) throws SQLException{
        Statement queryStatement = null;
        
        String getUserString = "SELECT * from users WHERE username = '" + username + "';";
        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getUserString);

        while (rs.next()) {
            JSONObject user = new JSONObject();
                user.put("username", rs.getString("username"));
                user.put("password", rs.getString("password"));
                user.put("email", rs.getString("email"));
                return user;
        }  
        return null;
     }



     // Saving messages to the database
    public void setMessage(JSONObject message) throws SQLException {

        String locationName = message.getString("locationName");
        String locationDescription = message.getString("locationDescription");
        String locationCity = message.getString("locationCity");
        long originalPostingTime = message.getLong("originalPostingTime");
        String originalPoster = message.getString("originalPoster");
        System.out.println(originalPoster);
        String locationCountry = message.getString("locationCountry");
        String locationStreetAddress = message.getString("locationStreetAddress");
        System.out.println(originalPostingTime);
        Double latitude = message.has("latitude") ? message.getDouble("latitude") : null;
        Double longitude = message.has("longitude") ? message.getDouble("longitude") : null;
                
        // lat&long if not entered
        String latitudeValue = (latitude != null) ? String.valueOf(latitude) : "NULL";
        String longitudeValue = (longitude != null) ? String.valueOf(longitude) : "NULL";

        // Save weather if given feature 5
        Double weather = message.has("Weather") ? message.getDouble("Weather") : null;

        // Linking values 27.4 MUOKATTU ALKUP= String originalPosterTag = getOriginalPosterTag(originalPoster);
        String originalPosterTag = originalPoster;

        // Insertion message feature 5 muokattu

        // Fetch the userNickname corresponding to the originalPoster
        String userNickname = getUserNickname(originalPoster);
        System.out.println(userNickname);

        String setMessageString = "INSERT INTO usermessage " +
                "(locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPostingTime, originalPoster, originalPosterTag, latitude, longitude, weather) " +
                "VALUES ('" + locationName + "', '" + locationDescription + "', '" + locationCity + "', '" + locationCountry + "', '" + locationStreetAddress + "', " + originalPostingTime + ", '" + originalPoster + "', '" + originalPosterTag + "', " + latitudeValue + ", " + longitudeValue + ", " + weather + ")";
                
                Statement createStatement;
                createStatement = dbConnection.createStatement();
                createStatement.executeUpdate(setMessageString);
                createStatement.close();
    }


    

    private String getUserNickname(String originalPoster) throws SQLException {
        String userNickname = null;
        String query = "SELECT userNickname FROM users WHERE username = '" + originalPoster + "'";
        
        try (Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery(query)) {
            
            if (rs.next()) {
                userNickname = rs.getString("userNickname");
            }
        }
        return userNickname;
    }


    // Getting usermessages from the database
    public JSONArray getMessages() throws SQLException {

        Statement queryStatement = null;
        JSONArray jsonArray = new JSONArray();

        String getMessagesString = "select locationID, locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPostingTime, originalPoster, latitude, longitude, weather, updatereason, modified from usermessage";

        queryStatement = dbConnection.createStatement();
		ResultSet rs = queryStatement.executeQuery(getMessagesString);
        //snib
        while (rs.next()) {
            JSONObject messageObject = new JSONObject();
            messageObject.put("locationID", rs.getInt("locationID"));
            messageObject.put("locationName", rs.getString("locationName"));
            messageObject.put("locationDescription", rs.getString("locationDescription"));
            messageObject.put("locationCity", rs.getString("locationCity"));
            messageObject.put("locationCountry", rs.getString("locationCountry"));
            messageObject.put("locationStreetAddress", rs.getString("locationStreetAddress"));
        
            // Converting epoch time to UTC
            long epochTime = rs.getLong("originalPostingTime");
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            messageObject.put("originalPostingTime", zonedDateTime.format(DateTimeFormatter.ISO_INSTANT));
            
            messageObject.put("originalPoster", rs.getString("originalPoster")); // Fetch userNickname instead of originalPoster SCUFFED TESTI

            // Checking if lat&long exist and adding them to the result if so
            double latitude = rs.getDouble("latitude");
            if (!rs.wasNull()) {
                messageObject.put("latitude", latitude);
            }
            double longitude = rs.getDouble("longitude");
            if (!rs.wasNull()) {
                messageObject.put("longitude", longitude);
            }

            // Adding weather if given
            double weather = rs.getDouble("weather");
            if (!rs.wasNull()) {
                String weatherS = weather + "°C";
                messageObject.put("weather", weatherS);
            }
            // Adding time modified
            long modifiedTime = rs.getLong("modified");
            if (!rs.wasNull()){
                ZonedDateTime mzonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(modifiedTime), ZoneOffset.UTC);
                 messageObject.put("modified", mzonedDateTime.format(DateTimeFormatter.ISO_INSTANT));
            }
            // Adding update reason if given
            String updatereason = rs.getString("updatereason");
            if (updatereason != null) {
                messageObject.put("updatereason", updatereason);
            }

            jsonArray.put(messageObject);
        }
        return jsonArray;
    }


    //bonus features
    // Feature 7
    public void updateMessage(JSONObject message, int locationID, String updatereason, long modified) throws SQLException{

        System.out.println("heloust updatings"); //debug
        StringBuilder updateMessage = new StringBuilder("UPDATE usermessage SET ");

        for (String key : message.keySet()) {
            if (!key.equals("locationID")) {
                Object value = message.get(key);
                if (value instanceof String) {
                    updateMessage.append(key).append(" = '").append(value).append("', ");
                } else if (value instanceof Double) {
                    updateMessage.append(key).append(" = ").append(value).append(", ");
                }
            }
        }

        // Adding update reason if given
        if (updatereason != null) {
            updateMessage.append("updatereason = '").append(updatereason).append("', ");
        }
        updateMessage.append("modified = ").append(modified).append(" ");
        
        // Update only matching tables
        updateMessage.append("WHERE locationID = ").append(locationID);

        if (updateMessage.charAt(updateMessage.length() - 2) == ',') {
            updateMessage.setLength(updateMessage.length() - 2);
        }

        try (Statement statement = dbConnection.createStatement()) {
            statement.executeUpdate(updateMessage.toString());
            System.out.println("Update successful!");
        }
    }

    // Method tocheck if location exists for updating
    public boolean locationExists(int locationID) {
        try {
            // Checking if location exists
            String query = "SELECT CASE WHEN EXISTS (SELECT 1 FROM usermessage WHERE locationID = '" + locationID + "') THEN 1 ELSE 0 END AS locationExists;";
            Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery(query);
    
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Checking for original poster of location with given id
    public String checkOriginalPoster(int locationID) throws SQLException {
        String originalPoster = null;
        String query = "SELECT originalPosterTag FROM usermessage WHERE locationID = " + locationID;
        
        try (Statement statement = dbConnection.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            
            if (rs.next()) {
                String originalPosterTag = rs.getString("originalPosterTag");
                originalPoster = String.valueOf(originalPosterTag);
            }
        }     
        return originalPoster;
    }
    
    /* private String getOriginalPosterTag(String originalPoster) throws SQLException {
        String originalPosterTag = null;
        String query = "SELECT username FROM users WHERE username = '" + originalPoster + "'";
        try (Statement statement = dbConnection.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                originalPosterTag = rs.getString("username");
            }
        }
        return originalPosterTag;
    } */
    
    //FEATURE 8
    // Increments the visitor field by one
    public void addVisitor(int locationID){
        try{
            String query = "UPDATE usermessage SET timesVisited = timesVisited + 1 WHERE locationID = " + locationID;
            Statement statement = dbConnection.createStatement();
            statement.executeUpdate(query);
            System.out.println("Visitor added!");
        } catch (SQLException e) {
            e.printStackTrace();
        }  
    }

    // Fetching 5 most visited locations
    public JSONArray getTopFive() throws SQLException{
        JSONArray topFive = new JSONArray();
        String findTopFive = "SELECT locationName, timesVisited FROM usermessage ORDER BY timesVisited DESC LIMIT 5";
        try (Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery(findTopFive)){
                while (rs.next()){
                    JSONObject location = new JSONObject();
                    location.put("locationName", rs.getString("locationName"));
                    location.put("timesVisited", rs.getInt("timesVisited"));
                    topFive.put(location);
                }

        } catch (SQLException e){
            e.printStackTrace();
    }
    return topFive;
}


    //FEATURE 6
    // Saves a new tour
    public void saveTour(JSONObject tour) throws SQLException{
        String tourName = tour.getString("tour_name");
        String tourDescription = tour.getString("tourDescription");
        JSONArray locations = tour.getJSONArray("locations");

        String TourString = "INSERT INTO tours (tour_name, tourDescription, locations) VALUES ('" + tourName + "', '" + tourDescription + "', '" + locations.toString() + "')";
        Statement createStatement;
		createStatement = dbConnection.createStatement();
		createStatement.executeUpdate(TourString);
		createStatement.close();
    }
    
    // Fetching saved tours
    public JSONArray getTours() throws SQLException {
        JSONArray toursArray = new JSONArray();
    
        String getToursQuery = "SELECT * FROM tours";
        try (Statement statement = dbConnection.createStatement();
             ResultSet rs = statement.executeQuery(getToursQuery)) {
    
            while (rs.next()) {
                JSONObject tourObject = new JSONObject();
                tourObject.put("tour_name", rs.getString("tour_name"));
                tourObject.put("tourDescription", rs.getString("tourDescription"));
    
                JSONArray locationsArray = new JSONArray();
                String locationIDsString = rs.getString("locations");
                JSONArray locationIDsArray = new JSONArray(locationIDsString); 
    
                for (int i = 0; i < locationIDsArray.length(); i++) {
                    JSONObject locationIDObject = locationIDsArray.getJSONObject(i);
                    int locationID = locationIDObject.getInt("locationID");
                    
                    // Adding data from usermessage table from rows with matching locationID
                    String getLocationQuery = "SELECT * FROM usermessage WHERE locationID = " + locationID;
                    try (ResultSet locationRS = statement.executeQuery(getLocationQuery)) {
                        if (locationRS.next()) {
                            JSONObject locationObject = new JSONObject();
                            locationObject.put("locationID", locationRS.getInt("locationID"));
                            locationObject.put("locationName", locationRS.getString("locationName"));
                            locationObject.put("locationDescription", locationRS.getString("locationDescription"));
                            locationObject.put("locationCity", locationRS.getString("locationCity"));
                            locationObject.put("locationCountry", locationRS.getString("locationCountry"));
                            locationObject.put("locationStreetAddress", locationRS.getString("locationStreetAddress"));
                            locationObject.put("originalPoster", locationRS.getString("originalPoster"));
                            // Formatting timestamp
                            long epochTime = locationRS.getLong("originalPostingTime"); 
                            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
                            locationObject.put("originalPostingTime", zonedDateTime.format(DateTimeFormatter.ISO_INSTANT));
                            //DEBUG, TOIMII EKALLA RUN
                            locationObject.put("latitude", locationRS.getDouble("latitude"));
                            locationObject.put("longitude", locationRS.getDouble("longitude"));

                            System.out.println("Latitude: " + locationRS.getDouble("latitude"));
                        System.out.println("Longitude: " + locationRS.getDouble("longitude"));

                            locationsArray.put(locationObject);
                            
                        }
                    }
                }
                tourObject.put("locations", locationsArray);
                toursArray.put(tourObject);
            }
        }
        return toursArray;
    }
    
}


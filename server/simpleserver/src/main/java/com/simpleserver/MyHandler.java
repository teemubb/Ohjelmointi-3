package com.simpleserver;

import com.sun.net.httpserver.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

class MyHandler implements HttpHandler {

    //private List<UserMessage> messages = new ArrayList<>();

    @Override
    public void handle(HttpExchange t) throws IOException { 
        System.out.println("Request handled in thread " + Thread.currentThread().getId()); //test

        String requestParamValue=null; 
        //Check for method
        if("GET".equals(t.getRequestMethod())) {
            requestParamValue = handleGetRequest(t);
            handleResponseGET(t, requestParamValue); 
        }
        else if("POST".equals(t.getRequestMethod())){     
            handlePOSTRequest(t);
            handleResponsePOST(t);   
        }
        else{
            handleResponse(t,"Not supported");
        }


    }

    private void handleResponseGET(HttpExchange httpExchange, String requestParamValue)  throws  IOException {
        try {
                // Fetch messages from the database
                JSONArray messagesJSON = MessageDatabase.getInstance().getMessages();
        
                if (messagesJSON.isEmpty()) {
                    int code = 204; // Response code & No Content
                    httpExchange.sendResponseHeaders(code, -1); // -1 Content length & no content
                } else {

                //Works
                JSONArray jsonArray = messagesJSON;

                String responseString = jsonArray.toString();

                byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
                httpExchange.sendResponseHeaders(200, bytes.length);

                OutputStream outputStream = httpExchange.getResponseBody();
                outputStream.write(bytes);

                //close the stream
                outputStream.flush();
                outputStream.close();    
                }
        } catch (SQLException e) {
            e.printStackTrace();
            handleResponse(httpExchange, "Database error");
        }
    }



    private void handleResponsePOST(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, -1);         // Respond with success (200) 
        System.out.println("debog");
        httpExchange.getResponseBody().close();
    }



    private void handleResponse(HttpExchange httpExchange, String errorMessage) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        //System.out.println("debug1");
        httpExchange.sendResponseHeaders(400, errorMessage.length());         // Respond with error code 400
        //System.out.println("debug2");
        outputStream.write(errorMessage.getBytes(StandardCharsets.UTF_8)); // Output error message
        
        //close the stream
        outputStream.flush();
        outputStream.close();
    }


    private String handleGetRequest(HttpExchange httpExchange) {
        String queryString = httpExchange.getRequestURI().getQuery();
        if (queryString != null && queryString.contains("=")) {
            return queryString.split("=")[1];
        } else {
            return "Invalid";
        }
    }

    private void handlePOSTRequest(HttpExchange httpExchange) throws IOException {
        InputStream requestBody = httpExchange.getRequestBody();
        String jsonText = new BufferedReader(new InputStreamReader(requestBody, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        
        try {
                JSONObject jsonObject = new JSONObject(jsonText);

                //FEATURE 8 / Adds a visitor to the given location
                if (jsonObject.has("locationVisitor")){
                    int locationID = jsonObject.getInt("locationID");
                    MessageDatabase.getInstance().addVisitor(locationID);
                    return;
                }
                
                String locationName = jsonObject.getString("locationName");
                String locationDescription = jsonObject.getString("locationDescription");
                String locationCity = jsonObject.getString("locationCity");
                String locationCountry = jsonObject.getString("locationCountry");
                String locationStreetAddress = jsonObject.getString("locationStreetAddress");
                String originalPoster = jsonObject.getString("originalPoster"); //debug
                String originalPostingTimeS = jsonObject.getString("originalPostingTime");
                System.out.println(originalPoster); //debug

                UserMessage userMessage = new UserMessage(locationName, locationDescription, locationCity); // used for setting posting time


                Long originalPostingTimeEpoch;
                try {
                    // Formatter for parsing and generating timestamps in the specified ISO 8601 format.
                    DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                            .withZone(ZoneId.of("UTC"));

                    // Parse the timestamp to ensure it's in the correct format
                    ZonedDateTime parsedTime = ZonedDateTime.parse(originalPostingTimeS, isoFormatter);

                    // Convert the parsed timestamp to epoch milliseconds
                    originalPostingTimeEpoch = parsedTime.toInstant().toEpochMilli();

                    // Continue with storing the timestamp or processing the message...
                } catch (DateTimeParseException e) {
                    handleResponse(httpExchange, "Invalid timestamp format: " + e.getMessage());
                    return;
                    }


                // Saving original poster from authenticated user, wasn't required
                //String originalPoster = httpExchange.getPrincipal().getUsername(); // Fetching authenticated user's name for originalposter

                //scuffed
                /* String originalPoster;
                if (jsonObject.has("originalPoster")) {
                    originalPoster = jsonObject.getString("originalPoster");
                } else {
                    // Fetch originalPoster from httpExchange
                    originalPoster = httpExchange.getPrincipal().getUsername();
                 } */


                // Setting lat & long
                Double latitude = null;
                Double longitude = null;
                if (jsonObject.has("latitude") && jsonObject.has("longitude")) {
                    latitude = jsonObject.getDouble("latitude");
                    longitude = jsonObject.getDouble("longitude");
                }

                JSONObject messageJSON = new JSONObject();
                messageJSON.put("locationName", userMessage.getLocationName());
                messageJSON.put("locationDescription", userMessage.getLocationDescription());
                messageJSON.put("locationCity", userMessage.getLocationCity());
                messageJSON.put("originalPostingTime", originalPostingTimeEpoch);
                messageJSON.put("originalPoster", originalPoster); // original poster

                messageJSON.put("locationCountry", locationCountry);
                messageJSON.put("locationStreetAddress", locationStreetAddress);
                // Saving lat&long if given
                if (latitude != null && longitude != null) {
                    messageJSON.put("latitude", latitude);
                    messageJSON.put("longitude", longitude);
                }


                //FEATURE 6
                if (jsonObject.has("weather")){
                    // Fetching weather info from server
                    JSONObject weatherInfo = Weather.getWeather(latitude, longitude);
                    int temperature = weatherInfo.getInt("temperature");
                    messageJSON.put("Weather", temperature);
                }
                
    
                //FEATURE 7 
                if (jsonObject.has("locationID")){
                    int locationID = jsonObject.getInt("locationID");
                    boolean existingLocation = MessageDatabase.getInstance().locationExists(locationID);
                    if (existingLocation) {
                        // Include updatereason and modified fields
                        String updatereason = jsonObject.getString("updatereason");
                        long modified = System.currentTimeMillis(); // Modified timestamp
                        
                        //String originalPosterTag = MessageDatabase.getInstance().checkOriginalPoster(locationID);
                        // Checking if user updating is same as original poster, wasn't required
                        /* String currentUser = httpExchange.getPrincipal().getUsername();
                        if (currentUser.equals(originalPosterTag)){ */

                            MessageDatabase.getInstance().updateMessage(messageJSON, locationID, updatereason, modified);
                            httpExchange.sendResponseHeaders(200, -1);
                            //handleResponse(httpExchange, "Message updated.");
                            return;
                        /* } else{
                            handleResponse(httpExchange, "Unauthorized user!");
                            System.out.println("unauthorized user");
                            return;
                        } */
                    
                    }
                }
                    MessageDatabase.getInstance().setMessage(messageJSON);
                    // Success response
                    httpExchange.sendResponseHeaders(200, -1); 
                    
        } catch (Exception e) {
            e.printStackTrace();
            handleResponse(httpExchange, "Invalid JSON format");
            
        } finally {
            requestBody.close(); // Close the stream
        }
    }

}
   
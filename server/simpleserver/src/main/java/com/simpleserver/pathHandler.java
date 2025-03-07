package com.simpleserver;

import com.sun.net.httpserver.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

public class pathHandler implements HttpHandler {
    
    public void handle(HttpExchange exchange) throws IOException{

        //Check for method
        if("GET".equals(exchange.getRequestMethod())) {
            handleResponseGET(exchange); 
        }
        else if("POST".equals(exchange.getRequestMethod())){     
            handlePOSTRequest(exchange);
        }
        else{
            handleResponse(exchange,"Not supported");
        }
        
    }
    


private void handlePOSTRequest(HttpExchange httpExchange) throws IOException{
    InputStream requestBody = httpExchange.getRequestBody();
        String jsonText = new BufferedReader(new InputStreamReader(requestBody, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    try{
            JSONObject jsonObject = new JSONObject(jsonText);

            String tourName = jsonObject.getString("tour_name");
            String tourDescription = jsonObject.getString("tourDescription");
            JSONArray locationsArray = jsonObject.getJSONArray("locations");

            JSONObject tour = new JSONObject();
            tour.put("tour_name", tourName);
            tour.put("tourDescription", tourDescription);
            tour.put("locations", locationsArray);
            MessageDatabase.getInstance().saveTour(tour);

            handleResponse(httpExchange, "Tour added!");
    } catch (Exception e){
        e.printStackTrace();
    }
}

private void handleResponseGET(HttpExchange httpExchange) throws IOException {
    try {
        JSONArray toursJSON = MessageDatabase.getInstance().getTours(); // Retrieve tours from the database

        // If no tours are added
        if (toursJSON.isEmpty()) {
            int code = 204;
            httpExchange.sendResponseHeaders(code, -1);
        } else {
            String responseString = toursJSON.toString();
            byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);

            httpExchange.sendResponseHeaders(200, bytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);

            outputStream.flush();
            outputStream.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
        handleResponse(httpExchange, "Server Error");
    }
}



private void handleResponse(HttpExchange httpExchange, String errorMessage) throws IOException {
    OutputStream outputStream = httpExchange.getResponseBody();

    httpExchange.sendResponseHeaders(400, errorMessage.length()); // Respond with error code 400
    outputStream.write(errorMessage.getBytes(StandardCharsets.UTF_8)); // Output error message
    
    //close the stream
    outputStream.flush();
    outputStream.close();
}
}

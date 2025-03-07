package com.simpleserver;

import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;

//FEATURE 8
public class TopfiveHandler implements HttpHandler {
    
    public void handle(HttpExchange exchange) throws IOException{

        if(!exchange.getRequestMethod().equalsIgnoreCase("GET")){
            exchange.sendResponseHeaders(405, -1);
            return;
        }
        
        try { // Retrieving 5 most visited locations
            JSONArray TopFive = MessageDatabase.getInstance().getTopFive();
            String responseString = TopFive.toString();

            byte[] bytes = responseString.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);

            // Close the stream
            outputStream.flush();
            outputStream.close();  
        }
        catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }

    }
}

package com.simpleserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class Weather {

    public static JSONObject getWeather(double latitude, double longitude) {
        JSONObject weatherInfo = new JSONObject();
    
        try {
            // Weather server url
            URL url = new URL("http://localhost:4001/weather");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setDoOutput(true);
    
            // XML message in correct format
            String xmlMessage = "<coordinates>\n" +
                    "<latitude>" + latitude + "</latitude>\n" +
                    "<longitude>" + longitude + "</longitude>\n" +
                    "</coordinates>";
    
            // XML message to request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(xmlMessage.getBytes());
            outputStream.flush();
            outputStream.close();
    
            // Response code
            int responseCode = connection.getResponseCode();
    
            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseString.append(line);
            }
            reader.close();
    
            // Parse weather information from response
            String weatherResponse = responseString.toString();
            // Extract temperature from XML
            int startIndex = weatherResponse.indexOf("<temperature>");
            int endIndex = weatherResponse.indexOf("</temperature>");
            if (startIndex != -1 && endIndex != -1) {
                String temperatureStr = weatherResponse.substring(startIndex + "<temperature>".length(), endIndex);
                int temperature = Integer.parseInt(temperatureStr);
                weatherInfo.put("temperature", temperature);
            }
    
            // Close connection
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weatherInfo;
    }
}


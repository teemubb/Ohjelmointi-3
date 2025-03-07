package com.simpleserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler {

    private final UserAuthenticator userAuthenticator;
    private final MessageDatabase messageDatabase; 

    // Constructor that takes a UserAuthenticator parameter
    public RegistrationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
        this.messageDatabase = MessageDatabase.getInstance(); // Getting instance
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Allowing only POST requests
        if ("POST".equals(exchange.getRequestMethod())) {
            // Checking content type
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType != null && contentType.startsWith("application/json")) {

            try {
                    // Reading the request body
                    BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));

                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        requestBody.append(line);
                    }
                    
                    JSONObject json = new JSONObject(requestBody.toString());
                    String username = json.getString("username");
                    String password = json.getString("password");
                    String email = json.getString("email");

                        // Blocks empty registration strings
                        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                            String errorResponse = "Invalid credentials";
                            exchange.sendResponseHeaders(400, errorResponse.length());
                            OutputStream os = exchange.getResponseBody();
                            os.write(errorResponse.getBytes());
                            os.close();
                            return;
                        }

                        // Authenticating the user with UserAuthenticator
                        boolean isAuthenticated = userAuthenticator.checkCredentials(username, password);
                        if (!isAuthenticated) {
                            // Adding new non authenticated users to UserAuthenticator
                            User user = new User(username, password, email);
                            boolean userAdded = userAuthenticator.addUser(user);
            
                            if (userAdded) {               
                                messageDatabase.setUsers(json); // Saving user to database

                                // Inform user of successful registration     
                                String response = "Successfully registered!";
                                exchange.sendResponseHeaders(200, response.length());
                                
                                OutputStream os = exchange.getResponseBody();
                                os.write(response.getBytes());
                                os.close();
                                return;
                            } else {
                                // Inform User of failed registration
                                String errorResponse = "User already exists or invalid data";
                                exchange.sendResponseHeaders(400, errorResponse.length());
                                OutputStream os = exchange.getResponseBody();
                                os.write(errorResponse.getBytes());
                                os.close();
                                return;
                            }
                        }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }   
        //For invalid requests, responding with error 400
        String errorResponse = "Not supported";
        exchange.sendResponseHeaders(400, errorResponse.length());
        OutputStream os = exchange.getResponseBody();
        os.write(errorResponse.getBytes());
        os.close();
    }
}
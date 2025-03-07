package com.simpleserver;

import com.sun.net.httpserver.*;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import org.json.JSONObject;
import org.apache.commons.codec.digest.Crypt;


class UserAuthenticator extends BasicAuthenticator{

    private Map<String, User> users = null;
    private MessageDatabase messageDatabase;

    public UserAuthenticator (){ 
        super("info");
        //?
        this.users = new Hashtable<>();
        users.put("dummy", new User("dummy", "passwd", "dummy@example.com")); //add dummy user 1

        this.messageDatabase = MessageDatabase.getInstance();
    }

    // Check if the given credentials match
    public boolean checkCredentials(String username, String password) { 
        
        try {
        JSONObject user = messageDatabase.getUser(username); //week 4
        if (user != null) {
            // Check if the provided password matches the stored password
            String storedPassword = user.getString("password"); // Fetching saved password from database
            
            System.out.println("saved:" + storedPassword); //debug

            String encryptedPassword = Crypt.crypt(password, storedPassword); // Crypting cleartext password with the hashed+salted password

            System.out.println("encrypted:" + encryptedPassword); //debug


            return storedPassword.equals(encryptedPassword); // Comparing crypted user input and stored password
        }  
    }
    catch (SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        return false;
        }
        return false;
    }

    // Handles adding new users 
    public synchronized boolean addUser(User user) { //STEP 8
        // Checks that the username given isn't empty or already registered, and that email & password are not null.
        if (user.getUsername() != null && user.getPassword() != null && user.getEmail() != null && !users.containsKey(user.getUsername())) { 
            users.put(user.getUsername(), user);
            
            return true; 
        }
        return false; 
    }
}

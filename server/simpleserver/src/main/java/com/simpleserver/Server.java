package com.simpleserver;

import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;

import java.util.concurrent.Executors;


class Server {
    public static void main(String[] args) throws Exception {
        try {
            UserAuthenticator authchecker = new UserAuthenticator();
            
            MessageDatabase database = MessageDatabase.getInstance();

            //opens server in port 8001
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0); 
            
            final HttpContext finalContext = server.createContext("/info", new MyHandler());
            finalContext.setAuthenticator(authchecker); // Sets authenticator for /info + checks if user is authenticated to post
            
            // Registration
            final HttpContext registrationContext = server.createContext("/registration", new RegistrationHandler(authchecker));
            
            // FEATURE 8 context
            final HttpContext topfiveContext = server.createContext("/topfive", new TopfiveHandler());
            topfiveContext.setAuthenticator(authchecker); // Sets authentication for topfive context

            // FEATURE 6 context
            final HttpContext pathContext = server.createContext("/paths", new pathHandler());
            pathContext.setAuthenticator(authchecker); // Sets authentication for /paths context

            //char[] passphrase = "salasana".toCharArray(); // For use without args

            char[] passphrase = args[1].toCharArray(); //NEEDS ARGS

            KeyStore ks = KeyStore.getInstance("JKS");
            //ks.load(new FileInputStream("C:/Users/Teemu/Desktop/Ohjelmointi 3/server/keystore.jks"), passphrase); //For use without args
            ks.load(new FileInputStream(args[0]), passphrase); //NEEDS ARGS PASS
        
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            //kmf.init(ks, passphrase); //For use without args
            kmf.init(ks, args[1].toCharArray()); //NEEDS ARGS PASS
        
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);
        
            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);


            server.setHttpsConfigurator (new HttpsConfigurator(ssl) {
                public void configure (HttpsParameters params) {
                // get the remote address if needed
                InetSocketAddress remote = params.getClientAddress();
                SSLContext c = getSSLContext();
                // get the default parameters
                SSLParameters sslparams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);
                }
            });

            server.setExecutor(Executors.newCachedThreadPool()); // creates executor
            server.start(); 
    }  catch (Exception e) { 
        e.printStackTrace(); // Outputs exception information to console
    }
}
}

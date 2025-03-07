package com.simpleserver;

import java.security.SecureRandom;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;

public class Salaaja {
    public static String SalaaSalasana(String passw){

        SecureRandom strongRandomNumberGenerator = new SecureRandom();
       
        byte b[] = new byte[13];
        strongRandomNumberGenerator.nextBytes(b);

        String saltedB = new String(Base64.getEncoder().encode(b));

        String salt = "$6$" + saltedB;
        System.out.println(salt);

        String userPasswordToBeSaved = Crypt.crypt(passw, salt);

        return userPasswordToBeSaved;

    }
}
    


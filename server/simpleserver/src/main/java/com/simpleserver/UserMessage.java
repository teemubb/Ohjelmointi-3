package com.simpleserver;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;


public class UserMessage {
    private String locationName;
    private String locationDescription;
    private String locationCity;
    private String originalPostingTime; 
    public ZonedDateTime sent; 

    public UserMessage(String locationName, String locationDescription, String locationCity) {
        this.locationName = locationName;
        this.locationDescription = locationDescription;
        this.locationCity = locationCity;

        // Getting posting time and saving it
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        this.originalPostingTime = now.format(formatter);
        this.sent = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getoriginalPostingTime(){
        return originalPostingTime;
    }

    public void setoriginalPostingTime(String originalPostingTime){
        this.originalPostingTime = originalPostingTime;
    }


    long dateAsInt() {
        return sent.toInstant().toEpochMilli();
    }

    void setSent(long epoch) {
        sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
}


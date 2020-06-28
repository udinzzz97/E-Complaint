package com.example.complaintsystems;

public class Artist {


    String artistUser;
    String artistId;
    String artistDate;
    String artistType;
    String status;
    String message;
    String hours;


    public Artist(){

    }


    public String artistUser() {
        return artistUser;
    }

    public Artist(String artistId, String artistDate, String artistType, String artistUser,String status,String hours,String message) {
        this.artistId = artistId;

        this.artistDate = artistDate;
        this.artistType = artistType;
        this.artistUser = artistUser;
        this.status = status;
        this.hours = hours;
        this.message = message;
    }
    public String getHours() {
        return hours;
    }

    public String getStatus() {
        return status;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getMessage() {
        return message;
    }

    public String getArtistDate() {
        return artistDate;
    }

    public String getArtistType() {
        return artistType;
    }
}


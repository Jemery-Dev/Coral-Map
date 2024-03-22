package com.example.coralmap.models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

public class UtilisateurLocation {

    private GeoPoint geo_point;
    private @ServerTimestamp String timestamp;
    private Utilisateur user;

    public UtilisateurLocation(GeoPoint geo_point, String timestamp,Utilisateur user){
        this.geo_point = geo_point;
        this.timestamp = timestamp;
        this.user = user;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Utilisateur getUser() {
        return user;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }
}

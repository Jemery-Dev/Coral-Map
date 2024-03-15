package com.example.coralmap.utils;
import android.app.Application;

import com.example.coralmap.models.Utilisateur;
import com.google.firebase.firestore.auth.User;

public class UserClient extends Application {

    private Utilisateur user = null;

    public Utilisateur getUser() {
        return user;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }

}
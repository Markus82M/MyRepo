package com.example.kyn.utils;

import java.util.Hashtable;

public class TokenStore {

    public Hashtable<String, String> hashTable;
    private static TokenStore instance;

    private TokenStore() {
        hashTable = new Hashtable<>();
    }

    public static synchronized TokenStore getInstance() {
        if (instance == null) {
            instance = new TokenStore();
        }
        return instance;
    }

}
package com.example.gateway.application.ids;

import java.util.UUID;

public class Ids {
    public static String newId() {
        return UUID.randomUUID().toString();
    }
}
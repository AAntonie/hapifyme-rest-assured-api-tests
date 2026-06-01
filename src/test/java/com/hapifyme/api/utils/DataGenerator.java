package com.hapifyme.api.utils;

public class DataGenerator {

    public static String generateEmail() {
        return "john_" + System.currentTimeMillis() + "@hapifyme.com";
    }
}
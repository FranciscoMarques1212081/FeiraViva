package com.example.feiraviva.Util;

import java.util.HashMap;
import java.util.Map;

public class AppSession {

    private static final Map<String, Object> sessionData = new HashMap<>();

    public static void set(String key, Object value) {
        sessionData.put(key, value);
    }

    public static Object get(String key) {
        return sessionData.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        return (T) sessionData.get(key);
    }

    public static void remove(String key) {
        sessionData.remove(key);
    }

    public static void clear() {
        sessionData.clear();
    }
}

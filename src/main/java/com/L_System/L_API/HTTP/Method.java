package com.L_System.L_API.HTTP;

public enum Method {
    GET, POST, PUT, DELETE, OPTION;

    public static Method fromString(String method) {
        for (Method m : Method.values()) {
            if (m.name().equalsIgnoreCase(method)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid method: " + method);
    }
}

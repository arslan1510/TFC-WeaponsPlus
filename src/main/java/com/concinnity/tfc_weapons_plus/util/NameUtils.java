package com.concinnity.tfc_weapons_plus.util;

public final class NameUtils {
    public static String normalize(String name) {
        return name.toLowerCase().replace(" ", "_").replace("-", "_");
    }
    
    public static String normalizeMetalName(String metalName) {
        return normalize(metalName);
    }

    private NameUtils() {}
}


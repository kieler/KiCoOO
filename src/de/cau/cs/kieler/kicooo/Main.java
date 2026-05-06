
package de.cau.cs.kieler.kicooo;

import mjson.Json;

/**
 * Main class for KiCoOO.
 */
public class Main {

    /**
     * Main method for KiCoOO.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("KiCoOO - Kieler Compiler for Object-Oriented languages");
        System.out.println("This is a placeholder for the main method.");

        String jsonString = "{\"name\": \"KiCoOO\", \"version\": \"1.0\"}";
        Json json = Json.read(jsonString);
        System.out.println("Parsed JSON: " + json);
    }
}

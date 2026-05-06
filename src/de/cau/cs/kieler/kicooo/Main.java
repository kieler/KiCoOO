
package de.cau.cs.kieler.kicooo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import mjson.Json;

/**
 * Main class for KiCoOO.
 */
public class Main {

    static final String PACKAGE = "kieler_gen";

    /**
     * Main method for KiCoOO.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("KiCoOO - Kieler Compiler for Object-Oriented languages");
        System.out.println("This is a placeholder for the main method.");

        Path outputFolder = Paths.get(args[0], PACKAGE);

        // Create output directory if it doesn't exist
        if (!outputFolder.toFile().exists()) {
            outputFolder.toFile().mkdirs();
        }

        // Create State and Region interfaces
        createStateInterface(outputFolder);
        createRegionInterface(outputFolder);

        try {
            String jsonString = new String(System.in.readAllBytes());
            Json json = Json.read(jsonString);
            processState(json.at(0), outputFolder);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    private static void createStateInterface(Path outputFolder) {
        var file = outputFolder.resolve("State.java");
        var fileContent = new StringBuilder();
        fileContent.append(String.format("package %s;\n\n", PACKAGE));
        fileContent.append("public interface State {\n");
        fileContent.append("}\n");

        FileOutputStream f;
        try {
            f = new FileOutputStream(file.toFile());
            try {
                f.write(fileContent.toString().getBytes());
            } catch (Exception e) {
                System.err.println("Error writing file: " + e.getMessage());
            } finally {
                f.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error creating file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createRegionInterface(Path outputFolder) {
        var file = outputFolder.resolve("Region.java");
        var fileContent = new StringBuilder();
        fileContent.append(String.format("package %s;\n\n", PACKAGE));
        fileContent.append("public interface Region {\n");
        fileContent.append("}\n");

        FileOutputStream f;
        try {
            f = new FileOutputStream(file.toFile());
            try {
                f.write(fileContent.toString().getBytes());
            } catch (Exception e) {
                System.err.println("Error writing file: " + e.getMessage());
            } finally {
                f.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error creating file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processState(Json json, Path outputFolder) {
        // Placeholder for the compilation logic
        System.out.println("Processing state with output folder: " + outputFolder);

        String id = Optional.ofNullable(json.at("id")).map(Json::asString).orElse(null);
        String label = Optional.ofNullable(json.at("label")).map(Json::asString).orElse(null);
        List<Json> regions = Optional.ofNullable(json.at("regions")).map(Json::asJsonList).orElse(List.of());

        if (id == null) {
            // TODO: Handle missing ID more gracefully, e.g., by generating a unique ID.
            System.err.println("State is missing 'id' field.");
            return;
        }

        String className = uppercaseFirst(id.replaceAll("[^a-zA-Z0-9]", "_"));

        Path fileName = outputFolder.resolve(className + ".java");

        var fileContent = new StringBuilder();

        fileContent.append(String.format("package %s;\n\n", PACKAGE));
        fileContent.append(String.format("public class %s implements State {\n", className));
        fileContent.append(String.format("    // Label: %s\n", label));
        fileContent.append(String.format("    // ID: %s\n", id));
        fileContent.append(String.format("    // Regions: %d\n", regions.size()));
        fileContent.append("}\n");

        FileOutputStream f;
        try {
            f = new FileOutputStream(fileName.toFile());
            try {
                f.write(fileContent.toString().getBytes());
            } catch (Exception e) {
                System.err.println("Error writing file: " + e.getMessage());
            } finally {
                f.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Json region : regions) {
            processRegion(region, outputFolder);
        }

    }

    private static void processRegion(Json json, Path outputFolder) {
        // Placeholder for the compilation logic
        System.out.println("Processing region with output folder: " + outputFolder);

        String id = Optional.ofNullable(json.at("id")).map(Json::asString).orElse(null);
        String label = Optional.ofNullable(json.at("label")).map(Json::asString).orElse(null);
        List<Json> states = Optional.ofNullable(json.at("states")).map(Json::asJsonList).orElse(List.of());

        if (id == null) {
            // TODO: Handle missing ID more gracefully, e.g., by generating a unique ID.
            System.err.println("Region is missing an ID.");
            return;
        }

        String className = uppercaseFirst(id.replaceAll("[^a-zA-Z0-9]", "_"));

        Path fileName = outputFolder.resolve(className + ".java");

        var fileContent = new StringBuilder();

        fileContent.append(String.format("package %s;\n\n", PACKAGE));
        fileContent.append(String.format("public class %s implements Region {\n", className));
        fileContent.append(String.format("    // Label: %s\n", label));
        fileContent.append(String.format("    // ID: %s\n", id));
        fileContent.append(String.format("    // States: %d\n", states.size()));
        fileContent.append("}\n");

        FileOutputStream f;
        try {
            f = new FileOutputStream(fileName.toFile());
            try {
                f.write(fileContent.toString().getBytes());
            } catch (Exception e) {
                System.err.println("Error writing file: " + e.getMessage());
            } finally {
                f.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Json state : states) {
            processState(state, outputFolder);
        }
    }

    private static String uppercaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

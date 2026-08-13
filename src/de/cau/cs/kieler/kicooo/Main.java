
package de.cau.cs.kieler.kicooo;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.cau.cs.kieler.kicooo.generators.IGenerator;
import de.cau.cs.kieler.kicooo.generators.JavaGenerator;
import de.cau.cs.kieler.kicooo.generators.TypeScriptGenerator;
import de.cau.cs.kieler.kicooo.model.Action;
import de.cau.cs.kieler.kicooo.model.Region;
import de.cau.cs.kieler.kicooo.model.State;
import de.cau.cs.kieler.kicooo.model.Transition;
import de.cau.cs.kieler.kicooo.model.Variable;
import mjson.Json;

/**
 * Main class for KiCoOO.
 */
public class Main {

    static final String PACKAGE = "kieler_gen";
    static final String BASE_CLASS_PACKAGE = "base_classes";
    static final String BASE_MODULE = "./base_classes/base";

    /**
     * Main method for KiCoOO.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("KiCoOO - Kieler Compiler for generating Object-Oriented languages");

        Path outputFolder = Paths.get(args[0], PACKAGE);
        String targetLanguage = args.length > 1 ? args[1] : "java";

        boolean generateMainClass = !(args.length > 2 && args[2].equals("--no-main"));

        // Create output directory if it doesn't exist
        if (!outputFolder.toFile().exists()) {
            outputFolder.toFile().mkdirs();
        }

        // Load the JSON schema for validation
        Json.Schema schema;
        try (var inputStream = Main.class.getResourceAsStream("/sctx_schema.json")) {
            schema = Json.schema(Json.read(new String(inputStream.readAllBytes())));
        } catch (Exception e) {
            System.err.println("Error reading JSON Schema: " + e.getMessage());
            System.exit(-1);
            return;
        }

        Json json;
        try {
            String jsonString = new String(System.in.readAllBytes());
            json = Json.read(jsonString);
            var validation_result = schema.validate(json);
            if (!validation_result.at("ok").asBoolean()) {
                System.err.println("Validation errors:");
                validation_result.at("errors").forEach(System.err::println);
                System.exit(1);
                return;
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            System.exit(-1);
            return;
        }

        var states = json.asJsonList().stream().map(State::fromJson).toList();
        IGenerator generator = switch (targetLanguage) {
            case "java" -> new JavaGenerator(PACKAGE, BASE_CLASS_PACKAGE);
            case "typescript", "ts" -> new TypeScriptGenerator(BASE_MODULE);
            default -> throw new IllegalArgumentException("Unsupported target language: " + targetLanguage);
        };
        for (var rootState : states) {
            generator.processRootState(rootState, outputFolder);
        }
        if (generateMainClass) {
            generator.createMainClass(states.get(0), outputFolder);

            // Create State and Region Classes, as well as mjson
            generator.createStaticFiles(outputFolder);
        }
    }

    
}
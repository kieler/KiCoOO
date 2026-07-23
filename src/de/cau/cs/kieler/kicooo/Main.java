
package de.cau.cs.kieler.kicooo;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mjson.Json;

/**
 * Main class for KiCoOO.
 */
public class Main {

    static final String PACKAGE = "kieler_gen";
    static final String BASE_CLASS_PACKAGE = "base_classes";

    /**
     * Main method for KiCoOO.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("KiCoOO - Kieler Compiler for generating Object-Oriented languages");

        Path outputFolder = Paths.get(args[0], PACKAGE);

        // Create output directory if it doesn't exist
        if (!outputFolder.toFile().exists()) {
            outputFolder.toFile().mkdirs();
        }

        // Create State and Region interfaces
        createStaticFiles(outputFolder);

        try {
            String jsonString = new String(System.in.readAllBytes());
            Json json = Json.read(jsonString);
            processRootState(json.at(0), outputFolder);
            createMainClass(json.at(0), outputFolder);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    private static void createMainClass(Json json, Path outputFolder) {
        var filePath = outputFolder.resolve("Main.java");
        String className = getStateName(json);

        var variables = Utils.getJsonListByKey(json, "variables").orElse(List.of());

        try (var output = new PrintStream(filePath.toFile())) {
            output.format("package %s;\n\n", PACKAGE);
            for (var imp : List.of(
                    "java.io.BufferedReader",
                    "java.io.IOException",
                    "java.io.InputStreamReader",
                    "mjson.Json")) {
                output.format("import %s;\n", imp);
            }
            output.println();
            output.println("public class Main {\n");
            output.format("    public static %s model = new %s(false);\n\n", className, className);
            output.println("    private static long _tickstart;");
            output.println("    private static long _ticktime;\n");
            output.println("    public static BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));");
            
            outputMethodStart(output, "", "static void", "receiveVariables", "");
            output.println("        try {");
            output.println("            String line = stdInReader.readLine();");
            output.println("            if (line == null) {");
            output.println("                // End of input stream, exit the program");
            output.println("                System.err.println(\"End of input stream detected. Exiting.\");");
            output.println("                System.exit(0);");
            output.println("            }");
            output.println("            Json json = Json.read(line);");
            output.println();
            for (var variable : variables) {
                String varName = Utils.getJsonStringByKey(variable, "id").orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
                String varType = Utils.getJsonStringByKey(variable, "type").orElse("Object");
                String getterMethod = switch (varType) {
                    case "int" -> "asInteger";
                    case "bool" -> "asBoolean";
                    case "string" -> "asString";
                    default -> "asJson"; // TODO: Handle unknown types more gracefully, e.g., by generating a custom class or throwing an error.
                };
                output.format("            // Receive %s\n", varName);
                output.format("            if (json.has(\"%s\")) {\n", varName);
                output.format("                model.%s = json.at(\"%s\").%s();\n", varName, varName, getterMethod);
                output.format("            }\n");
            }
            output.format("            // Receive #ticktime\n");
            output.format("            if (json.has(\"#ticktime\")) {\n");
            output.format("                _ticktime = json.at(\"#ticktime\").asLong();\n");
            output.format("            }\n");
            output.println("        } catch (IOException e) {");
            output.println("            e.printStackTrace();");
            output.println("        } catch (Json.MalformedJsonException e) {");
            output.println("           // Ignore other input");
            output.println("        }\n");
            output.println("    }");

            outputMethodStart(output, "", "static void", "sendVariables", "");
            output.println("        Json json = Json.object();");
            for (var variable : variables) {
                String varName = Utils.getJsonStringByKey(variable, "id").orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
                output.format("        // Send %s\n", varName);
                output.format("        json.set(\"%s\", model.%s);\n", varName, varName);
            }
            output.println("        // Send #ticktime");
            output.println("        json.set(\"#ticktime\", _ticktime);");
            output.println("        System.out.println(json.toString());");
            output.println("    }\n");

            outputMethodStart(output, "", "static void", "main", "String[] args");

            output.println("        model.reset();");
            output.println("        sendVariables();");

            output.println("        while (true) {");
            output.println("            // Read inputs");
            output.println("            receiveVariables();");
            output.println();
            output.println("            _tickstart = System.nanoTime();");
            output.println("            // Reaction of model");
            output.println("            model.tick();");
            output.println("            _ticktime = System.nanoTime() - _tickstart;");
            output.println();
            output.println("            // Send outputs");
            output.println("            sendVariables();");

            output.println("        }");

            output.println("    }");

            output.println("}");
        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createStaticFiles(Path outputFolder) {
        // TODO: This method should place the State and Region base classes in the output folder. For now, it does none of that.
    }

    private static void processRootState(Json json, Path outputFolder) {
        String className = getStateName(json);
        var filePath = outputFolder.resolve(className + ".java");

        // Create a PrintStream for the boilerplate content, then pass it to processState and processRegion to fill in the details.
        try (var output = new PrintStream(filePath.toFile())) {
            output.format("package %s;\n\n", PACKAGE);
            output.print("import java.util.List;\n");
            output.format("import %s.%s.State;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.format("import %s.%s.Region;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.format("import %s.%s.InstantaneousRegion;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.append("\n");

            processState(json, output, 0, "public ");

        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Afterwards, write the content to the output folder as an appropriately named .java file.
        // try (var outputStream = new FileOutputStream(filePath.toFile())) {
        //     outputStream.write(output.toString().getBytes());
        //     System.out.println("Generated file: " + filePath);
        // } catch (FileNotFoundException e) {
        //     System.err.println("Error writing file: " + e.getMessage());
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    private static void processState(Json json, PrintStream output, int indentLevel, String classPrefix) {
        // Placeholder for the compilation logic
        System.out.println("Processing state: " + json.at("id").toString());

        String id = Utils.getJsonStringByKey(json, "id").orElseThrow(() -> new IllegalArgumentException("State is missing required 'id' field."));
        String label = Utils.getJsonStringByKey(json, "label").orElse(id);
        List<Json> variables = Utils.getJsonListByKey(json, "variables").orElse(List.of());
        List<Json> regions = Utils.getJsonListByKey(json, "regions").orElse(List.of());

        List<Json> actions = Utils.getJsonListByKey(json, "actions").orElseThrow(() -> new IllegalArgumentException("State is missing required 'actions' field."));
        List<Json> entryActions = actions.stream()
            .filter(action -> ActionType.fromJsonAction(action).equals(ActionType.ENTRY))
            .toList();
        List<Json> exitActions = actions.stream()
            .filter(action -> ActionType.fromJsonAction(action).equals(ActionType.EXIT))
            .toList();
        List<Json> duringActions = actions.stream()
            .filter(action -> ActionType.fromJsonAction(action).equals(ActionType.DURING))
            .toList();

        List<String> regionNames = regions.stream()
                .map(region -> Utils.getJsonStringByKey(region, "id").orElse(null))
                .map(Utils::formatClassName)
                .toList();

        String className = getStateName(json);
        String indent = "    ".repeat(indentLevel);

        output.format("\n%s%sclass %s extends State {\n\n", indent, classPrefix, className);
        // Debug
        output.format("%s    // Label: %s\n", indent, label);
        // output.format("%s    // ID: %s\n", indent, id));
        // output.format("%s    // Regions: %d\n", indent, regions.size()));

        for (Json variable : variables) {
            String varName = Utils.getJsonStringByKey(variable, "id").orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
            String varType = Utils.getJsonStringByKey(variable, "type").map(Main::sctxTypeToJavaType).orElse("Object");
            if (varName != null) {
                output.format("%s    public %s %s;\n", indent, varType, varName);
            }
        }
        if (!variables.isEmpty()) {
            output.append("\n");
        }

        // add a constructor that initializes the regions and sets the final flag of the state
        output.format("%s    public %s(boolean isFinal) {\n", indent, className);
        output.format("%s        super(isFinal);\n", indent);
        if (!regionNames.isEmpty()) {
            output.format("%s        this.regions = List.of(%s);\n", indent, regionNames.stream().map(name -> "new " + name + "()").collect(Collectors.joining(", ")));
        }
        output.format("%s    }\n", indent, "");
        if (!variables.isEmpty()) {
            outputMethodStart(output, indent, "void", "localReset", "");
            for (Json variable : variables) {
                String varName = Utils.getJsonStringByKey(variable, "id").orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
                String varType = Utils.getJsonStringByKey(variable, "type").map(Main::sctxTypeToJavaType).orElse("Object");
                String defaultValue = switch (varType) {
                    case "int" -> "0";
                    case "boolean" -> "false";
                    case "String" -> "\"\"";
                    default -> "null";
                };
                String initialValue = Utils.getJsonStringByKey(variable, "initialValue").orElse(defaultValue);
                output.format("%s        %s = %s;\n", indent, varName, initialValue);
            }
            output.format("%s    }\n", indent);
        }

        if (!entryActions.isEmpty()) {
            outputMethodStart(output, indent, "void", "onEntry", "");
            processEntryExitActions(output, entryActions, indent);
            output.format("%s    }\n", indent);
        }

        if (!duringActions.isEmpty()) {
            outputMethodStart(output, indent, "void", "onTick", "");
            for (Json action : duringActions) {
                String guard = Utils.getJsonStringByKey(action, "guard").orElse(null);
                String effect = Utils.getJsonStringByKey(action, "action").orElse("");
                boolean isImmediate = Utils.getJsonBooleanByKey(action, "isImmediate").orElse(false);

                if (!isImmediate) {
                    guard = guard == null ? "delayedEnabled" : "delayedEnabled && (" + guard + ")";
                }

                if (guard != null) {
                    output.format("%s        if (%s) {\n", indent, guard);
                    output.format("%s            %s;\n", indent, effect);
                    output.format("%s        }\n", indent);
                } else {
                    output.format("%s        %s;\n", indent, effect);
                }
            }
            output.format("%s    }\n", indent);
        }

        if (!exitActions.isEmpty()) {
            outputMethodStart(output, indent, "void", "onExit", "");
            processEntryExitActions(output, exitActions, indent);
            output.format("%s    }\n", indent);
        }

        for (Json region : regions) {
            processRegion(region, output, indentLevel + 1, "");
        }

        output.format("%s}\n", indent, "");

    }

    private static void processEntryExitActions(PrintStream output, List<Json> actions, String indent) {
        for (Json action : actions) {
            var guard = Utils.getJsonStringByKey(action, "guard");
            var effect = Utils.getJsonStringByKey(action, "action").orElseThrow(() -> new IllegalArgumentException("Action is missing required 'action' field."));

            guard.ifPresentOrElse(
                (g) -> {
                    output.format("%s        if (%s) {\n", indent, g);
                    output.format("%s            %s;\n", indent, effect);
                    output.format("%s        }\n", indent);
                }, () -> {
                    output.format("%s        %s;\n", indent, effect);
                }
            );
        }
    }

    private static void outputMethodStart(PrintStream output, String indent, String methodReturnType, String methodName,
            String methodArgs) {
        // output.format("\n%s    @Override\n", indent);
        output.format("\n%s    public %s %s(%s) {\n", indent, methodReturnType, methodName, methodArgs);
    }

    private static boolean isComplexState(Json state) {
        boolean hasActions = !Utils.getJsonListByKey(state, "actions").map(List::isEmpty).orElse(true);
        boolean hasRegions = !Utils.getJsonListByKey(state, "regions").map(List::isEmpty).orElse(true);
        return hasActions || hasRegions;
    }

    private static void processRegion(Json json, PrintStream output, int indentLevel, String classPrefix) {
        System.out.println("Processing region: " + json.at("id").toString());

        String id = Utils.getJsonStringByKey(json, "id").orElseThrow(() -> new IllegalArgumentException("Region is missing required 'id' field."));
        String label = Utils.getJsonStringByKey(json, "label").orElse(id);
        List<Json> states = Utils.getJsonListByKey(json, "states").orElse(List.of());
        var complexStates = states.stream().filter(Main::isComplexState).toList();

        // var stateNames = states.stream()
        //         .map(state -> Optional.ofNullable(state.at("id")).map(Json::asString).orElse(null))
        //         .map(Main::formatClassName)
        //         .toList();
        var initialStateName = states.stream()
                .filter(state -> Utils.getJsonBooleanByKey(state, "isInitial").orElse(false))
                .findFirst()
                .map(Main::getStateName);

        var weakTransitions = states.stream()
                .collect(Collectors.toMap(
                    state -> getStateName(state),
                    state -> state.at("transitions").asJsonList().stream()
                        .filter(transition -> !PreemptionType.fromJsonTransition(transition).isStrong()).toList()
                ));

        var strongTransitions = states.stream()
                .collect(Collectors.toMap(
                    state -> getStateName(state),
                    state -> state.at("transitions").asJsonList().stream()
                        .filter(transition -> PreemptionType.fromJsonTransition(transition).isStrong()).toList()
                ));

        var hasImmediateTransitions = states.stream()
                .flatMap(state -> state.at("transitions").asJsonList().stream())
                .anyMatch(transition -> Utils.getJsonBooleanByKey(transition, "isImmediate").orElse(false));

        String className = Utils.formatClassName(id);
        var superClassName = hasImmediateTransitions ? "InstantaneousRegion" : "Region";
        String indent = "    ".repeat(indentLevel);

        output.format("\n%s%sclass %s extends %s {\n", indent, classPrefix, className, superClassName);
        // Debug
        output.format("%s    // Label: %s\n", indent, label);
        // output.format("%s    // ID: %s\n", indent, id));
        // output.format("%s    // States: %d\n", indent, states.size()));
        // output.format("%s// Strong Abort Transitions: %s\n", indent, strongTransitions.toString()));
        // output.format("%s// Weak Abort Transitions: %s\n", indent, weakTransitions.toString());

        for (var state : states) {
            var stateName = getStateName(state);
            output.format("%s    private final State %s;\n", indent, stateName);
        }

        output.append("\n");
        // add a constructor that initializes the states and sets the initial state
        output.format("%s    public %s() {\n", indent, className);
        for (var state : states) {
            var stateName = getStateName(state);
            var stateClassName = isComplexState(state) ? stateName : "State";
            var isFinal = isStateFinal(state);
            output.format("%s        this.%s = new %s(%b);\n", indent, stateName, stateClassName, isFinal);
        }
        output.append("\n");
        output.format("%s        this.initialState = %s;\n", indent, initialStateName.orElse(null));
        output.format("%s        this.states = List.of(%s);\n", indent, states.stream().map(Main::getStateName).collect(Collectors.joining(", ")));
        output.format("%s    }\n", indent);


        // process all strong abort transitions if there are any
        if (strongTransitions.values().stream().anyMatch(list -> !list.isEmpty())) {
            outputMethodStart(output, indent, "boolean", "handlePreemptiveTransitions", "");
            processTransitonMap(output, strongTransitions, indent);
            output.format("%s        return false;\n", indent);
            output.format("%s    }\n", indent);
        }

        // process all weak abort transitions if there are any
        if (weakTransitions.values().stream().anyMatch(list -> !list.isEmpty())) {
            outputMethodStart(output, indent, "boolean", "handleNonPreemptiveTransitions", "");
            processTransitonMap(output, weakTransitions, indent);
            output.format("%s        return false;\n", indent);
            output.format("%s    }\n", indent);
        }

        for (Json state : complexStates) {
            processState(state, output, indentLevel + 1, "");
        }

        output.format("%s}\n", indent);
    }

    private static Boolean isStateFinal(Json state) {
        return Utils.getJsonBooleanByKey(state, "isFinal").orElse(false);
    }

    private static String getStateName(Json state) {
        return Utils.getJsonStringByKey(state, "id").map(Utils::formatClassName).orElseThrow(() -> new IllegalArgumentException("State is missing required 'id' field."));
    }

    private static void processTransitonMap(PrintStream output, Map<String, List<Json>> transitionMap, String indent) {
        for (var entry : transitionMap.entrySet()) {
            String stateName = entry.getKey();
            List<Json> transitions = entry.getValue();
            if (!transitions.isEmpty()) {
                output.format("%s        if (activeState.equals(%s)) {\n", indent, stateName);
                for (Json transition : transitions) {
                    String guard = Utils.getJsonStringByKey(transition, "guard").orElse("");
                    String target = Utils.getJsonStringByKey(transition, "targetID").map(Utils::formatClassName).orElse(null);
                    String effect = Utils.getJsonStringByKey(transition, "action").orElse("");

                    boolean isImmediate = Utils.getJsonBooleanByKey(transition, "isImmediate").orElse(false);
                    boolean isTermination = PreemptionType.fromJsonTransition(transition).isTermination();

                    if (isTermination) {
                        guard = guard.isEmpty() ? "activeState.isTerminated()" : "activeState.isTerminated() && (" + guard + ")";
                    } else if (!isImmediate) {
                        guard = guard.isEmpty() ? "activeState.delayedEnabled" : "activeState.delayedEnabled && (" + guard + ")";
                    }

                    if (target != null) {
                        if (!guard.isEmpty()) {
                            output.format("%s            if (%s) { \n", indent, guard);
                            output.format("%s                %s;\n", indent, buildTransitionCommand(target, effect));
                            output.format("%s                return true;\n", indent);
                            output.format("%s            }\n", indent);
                        } else {
                            output.format("%s            %s;\n", indent, buildTransitionCommand(target, effect));
                            output.format("%s            return true;\n", indent);
                        }
                    }
                }
                output.format("%s        }\n", indent);
            }
        }
    }

    private static String buildTransitionCommand(String target, String effect) {
        if (effect.isEmpty()) {
            return String.format("transitionTo(%s)", target);
        } else {
            return String.format("transitionTo(%s, () -> { %s; })", target, effect);
        }
    }

    private static String sctxTypeToJavaType(String sctxType) {
        return switch (sctxType) {
            case "int" -> "int";
            case "bool" -> "boolean";
            case "string" -> "String";
            default -> "Object"; // TODO: Handle unknown types more gracefully, e.g., by generating a custom class or throwing an error.
        };
    }
}

enum PreemptionType {
    STRONG,
    WEAK,
    TERMINATION;

    static PreemptionType fromString(String type) {
        return switch (type.toLowerCase()) {
            case "strong" -> STRONG;
            case "weak" -> WEAK;
            case "termination" -> TERMINATION;
            default -> throw new IllegalArgumentException("Unknown preemption type: " + type);
        };
    }

    public static PreemptionType fromJsonTransition(Json transition) {
        String preemptionStr = Utils.getJsonStringByKey(transition, "preemption").orElse("weak"); // Default to weak if not specified
        return fromString(preemptionStr);
    }

    public boolean isTermination() {
        return this == TERMINATION;
    }

    public boolean isStrong() {
        return this == STRONG;
    }
}

enum ActionType {
    ENTRY,
    EXIT,
    DURING;

    static ActionType fromString(String type) {
        return switch (type.toLowerCase()) {
            case "entry" -> ENTRY;
            case "exit" -> EXIT;
            case "during" -> DURING;
            default -> throw new IllegalArgumentException("Unknown action type: " + type);
        };
    }

    static ActionType fromJsonAction(Json action) {
        String typeStr = Utils.getJsonStringByKey(action, "type").orElseThrow(() -> new IllegalArgumentException("Action is missing required 'type' field."));
        return fromString(typeStr);
    }
}
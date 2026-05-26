
package de.cau.cs.kieler.kicooo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        System.out.println("This is a placeholder for the main method.");

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
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }

    private static void createStaticFiles(Path outputFolder) {
        // TODO: This method should place the State and Region base classes in the output folder. For now, it does none of that.
    }

    private static void processRootState(Json json, Path outputFolder) {
        // Create a string builder for the boilerplate content, then pass it to processState and processRegion to fill in the details.
        var output = new StringBuilder();

        output.append(String.format("package %s;\n\n", PACKAGE));
        output.append("import java.util.List;\n");
        output.append(String.format("import %s.%s.State;\n", PACKAGE, BASE_CLASS_PACKAGE));
        output.append(String.format("import %s.%s.Region;\n", PACKAGE, BASE_CLASS_PACKAGE));
        output.append("\n");

        processState(json, output, 0, "public ");

        // Afterwards, write the content to the output folder as an appropriately named .java file.
        String id = Optional.ofNullable(json.at("id")).map(Json::asString).orElse(null);
        String className = formatClassName(id);

        var filePath = outputFolder.resolve(className + ".java");
        try (var outputStream = new FileOutputStream(filePath.toFile())) {
            outputStream.write(output.toString().getBytes());
            System.out.println("Generated file: " + filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private static void processState(Json json, StringBuilder output, int indentLevel, String classPrefix) {
        // Placeholder for the compilation logic
        System.out.println("Processing state: " + json.at("id").toString());

        String id = Optional.ofNullable(json.at("id")).map(Json::asString).orElse(null);
        String label = Optional.ofNullable(json.at("label")).map(Json::asString).orElse(null);
        List<Json> variables = Optional.ofNullable(json.at("variables")).map(Json::asJsonList).orElse(List.of());
        List<Json> regions = Optional.ofNullable(json.at("regions")).map(Json::asJsonList).orElse(List.of());

        List<Json> actions = Optional.ofNullable(json.at("actions")).map(Json::asJsonList).orElse(List.of());
        List<Json> entryActions = actions.stream().filter(action -> Optional.ofNullable(action.at("type")).map(Json::asString).orElse("").equals("entry")).toList();
        List<Json> exitActions = actions.stream().filter(action -> Optional.ofNullable(action.at("type")).map(Json::asString).orElse("").equals("exit")).toList();
        List<Json> duringActions = actions.stream().filter(action -> Optional.ofNullable(action.at("type")).map(Json::asString).orElse("").equals("during")).toList();

        List<String> regionNames = regions.stream()
                .map(region -> Optional.ofNullable(region.at("id")).map(Json::asString).orElse(null))
                .map(Main::formatClassName)
                .toList();

        if (id == null) {
            // TODO: Handle missing ID more gracefully, e.g., by generating a unique ID.
            System.err.println("State is missing 'id' field.");
            return;
        }

        String className = formatClassName(id);
        String indent = "    ".repeat(indentLevel);

        output.append(String.format("\n%s%sclass %s extends State {\n\n", indent, classPrefix, className));
        // Debug
        // output.append(String.format("%s    // Label: %s\n", indent, label));
        // output.append(String.format("%s    // ID: %s\n", indent, id));
        // output.append(String.format("%s    // Regions: %d\n", indent, regions.size()));

        for (Json variable : variables) {
            String varName = Optional.ofNullable(variable.at("id")).map(Json::asString).orElse(null);
            String varType = Optional.ofNullable(variable.at("type")).map(Json::asString).map(Main::sctxTypeToJavaType).orElse("Object");
            if (varName != null) {
                output.append(String.format("%s    public %s %s;\n", indent, varType, varName));
            }
        }
        if (!variables.isEmpty()) {
            output.append("\n");
        }

        // add a constructor that initializes the regions and sets the final flag of the state
        output.append(String.format("%s    public %s(boolean isFinal) {\n", indent, className));
        output.append(String.format("%s        super(isFinal);\n", indent));
        if (!regionNames.isEmpty()) {
            output.append(String.format("%s        this.regions = List.of(%s);\n", indent, regionNames.stream().map(name -> "new " + name + "()").collect(Collectors.joining(", "))));
        }
        output.append(String.format("%s    }\n", indent, ""));

        if (!variables.isEmpty()) {
            outputMethodStart(output, indent, "void", "localReset", "");
            for (Json variable : variables) {
                String varName = Optional.ofNullable(variable.at("id")).map(Json::asString).orElse(null);
                String varType = Optional.ofNullable(variable.at("type")).map(Json::asString).map(Main::sctxTypeToJavaType).orElse("Object");
                String defaultValue = switch (varType) {
                    case "int" -> "0";
                    case "boolean" -> "false";
                    case "String" -> "\"\"";
                    default -> "null";
                };
                String initialValue = Optional.ofNullable(variable.at("initialValue")).map(Json::asString).orElse(defaultValue);
                output.append(String.format("%s        %s = %s;\n", indent, varName, initialValue));
            }
            output.append(String.format("%s    }\n", indent));
        }

        if (!entryActions.isEmpty()) {
            outputMethodStart(output, indent, "void", "onEntry", "");
            for (Json action : entryActions) {
                String guard = Optional.ofNullable(action.at("guard")).map(Json::asString).orElse("");
                String effect = Optional.ofNullable(action.at("action")).map(Json::asString).orElse("");
                if (!guard.isEmpty()) {
                    output.append(String.format("%s        if (%s) {\n", indent, guard));
                    output.append(String.format("%s            %s;\n", indent, effect));
                    output.append(String.format("%s        }\n", indent));
                } else {
                    output.append(String.format("%s        %s;\n", indent, effect));
                }
            }
            output.append(String.format("%s    }\n", indent));
        }

        if (!duringActions.isEmpty()) {
            outputMethodStart(output, indent, "void", "onTick", "");
            for (Json action : duringActions) {
                String guard = Optional.ofNullable(action.at("guard")).map(Json::asString).orElse("");
                String effect = Optional.ofNullable(action.at("action")).map(Json::asString).orElse("");
                boolean isImmediate = Optional.ofNullable(action.at("isImmediate")).map(Json::asBoolean).orElse(false);

                if (!isImmediate) {
                    guard = guard.isEmpty() ? "delayedEnabled" : "delayedEnabled && (" + guard + ")";
                }

                if (!guard.isEmpty()) {
                    output.append(String.format("%s        if (%s) {\n", indent, guard));
                    output.append(String.format("%s            %s;\n", indent, effect));
                    output.append(String.format("%s        }\n", indent));
                } else {
                    output.append(String.format("%s        %s;\n", indent, effect));
                }
            }
            output.append(String.format("%s    }\n", indent));
        }

        if (!exitActions.isEmpty()) {
            outputMethodStart(output, indent, "void", "onExit", "");
            for (Json action : exitActions) {
                String guard = Optional.ofNullable(action.at("guard")).map(Json::asString).orElse("");
                String effect = Optional.ofNullable(action.at("action")).map(Json::asString).orElse("");
                if (!guard.isEmpty()) {
                    output.append(String.format("%s        if (%s) {\n", indent, guard));
                    output.append(String.format("%s            %s;\n", indent, effect));
                    output.append(String.format("%s        }\n", indent));
                } else {
                    output.append(String.format("%s        %s;\n", indent, effect));
                }
            }
            output.append(String.format("%s    }\n", indent));
        }

        for (Json region : regions) {
            processRegion(region, output, indentLevel + 1, "");
        }

        output.append(String.format("%s}\n", indent, ""));

    }

    private static void outputMethodStart(StringBuilder output, String indent, String methodReturnType, String methodName,
            String methodArgs) {
        output.append(String.format("\n%s    @Override\n", indent));
        output.append(String.format("%s    public %s %s(%s) {\n", indent, methodReturnType, methodName, methodArgs));
    }

    private static boolean isComplexState(Json state) {
        boolean hasActions = !Optional.ofNullable(state.at("actions")).map(Json::asJsonList).map(List::isEmpty).orElse(true);
        boolean hasRegions = !Optional.ofNullable(state.at("regions")).map(Json::asJsonList).map(List::isEmpty).orElse(true);
        return hasActions || hasRegions;
    }

    private static void processRegion(Json json, StringBuilder output, int indentLevel, String classPrefix) {
        System.out.println("Processing region: " + json.at("id").toString());

        String id = Optional.ofNullable(json.at("id")).map(Json::asString).orElse(null);
        String label = Optional.ofNullable(json.at("label")).map(Json::asString).orElse(null);
        List<Json> states = Optional.ofNullable(json.at("states")).map(Json::asJsonList).orElse(List.of());
        var complexStates = states.stream().filter(state -> isComplexState(state)).toList();

        // var stateNames = states.stream()
        //         .map(state -> Optional.ofNullable(state.at("id")).map(Json::asString).orElse(null))
        //         .map(Main::formatClassName)
        //         .toList();
        var initialStateName = states.stream()
                .filter(state -> Optional.ofNullable(state.at("isInitial")).map(Json::asBoolean).orElse(false))
                .findFirst()
                .map(state -> Optional.ofNullable(state.at("id")).map(Json::asString).orElse(null))
                .map(Main::formatClassName);

        var weakTransitions = states.stream()
                .collect(Collectors.toMap(
                    state -> getStateName(state),
                    state -> state.at("transitions").asJsonList().stream()
                        .filter(transition -> !Optional.ofNullable(transition.at("preemption")).map(Json::asString).equals(Optional.of("strong"))).toList()
                ));

        var strongTransitions = states.stream()
                .collect(Collectors.toMap(
                    state -> getStateName(state),
                    state -> state.at("transitions").asJsonList().stream()
                        .filter(transition -> Optional.ofNullable(transition.at("preemption")).map(Json::asString).equals(Optional.of("strong"))).toList()
                ));

        if (id == null) {
            // TODO: Handle missing ID more gracefully, e.g., by generating a unique ID.
            System.err.println("Region is missing an ID.");
            return;
        }

        String className = formatClassName(id);
        String indent = "    ".repeat(indentLevel);

        output.append(String.format("\n%s%sclass %s extends Region {\n", indent, classPrefix, className));
        // Debug
        // output.append(String.format("%s    // Label: %s\n", indent, label));
        // output.append(String.format("%s    // ID: %s\n", indent, id));
        // output.append(String.format("%s    // States: %d\n", indent, states.size()));
        // output.append(String.format("%s// Strong Abort Transitions: %s\n", indent, strongTransitions.toString()));
        // output.append(String.format("%s// Weak Abort Transitions: %s\n", indent, weakTransitions.toString()));

        for (var state : states) {
            var stateName = getStateName(state);
            output.append(String.format("%s    private final State %s;\n", indent, stateName));
        }

        output.append("\n");
        // add a constructor that initializes the states and sets the initial state
        output.append(String.format("%s    public %s() {\n", indent, className));
        for (var state : states) {
            var stateName = getStateName(state);
            var stateClassName = isComplexState(state) ? stateName : "State";
            var isFinal = isStateFinal(state);
            output.append(String.format("%s        this.%s = new %s(%b);\n", indent, stateName, stateClassName, isFinal));
        }
        output.append("\n");
        output.append(String.format("%s        this.initialState = %s;\n", indent, initialStateName.orElse(null)));
        output.append(String.format("%s        this.states = List.of(%s);\n", indent, states.stream().map(Main::getStateName).collect(Collectors.joining(", "))));
        output.append(String.format("%s    }\n", indent));


        // process all strong abort transitions if there are any
        if (strongTransitions.values().stream().anyMatch(list -> !list.isEmpty())) {
            outputMethodStart(output, indent, "boolean", "didStrongAborts", "");
            processTransitonMap(output, strongTransitions, indent);
            output.append(String.format("%s        return false;\n", indent));
            output.append(String.format("%s    }\n", indent));
        }

        // process all weak abort transitions if there are any
        if (weakTransitions.values().stream().anyMatch(list -> !list.isEmpty())) {
            outputMethodStart(output, indent, "boolean", "didWeakAborts", "");
            processTransitonMap(output, weakTransitions, indent);
            output.append(String.format("%s        return false;\n", indent));
            output.append(String.format("%s    }\n", indent));
        }

        for (Json state : complexStates) {
            processState(state, output, indentLevel + 1, "");
        }

        output.append(String.format("%s}\n", indent));
    }

    private static Boolean isStateFinal(Json state) {
        return Optional.ofNullable(state.at("isFinal")).map(Json::asBoolean).orElse(false);
    }

    private static String getStateName(Json state) {
        return Optional.ofNullable(state.at("id")).map(Json::asString).map(Main::formatClassName).orElse(null);
    }

    private static void processTransitonMap(StringBuilder output, Map<String, List<Json>> transitionMap, String indent) {
        for (var entry : transitionMap.entrySet()) {
            String stateName = entry.getKey();
            List<Json> transitions = entry.getValue();
            if (!transitions.isEmpty()) {
                output.append(String.format("%s        if (activeState.equals(%s)) {\n", indent, stateName));
                for (Json transition : transitions) {
                    String guard = Optional.ofNullable(transition.at("guard")).map(Json::asString).orElse("");
                    String target = Optional.ofNullable(transition.at("targetID")).map(Json::asString).map(Main::formatClassName).orElse(null);
                    String effect = Optional.ofNullable(transition.at("action")).map(Json::asString).orElse("");

                    boolean isImmediate = Optional.ofNullable(transition.at("isImmediate")).map(Json::asBoolean).orElse(false);
                    boolean isTermination = Optional.ofNullable(transition.at("preemption")).map(Json::asString).map(preemption -> preemption.equals("termination")).orElse(false);

                    if (isTermination) {
                        guard = guard.isEmpty() ? "activeState.isTerminated()" : "activeState.isTerminated() && (" + guard + ")";
                    } else if (!isImmediate) {
                        guard = guard.isEmpty() ? "delayedEnabled" : "delayedEnabled && (" + guard + ")";
                    }

                    if (target != null) {
                        if (!guard.isEmpty()) {
                            output.append(String.format("%s            if (%s) { \n", indent, guard));
                            output.append(String.format("%s                transitionTo(%s%s);\n", indent, target, effect.isEmpty() ? "" : ", () -> { " + effect + "; }"));
                            output.append(String.format("%s                return true;\n", indent));
                            output.append(String.format("%s            }\n", indent));
                        } else {
                            output.append(String.format("%s            transitionTo(%s); // strong abort\n", indent, target));
                            output.append(String.format("%s            return true;\n", indent));
                        }
                    }
                }
                output.append(String.format("%s        }\n", indent));
            }
        }
    }

    private static String uppercaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String formatClassName(String id) {
        return uppercaseFirst(id.replaceAll("[^a-zA-Z0-9]", "_"));
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

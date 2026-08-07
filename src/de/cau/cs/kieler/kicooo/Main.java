
package de.cau.cs.kieler.kicooo;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        boolean generateMainClass = !(args.length > 1 && args[1].equals("--no-main"));

        // Create output directory if it doesn't exist
        if (!outputFolder.toFile().exists()) {
            outputFolder.toFile().mkdirs();
        }

        // Create State and Region interfaces
        createStaticFiles(outputFolder);

        // Load the JSON schema for validation
        Json.Schema schema;
        try (var inputStream = Main.class.getResourceAsStream("/sctx_schema.json")) {
            schema = Json.schema(Json.read(new String(inputStream.readAllBytes())));
        } catch (Exception e) {
            System.err.println("Error reading JSON Schema: " + e.getMessage());
            return;
        }

        try {
            String jsonString = new String(System.in.readAllBytes());
            Json json = Json.read(jsonString);
            var validation_result = schema.validate(json);
            if (!validation_result.at("ok").asBoolean()) {
                System.err.println("Validation errors:");
                validation_result.at("errors").forEach(System.err::println);
                return;
            }
            processRootState(json.at(0), outputFolder);
            if (generateMainClass) {
                createMainClass(json.at(0), outputFolder);
            }
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
            output.println(
                    "    public static BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));");

            try (var method = new Method(output, 0, "static void", "receiveVariables", "", false)) {
                method.addLine("try {");
                method.addLine("    String line = stdInReader.readLine();");
                method.addLine("    if (line == null) {");
                method.addLine("        // End of input stream, exit the program");
                method.addLine("        System.err.println(\"End of input stream detected. Exiting.\");");
                method.addLine("        System.exit(0);");
                method.addLine("    }");
                method.addLine("    Json json = Json.read(line);");
                method.addLine("");
                for (var variable : variables) {
                    String varName = Utils.getJsonStringByKey(variable, "id").orElseThrow(
                            () -> new IllegalArgumentException("Variable is missing required 'id' field."));
                    String varType = Utils.getJsonStringByKey(variable, "type").orElse("Object");
                    List<Integer> arrayDimensions = Utils.getJsonListByKey(variable, "cardinalities")
                            .map(list -> list.stream().map(Json::asInteger).toList()).orElse(List.of());
                    String getterMethod = switch (varType) {
                        case "int" -> "asInteger";
                        case "bool" -> "asBoolean";
                        case "string" -> "asString";
                        case "float" -> "asDouble";
                        case "double" -> "asDouble";
                        default -> "asJson"; // TODO: Handle unknown types more gracefully, e.g., by generating a custom
                                             // class or throwing an error.
                    };
                    method.formatLine("    // Receive %s", varName);
                    method.formatLine("    if (json.has(\"%s\")) {", varName);
                    if (arrayDimensions.isEmpty()) {
                        // Scalar value
                        method.formatLine("        model.%s = json.at(\"%s\").%s();", varName, varName, getterMethod);
                    } else {
                        // Array value
                        // TODO: make this work nice with the method thingy

                        for (int i = 0; i < arrayDimensions.size(); i++) {
                            output.append(Utils.indent(4 + i));
                            output.format("// Dimension %d\n", i + 1);

                            output.append(Utils.indent(4 + i));
                            if (i == 0) {
                                output.format("var _item%d = json.at(\"%s\").asJsonList();\n", i + 1, varName);
                            } else {
                                output.format("var _item%d = _item%d.get(_i%d).asJsonList();\n", i + 1, i, i);
                            }

                            output.append(Utils.indent(4 + i));
                            output.format("for (int _i%d = 0; _i%d < _item%d.size(); _i%d++) {\n", i + 1, i + 1, i + 1,
                                    i + 1);
                        }

                        // TODO: add the copying code.
                        output.append(Utils.indent(4 + arrayDimensions.size()));
                        output.format("model.%s[%s] = _item%d.get(_i%d).%s();\n", varName,
                                IntStream.range(0, arrayDimensions.size()).mapToObj(i -> "_i" + (i + 1))
                                        .collect(Collectors.joining("][")),
                                arrayDimensions.size(), arrayDimensions.size(), getterMethod);

                        for (int i = arrayDimensions.size() - 1; i >= 0; i--) {
                            output.append(Utils.indent(4 + i));
                            output.format("}\n", i + 1);
                        }
                    }
                    method.formatLine("    }");
                    method.addLine("");
                }
                method.addLine("    // Receive #ticktime");
                method.addLine("    if (json.has(\"#ticktime\")) {");
                method.addLine("        _ticktime = json.at(\"#ticktime\").asLong();");
                method.addLine("    }");
                method.addLine("} catch (IOException e) {");
                method.addLine("    e.printStackTrace();");
                method.addLine("} catch (Json.MalformedJsonException e) {");
                method.addLine("   // Ignore other input");
                method.addLine("}");
            }

            outputMethodStart(output, "", "static void", "sendVariables", "");
            output.println("        Json json = Json.object();");
            for (var variable : variables) {
                String varName = Utils.getJsonStringByKey(variable, "id")
                        .orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
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
        // TODO: This method should place the State and Region base classes in the
        // output folder. For now, it does none of that.
    }

    private static void processRootState(Json json, Path outputFolder) {
        String className = getStateName(json);
        var filePath = outputFolder.resolve(className + ".java");

        // Create a PrintStream, add the boilerplate content, then pass it to
        // processState and processRegion to fill in the details.
        try (var output = new PrintStream(filePath.toFile())) {
            output.format("package %s;\n\n", PACKAGE);
            output.print("import java.util.List;\n");
            output.format("import %s.%s.State;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.format("import %s.%s.Region;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.format("import %s.%s.InstantaneousRegion;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.format("import %s.%s.ReferencedState;\n", PACKAGE, BASE_CLASS_PACKAGE);
            output.append("\n");

            processState(json, output, 0, "public ");

        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processState(Json json, PrintStream output, int indentLevel, String classPrefix) {
        // Placeholder for the compilation logic
        System.out.println("Processing state: " + json.at("id").toString());

        String id = Utils.getJsonStringByKey(json, "id")
                .orElseThrow(() -> new IllegalArgumentException("State is missing required 'id' field."));
        String label = Utils.getJsonStringByKey(json, "label").orElse(id);
        List<Json> variables = Utils.getJsonListByKey(json, "variables").orElse(List.of());
        List<Json> regions = Utils.getJsonListByKey(json, "regions").orElse(List.of());

        List<Json> actions = Utils.getJsonListByKey(json, "actions")
                .orElseThrow(() -> new IllegalArgumentException("State is missing required 'actions' field."));
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

        // TODO: turn this into a Record to avoid the Optional mess
        var reference = Optional.ofNullable(json.at("reference"));
        var referenceTarget = reference.map(ref -> Utils.getJsonStringByKey(ref, "targetID").orElseThrow());
        var referenceParameters = reference.map(
                ref -> Utils.getJsonListByKey(ref, "parameters").orElseThrow().stream().map(Json::asString).toList());

        String className = getStateName(json);
        String indent = "    ".repeat(indentLevel);

        if (reference.isEmpty()) {
            output.format("\n%s%sclass %s extends State {\n\n", Utils.indent(indentLevel), classPrefix, className);
        } else {
            assert referenceTarget.isPresent() : "Reference target is missing.";
            var target = referenceTarget.get();
            output.format("\n%s%sclass %s extends ReferencedState<%s> {\n\n", Utils.indent(indentLevel), classPrefix,
                    className, Utils.formatClassName(target));
        }
        // Debug
        output.format("%s// Label: %s\n", Utils.indent(indentLevel + 1), label);
        // output.format("%s // ID: %s\n", indent, id));
        // output.format("%s // Regions: %d\n", indent, regions.size()));

        for (Json variable : variables) {
            String varName = Utils.getJsonStringByKey(variable, "id")
                    .orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
            String varType = Utils.getJsonStringByKey(variable, "type").map(Main::sctxTypeToJavaType).orElse("Object");
            String cardinalities = Utils.getJsonListByKey(variable, "cardinalities")
                    .map(list -> list.stream().map(dim -> "[]").collect(Collectors.joining()))
                    .orElse("");
            if (varName != null) {
                output.append(Utils.indent(indentLevel + 1));
                output.format("public %s%s %s;\n", varType, cardinalities, varName);
            }
        }
        if (!variables.isEmpty()) {
            output.append("\n");
        }

        // add a constructor that initializes the regions and sets the final flag of the
        // state
        output.append(Utils.indent(indentLevel + 1));
        output.format("public %s(boolean isFinal) {\n", className);
        if (reference.isPresent()) {
            assert referenceTarget.isPresent() : "Reference target is missing.";
            var target = referenceTarget.get();

            output.append(Utils.indent(indentLevel + 2));
            output.format("super(new %s(isFinal), isFinal);\n", Utils.formatClassName(target));

        } else {
            output.append(Utils.indent(indentLevel + 2));
            output.append("super(isFinal);\n");
            if (!regionNames.isEmpty()) {
                output.append(Utils.indent(indentLevel + 2));
                // This is somewhat ugly. However, as Java does not allow trailing commas in
                // List.of(), Collectors.joining is probably the least bad solution.
                output.format("this.regions = List.of(%s);\n",
                        regionNames.stream().map(name -> "new " + name + "()").collect(Collectors.joining(", ")));
            }
        }
        output.format("%s}\n", Utils.indent(indentLevel + 1));

        if (!variables.isEmpty()) {
            outputMethodStart(output, indent, "void", "localReset", "");
            for (Json variable : variables) {
                String varName = Utils.getJsonStringByKey(variable, "id")
                        .orElseThrow(() -> new IllegalArgumentException("Variable is missing required 'id' field."));
                String varType = Utils.getJsonStringByKey(variable, "type").map(Main::sctxTypeToJavaType)
                        .orElse("Object");
                List<Integer> arrayDimensions = Utils.getJsonListByKey(variable, "cardinalities")
                        .map(list -> list.stream().map(Json::asInteger).toList()).orElse(List.of());
                String defaultValue = switch (varType) {
                    case "int" -> "0";
                    case "float", "double" -> "0.0";
                    case "boolean" -> "false";
                    case "String" -> "null"; // TODO: current semantics set to null. Maybe "" is better?
                    default -> "null";
                };
                Optional<String> maybeInitialValue = Utils.getJsonStringByKey(variable, "initialValue");
                String initialValue;
                if (arrayDimensions.size() > 0) {
                    if (maybeInitialValue.isPresent()) {
                        initialValue = String.format("new %s[%s]", varType,
                                arrayDimensions.stream().map(_ -> "").collect(Collectors.joining("][")));
                    } else {
                        initialValue = String.format("new %s[%s]", varType,
                                arrayDimensions.stream().map(String::valueOf).collect(Collectors.joining("][")));
                    }

                } else {
                    initialValue = maybeInitialValue.orElse(defaultValue);
                }
                output.append(Utils.indent(indentLevel + 2));
                output.format("%s = %s;\n", varName, initialValue);
            }
            output.format("%s}\n", Utils.indent(indentLevel + 1));
        }

        if (!entryActions.isEmpty()) {
            // outputMethodStart(output, indent, "void", "onEntry", "");
            try (var _ = new Method(output, indentLevel, "void", "onEntry", "", true)) {
                processEntryExitActions(output, entryActions, indent);
            }
            // output.format("%s }\n", indent);
        }

        if (!duringActions.isEmpty()) {
            try (var method = new Method(output, indentLevel, "void", "onTick", "")) {
                for (Json action : duringActions) {
                    String guard = Utils.getJsonStringByKey(action, "guard").orElse(null);
                    String effect = Utils.getJsonStringByKey(action, "action").orElse("");
                    boolean isImmediate = Utils.getJsonBooleanByKey(action, "isImmediate").orElse(false);

                    if (!isImmediate) {
                        guard = guard == null ? "delayedEnabled" : "delayedEnabled && (" + guard + ")";
                    }

                    if (guard != null) {
                        method.formatLine("if (%s) {", guard);
                        method.formatLine("    %s;", effect);
                        method.formatLine("}");
                    } else {
                        method.formatLine("%s;", effect);
                    }
                }
            }
            // output.format("%s }\n", indent);
        }

        if (!exitActions.isEmpty()) {
            try (var _ = new Method(output, indentLevel, "void", "onExit", "")) {
                processEntryExitActions(output, exitActions, indent);
            }
            // output.format("%s }\n", indent);
        }

        if (reference.isPresent()) {
            assert referenceParameters.isPresent() : "Reference parameters are missing.";
            var parameters = referenceParameters.get();

            try (var method = new Method(output, indentLevel, "void", "copyVariablesIn", "")) {
                for (String parameter : parameters) {
                    String[] parts = parameter.split("to");
                    String input_var = parts[1].strip();
                    String output_var = parts[0].strip();
                    method.formatLine("this.getReference().%s = %s;", output_var, input_var);
                }
            }

            try (var method = new Method(output, indentLevel, "void", "copyVariablesOut", "")) {
                for (String parameter : parameters) {
                    String[] parts = parameter.split("to");
                    String input_var = parts[1].strip();
                    String output_var = parts[0].strip();
                    method.formatLine("%s = this.getReference().%s;", input_var, output_var);
                }
            }
        }

        for (Json region : regions) {
            processRegion(region, output, indentLevel + 1, "");
        }

        output.format("%s}\n", indent, "");

    }

    private static void processEntryExitActions(PrintStream output, List<Json> actions, String indent) {
        for (Json action : actions) {
            var guard = Utils.getJsonStringByKey(action, "guard");
            var effect = Utils.getJsonStringByKey(action, "action")
                    .orElseThrow(() -> new IllegalArgumentException("Action is missing required 'action' field."));

            guard.ifPresentOrElse(
                    (g) -> {
                        output.format("%s        if (%s) {\n", indent, g);
                        output.format("%s            %s;\n", indent, effect);
                        output.format("%s        }\n", indent);
                    }, () -> {
                        output.format("%s        %s;\n", indent, effect);
                    });
        }
    }

    private static void outputMethodStart(PrintStream output, String indent, String methodReturnType, String methodName,
            String methodArgs) {
        // output.format("\n%s @Override\n", indent);
        output.format("\n%s    public %s %s(%s) {\n", indent, methodReturnType, methodName, methodArgs);
    }

    private static boolean isComplexState(Json state) {
        boolean hasActions = !Utils.getJsonListByKey(state, "actions").map(List::isEmpty).orElse(true);
        boolean hasRegions = !Utils.getJsonListByKey(state, "regions").map(List::isEmpty).orElse(true);
        boolean hasReference = state.has("reference");
        return hasActions || hasRegions || hasReference;
    }

    private static void processRegion(Json json, PrintStream output, int indentLevel, String classPrefix) {
        System.out.println("Processing region: " + json.at("id").toString());

        String id = Utils.getJsonStringByKey(json, "id")
                .orElseThrow(() -> new IllegalArgumentException("Region is missing required 'id' field."));
        String label = Utils.getJsonStringByKey(json, "label").orElse(id);
        List<Json> states = Utils.getJsonListByKey(json, "states").orElse(List.of());
        var complexStates = states.stream().filter(Main::isComplexState).toList();

        // var stateNames = states.stream()
        // .map(state ->
        // Optional.ofNullable(state.at("id")).map(Json::asString).orElse(null))
        // .map(Main::formatClassName)
        // .toList();
        var initialStateName = states.stream()
                .filter(state -> Utils.getJsonBooleanByKey(state, "isInitial").orElse(false))
                .findFirst()
                .map(Main::getStateName);

        var weakTransitions = states.stream()
                .collect(Collectors.toMap(
                        state -> getStateName(state),
                        state -> state.at("transitions").asJsonList().stream()
                                .filter(transition -> !PreemptionType.fromJsonTransition(transition).isStrong())
                                .toList()));

        var strongTransitions = states.stream()
                .collect(Collectors.toMap(
                        state -> getStateName(state),
                        state -> state.at("transitions").asJsonList().stream()
                                .filter(transition -> PreemptionType.fromJsonTransition(transition).isStrong())
                                .toList()));

        var hasImmediateTransitions = states.stream()
                .flatMap(state -> state.at("transitions").asJsonList().stream())
                .anyMatch(transition -> Utils.getJsonBooleanByKey(transition, "isImmediate").orElse(false));

        String className = Utils.formatClassName(id);
        var superClassName = hasImmediateTransitions ? "InstantaneousRegion" : "Region";
        String indent = "    ".repeat(indentLevel);

        output.format("\n%s%sclass %s extends %s {\n", indent, classPrefix, className, superClassName);
        // Debug
        output.format("%s    // Label: %s\n", indent, label);
        // output.format("%s // ID: %s\n", indent, id));
        // output.format("%s // States: %d\n", indent, states.size()));
        // output.format("%s// Strong Abort Transitions: %s\n", indent,
        // strongTransitions.toString()));
        // output.format("%s// Weak Abort Transitions: %s\n", indent,
        // weakTransitions.toString());

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
        output.format("%s        this.states = List.of(%s);\n", indent,
                states.stream().map(Main::getStateName).collect(Collectors.joining(", ")));
        output.format("%s    }\n", indent);

        // process all strong abort transitions if there are any
        if (strongTransitions.values().stream().anyMatch(list -> !list.isEmpty())) {
            try (var method = new Method(output, indentLevel, "boolean", "handlePreemptiveTransitions", "")) {
                processTransitonMap(method, strongTransitions);
                method.addLine("return false;");
            }
        }

        // process all weak abort transitions if there are any
        if (weakTransitions.values().stream().anyMatch(list -> !list.isEmpty())) {
            try (var method = new Method(output, indentLevel, "boolean", "handleNonPreemptiveTransitions", "")) {
                processTransitonMap(method, weakTransitions);
                method.addLine("return false;");
            }
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
        return Utils.getJsonStringByKey(state, "id").map(Utils::formatClassName)
                .orElseThrow(() -> new IllegalArgumentException("State is missing required 'id' field."));
    }

    private static void processTransitonMap(Method method, Map<String, List<Json>> transitionMap) {
        for (var entry : transitionMap.entrySet()) {
            String stateName = entry.getKey();
            List<Json> transitions = entry.getValue();
            if (!transitions.isEmpty()) {
                method.formatLine("if (activeState.equals(%s)) {", stateName);
                for (Json transition : transitions) {
                    String guard = Utils.getJsonStringByKey(transition, "guard").orElse("");
                    String target = Utils.getJsonStringByKey(transition, "targetID").map(Utils::formatClassName)
                            .orElse(null);
                    var effect = Utils.getJsonStringByKey(transition, "action");

                    boolean isImmediate = Utils.getJsonBooleanByKey(transition, "isImmediate").orElse(false);
                    boolean isTermination = PreemptionType.fromJsonTransition(transition).isTermination();

                    if (isTermination) {
                        guard = guard.isEmpty() ? "activeState.isTerminated()"
                                : "activeState.isTerminated() && (" + guard + ")";
                    } else if (!isImmediate) {
                        guard = guard.isEmpty() ? "activeState.delayedEnabled"
                                : "activeState.delayedEnabled && (" + guard + ")";
                    }

                    if (target != null) {
                        if (!guard.isEmpty()) {
                            method.formatLine("    if (%s) {", guard);
                            method.formatLine("        %s;", buildTransitionCommand(target, effect));
                            method.addLine("        return true;");
                            method.addLine("    }");
                        } else {
                            method.formatLine("    %s;", buildTransitionCommand(target, effect));
                            method.addLine("    return true;");
                        }
                    }
                }
                method.addLine("}");
            }
        }
    }

    private static String buildTransitionCommand(String target, Optional<String> effect) {
        return effect.map(
                (effect_string) -> String.format("transitionTo(%s, () -> { %s; })", target, effect_string)).orElseGet(
                        () -> String.format("transitionTo(%s)", target));
    }

    private static String sctxTypeToJavaType(String sctxType) {
        // float is mapped to double, because we probably have the memory.
        return switch (sctxType) {
            case "int" -> "int";
            case "bool" -> "boolean";
            case "string" -> "String";
            case "float" -> "double";
            case "double" -> "double";
            default -> "Object"; // TODO: Handle unknown types more gracefully, e.g., by generating a custom
                                 // class or throwing an error.
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
        String preemptionStr = Utils.getJsonStringByKey(transition, "preemption").orElse("weak"); // Default to weak if
                                                                                                  // not specified
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
        String typeStr = Utils.getJsonStringByKey(action, "type")
                .orElseThrow(() -> new IllegalArgumentException("Action is missing required 'type' field."));
        return fromString(typeStr);
    }
}

class Method implements AutoCloseable {
    private final PrintStream output;
    private final int indentLevel;
    private boolean closed = false;

    public Method(PrintStream output, int indentLevel, String returnType, String methodName, String methodArgs) {
        this(output, indentLevel, returnType, methodName, methodArgs, false);
    }

    public Method(PrintStream output, int indentLevel, String returnType, String methodName, String methodArgs,
            boolean isOverride) {
        this.output = output;
        this.indentLevel = indentLevel;
        output.append("\n");
        if (isOverride) {
            output.append(Utils.indent(indentLevel + 1));
            output.format("@Override\n");
        }
        output.append(Utils.indent(indentLevel + 1));
        output.format("public %s %s(%s) {\n", returnType, methodName, methodArgs);
    }

    public void addLine(String statement) {
        if (closed) {
            throw new IllegalStateException("Cannot add lines to a closed method.");
        }
        if (statement != null && !statement.isEmpty()) {
            output.append(Utils.indent(indentLevel + 2));
            output.append(statement);
        }
        output.append("\n");
    }

    public void formatLine(String format, Object... args) {
        addLine(String.format(format, args));
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            // output end of method
            output.append(Utils.indent(indentLevel + 1));
            output.append("}\n");
        }
    }
}
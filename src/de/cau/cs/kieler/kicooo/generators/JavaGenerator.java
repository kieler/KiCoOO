package de.cau.cs.kieler.kicooo.generators;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.cau.cs.kieler.kicooo.KOptional;
import de.cau.cs.kieler.kicooo.Utils;
import de.cau.cs.kieler.kicooo.KOptional.None;
import de.cau.cs.kieler.kicooo.KOptional.Some;
import de.cau.cs.kieler.kicooo.model.Action;
import de.cau.cs.kieler.kicooo.model.Region;
import de.cau.cs.kieler.kicooo.model.State;
import de.cau.cs.kieler.kicooo.model.Transition;
import de.cau.cs.kieler.kicooo.model.Variable;

public class JavaGenerator implements IGenerator {

    final String PACKAGE;
    final String BASE_CLASS_PACKAGE;

    public JavaGenerator(String packageName, String baseClassPackage) {
        this.PACKAGE = packageName;
        this.BASE_CLASS_PACKAGE = baseClassPackage;
    }

    @Override
    public void createStaticFiles(Path outputFolder) {
        // TODO: This method should place the State and Region base classes in the
        // output folder. For now, it does none of that.
    }

    @Override
    public void createMainClass(State mainRootState, Path outputFolder) {
        var filePath = outputFolder.resolve("Main.java");
        String className = mainRootState.getClassName();

        var variables = mainRootState.variables();

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
            // output.println(" private static long _tickstart;");
            // output.println(" private static long _ticktime;\n");
            output.println(
                    "    public static BufferedReader stdInReader = new BufferedReader(new InputStreamReader(System.in));");

            try (var method = new JavaMethod(output, 0, "static void", "receiveVariables", "", false)) {
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
                    String varName = variable.name();
                    String varType = variable.type();
                    List<Integer> arrayDimensions = variable.cardinalities();
                    String getterMethod = switch (varType) {
                        case "int" -> "asInteger";
                        case "bool" -> "asBoolean";
                        case "string" -> "asString";
                        case "float" -> "asDouble";
                        case "double" -> "asDouble";
                        default -> "asValue"; // TODO: Handle unknown types more gracefully, e.g., by generating a
                                              // custom
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
                // method.addLine(" // Receive #ticktime");
                // method.addLine(" if (json.has(\"#ticktime\")) {");
                // method.addLine(" _ticktime = json.at(\"#ticktime\").asLong();");
                // method.addLine(" }");
                method.addLine("} catch (IOException e) {");
                method.addLine("    e.printStackTrace();");
                method.addLine("} catch (Json.MalformedJsonException e) {");
                method.addLine("   // Ignore other input");
                method.addLine("}");
            }

            outputMethodStart(output, "", "static void", "sendVariables", "");
            output.println("        Json json = Json.object();");
            for (var variable : variables) {
                output.format("        // Send %s\n", variable.name());
                output.format("        json.set(\"%s\", model.%s);\n", variable.name(), variable.name());
            }
            // output.println(" // Send #ticktime");
            // output.println(" json.set(\"#ticktime\", _ticktime);");
            output.println("        System.out.println(json.toString());");
            output.println("    }\n");

            outputMethodStart(output, "", "static void", "main", "String[] args");

            output.println("        model.reset();");
            output.println("        sendVariables();");

            output.println("        while (true) {");
            output.println("            // Read inputs");
            output.println("            receiveVariables();");
            output.println();
            // output.println(" _tickstart = System.nanoTime();");
            output.println("            // Reaction of model");
            output.println("            model.tick();");
            // output.println(" _ticktime = System.nanoTime() - _tickstart;");
            output.println();
            output.println("            // Send outputs");
            output.println("            sendVariables();");

            output.println("        }");

            output.println("    }");

            output.println("}");
        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processRootState(State state, Path outputFolder) {
        String className = state.getClassName();
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

            processState(state, output, 0, "public ");

        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void processState(State state, PrintStream output, int indentLevel, String classPrefix) {
        System.out.println("Processing state: " + state.id());

        String label = state.label();
        List<Variable> variables = state.variables();
        List<Region> regions = state.regions();

        List<Action> entryActions = state.entryActions();
        List<Action> exitActions = state.exitActions();
        List<Action> duringActions = state.duringActions();

        var reference = state.reference();
        String indent = "    ".repeat(indentLevel);

        if (reference instanceof KOptional.Some(var ref)) {
            var target = ref.target();
            output.format("\n%s%sclass %s extends ReferencedState<%s> {\n\n", Utils.indent(indentLevel), classPrefix,
                    state.getClassName(), Utils.formatClassName(target));
        } else {
            output.format("\n%s%sclass %s extends State {\n\n", Utils.indent(indentLevel), classPrefix,
                    state.getClassName());
        }
        // Add the label as a comment for clarity
        output.format("%s// Label: %s\n", Utils.indent(indentLevel + 1), label);

        for (var variable : variables) {
            String varName = variable.name();
            String varType = sctxTypeToJavaType(variable.type());
            String cardinalities = variable.cardinalities().stream()
                    .map(dim -> "[]")
                    .collect(Collectors.joining());
            assert (varName != null) : "Variable name is missing.";
            output.append(Utils.indent(indentLevel + 1));
            output.format("public %s%s %s;\n", varType, cardinalities, varName);
        }
        if (!variables.isEmpty()) {
            output.append("\n");
        }

        // add a constructor that initializes the regions and sets the final flag of the
        // state
        output.append(Utils.indent(indentLevel + 1));
        output.format("public %s(boolean isFinal) {\n", state.getClassName());

        if (reference instanceof KOptional.Some(var ref)) {
            var target = ref.target();

            output.append(Utils.indent(indentLevel + 2));
            output.format("super(new %s(isFinal), isFinal);\n", Utils.formatClassName(target));

        } else {
            output.append(Utils.indent(indentLevel + 2));
            output.append("super(isFinal);\n");
            if (!regions.isEmpty()) {
                output.append(Utils.indent(indentLevel + 2));
                // This is somewhat ugly. However, as Java does not allow trailing commas in
                // List.of(), Collectors.joining is probably the least bad solution.
                output.format("this.regions = List.of(%s);\n",
                        regions.stream()
                                .map(Region::getClassName)
                                .map(name -> String.format("new %s()", name))
                                .collect(Collectors.joining(", ")));
            }
        }
        output.format("%s}\n", Utils.indent(indentLevel + 1));

        if (!variables.isEmpty()) {
            outputMethodStart(output, indent, "void", "localReset", "");
            for (var variable : variables) {
                String varName = variable.name();
                String varType = sctxTypeToJavaType(variable.type());
                List<Integer> arrayDimensions = variable.cardinalities();
                String defaultValue = switch (varType) {
                    case "int" -> "0";
                    case "float", "double" -> "0.0";
                    case "boolean" -> "false";
                    case "String" -> "null"; // TODO: current semantics set to null. Maybe "" is better?
                    default -> "null";
                };
                Optional<String> maybeInitialValue = variable.initialValue().map(Object::toString);
                String initialValue;
                if (arrayDimensions.size() > 0) {
                    if (maybeInitialValue.isPresent()) {
                        initialValue = String.format("new %s[%s]", varType,
                                arrayDimensions.stream().map(_ -> "").collect(Collectors.joining("][")))
                                + maybeInitialValue.get();
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
            try (var _ = new JavaMethod(output, indentLevel, "void", "onEntry", "", true)) {
                processEntryExitActions(output, entryActions, indent);
            }
            // output.format("%s }\n", indent);
        }

        if (!duringActions.isEmpty()) {
            try (var method = new JavaMethod(output, indentLevel, "void", "onTick", "")) {
                for (Action action : duringActions) {
                    KOptional<String> guard = action.guard();
                    String effect = action.action();
                    boolean isImmediate = action.isImmediate();

                    if (!isImmediate) {
                        guard = KOptional.of(switch (guard) {
                            case None<String> _ -> "delayedEnabled";
                            case Some(var g) -> "delayedEnabled && (" + g + ")";
                        });
                    }

                    switch (guard) {
                        case Some(String g):
                            method.formatLine("if (%s) {", g);
                            method.formatLine("    %s;", effect);
                            method.formatLine("}");
                            break;
                        case None<String> _:
                            method.formatLine("%s;", effect);
                            break;
                    }
                }
            }
        }

        if (!exitActions.isEmpty()) {
            try (var _ = new JavaMethod(output, indentLevel, "void", "onExit", "")) {
                processEntryExitActions(output, exitActions, indent);
            }
        }

        if (reference instanceof Some(var ref)) {
            var parameters = ref.parameters();

            try (var method = new JavaMethod(output, indentLevel, "void", "copyVariablesIn", "")) {
                for (String parameter : parameters) {
                    String[] parts = parameter.split("to");
                    String input_var = parts[0].strip();
                    String output_var = parts[1].strip();
                    method.formatLine("this.reference.%s = %s;", output_var, input_var);
                }
            }

            try (var method = new JavaMethod(output, indentLevel, "void", "copyVariablesOut", "")) {
                for (String parameter : parameters) {
                    String[] parts = parameter.split("to");
                    String input_var = parts[0].strip();
                    String output_var = parts[1].strip();
                    method.formatLine("%s = this.reference.%s;", input_var, output_var);
                }
            }
        }

        for (var region : regions) {
            processRegion(region, output, indentLevel + 1, "");
        }

        output.format("%s}\n", indent, "");

    }

    private static void processEntryExitActions(PrintStream output, List<Action> actions, String indent) {
        for (Action action : actions) {
            var guard = action.guard();
            var effect = action.action();
            switch (guard) {
                case Some(String g):
                    output.format("%s        if (%s) {\n", indent, g);
                    output.format("%s            %s;\n", indent, effect);
                    output.format("%s        }\n", indent);
                    break;
                case None<String> _:
                    output.format("%s        %s;\n", indent, effect);
                    break;
            }
        }
    }

    private static void outputMethodStart(PrintStream output, String indent, String methodReturnType, String methodName,
            String methodArgs) {
        output.format("\n%s    public %s %s(%s) {\n", indent, methodReturnType, methodName, methodArgs);
    }

    private void processRegion(Region region, PrintStream output, int indentLevel, String classPrefix) {
        System.out.println("Processing region: " + region.id());

        String id = region.id();
        String label = region.label();
        List<State> states = region.states();
        var complexStates = states.stream().filter(s -> !s.isConnector()).filter(State::isComplex).toList();
        var stateMap = states.stream().collect(Collectors.toMap(State::getClassName, s -> s));

        var initialStateName = region.initialState().getClassName();

        var weakTransitions = states.stream()
                .collect(Collectors.toMap(
                        state -> state.getClassName(),
                        state -> state.transitions().stream()
                                .filter(transition -> !transition.preemption().isStrong())
                                .toList()));

        var strongTransitions = states.stream()
                .collect(Collectors.toMap(
                        state -> state.getClassName(),
                        state -> state.transitions().stream()
                                .filter(transition -> transition.preemption().isStrong())
                                .toList()));

        var hasImmediateTransitions = states.stream()
                .filter(state -> !state.isConnector()) // Immediate Transitions from connector states are handled
                                                       // differently, so we can exclude them.
                .flatMap(state -> state.transitions().stream())
                .anyMatch(Transition::isImmediate);

        String className = Utils.formatClassName(id);
        var superClassName = hasImmediateTransitions ? "InstantaneousRegion" : "Region";
        String indent = "    ".repeat(indentLevel);

        output.format("\n%s%sclass %s extends %s {\n", indent, classPrefix, className, superClassName);

        output.format("%s    // Label: %s\n", indent, label);

        for (var state : states) {
            if (state.isConnector()) {
                continue; // Skip connector states, as they are not instantiated.
            }
            var stateName = state.getClassName();
            output.format("%s    private final State %s;\n", indent, stateName);
        }

        output.append("\n");
        // add a constructor that initializes the states and sets the initial state
        output.format("%s    public %s() {\n", indent, className);
        for (var state : states) {
            if (state.isConnector()) {
                continue; // Skip connector states, as they are not instantiated.
            }
            var stateName = state.getClassName();
            var stateClassName = (state.isComplex()) ? stateName : "State";
            var isFinal = state.isFinal();
            output.format("%s        this.%s = new %s(%b);\n", indent, stateName, stateClassName, isFinal);
        }
        output.append("\n");
        output.format("%s        this.initialState = %s;\n", indent, initialStateName);
        output.format("%s        this.states = List.of(%s);\n", indent,
                states.stream().filter(state -> !state.isConnector()).map((state) -> {
                    return state.getClassName();
                }).collect(Collectors.joining(", ")));
        output.format("%s    }\n", indent);

        // process all strong abort transitions if there are any
        boolean hasStrongTransitions = strongTransitions.values().stream().anyMatch(list -> !list.isEmpty());
        if (hasStrongTransitions) {
            try (var method = new JavaMethod(output, indentLevel, "boolean", "handlePreemptiveTransitions", "")) {
                processTransitonMap(method, strongTransitions, stateMap);
                method.addLine("return false;");
            }
        }

        // process all weak abort transitions if there are any
        boolean hasWeakTransitions = weakTransitions.values().stream().anyMatch(list -> !list.isEmpty());
        if (hasWeakTransitions) {
            try (var method = new JavaMethod(output, indentLevel, "boolean", "handleNonPreemptiveTransitions", "")) {
                processTransitonMap(method, weakTransitions, stateMap);
                method.addLine("return false;");
            }
        }

        for (var state : complexStates) {
            processState(state, output, indentLevel + 1, "");
        }

        output.format("%s}\n", indent);
    }

    private static void processTransitonMap(JavaMethod method, Map<String, List<Transition>> transitionMap,
            Map<String, State> stateMap) {
        for (var entry : transitionMap.entrySet()) {
            String stateName = entry.getKey();
            State state = stateMap.get(stateName);
            if (state == null) {
                throw new IllegalStateException("State not found for transition map entry: " + stateName);
            }
            if (state.isConnector()) {
                continue; // Skip connector states, as they are not instantiated and we do not start a tick in one.
            }
            List<Transition> transitions = entry.getValue();
            if (!transitions.isEmpty()) {
                method.formatLine("if (activeState.equals(%s)) {", stateName);

                generateTransitionStatements(method, transitions, stateMap, 1, Set.of(), false);

                method.addLine("}");
            }
        }
    }

    private static void generateTransitionStatements(JavaMethod method, List<Transition> transitions,
            Map<String, State> stateMap, int indentLevel, Set<String> visitedConnectors, boolean fromConnector) {
        for (var transition : transitions) {
            KOptional<String> guard = transition.guard();
            String targetName = Utils.formatClassName(transition.targetID());
            State targetState = stateMap.get(targetName);
            if (targetState == null) {
                throw new IllegalStateException("Target state not found for transition: " + transition);
            }
            boolean targetIsConnector = targetState.isConnector();
            System.out.println("Processing transition " + transition + " to " + targetState + " isConnector: "
                    + targetIsConnector);
            KOptional<String> effect = transition.action();

            boolean isImmediate = fromConnector || transition.isImmediate();
            boolean isTermination = transition.preemption().isTermination();

            if (targetIsConnector) {
                if (visitedConnectors.contains(targetName)) {
                    throw new IllegalStateException("Cycle detected in connector transitions involving: " + targetName);
                }
                visitedConnectors = new HashSet<>(visitedConnectors);
                visitedConnectors.add(targetName);
            }

            if (isTermination) {
                guard = KOptional.of(switch (guard) {
                    case None<String> _ -> "activeState.isTerminated()";
                    case Some(var value) -> "activeState.isTerminated() && (" + value + ")";
                });
            }

            if (!isImmediate) {
                guard = KOptional.of(switch (guard) {
                    case None<String> _ -> "activeState.delayedEnabled";
                    case Some(var value) -> "activeState.delayedEnabled && (" + value + ")";
                });
            }

            switch (guard) {
                case Some(var guardExpr):
                    method.formatLine("%sif (%s) {", Utils.indent(indentLevel), guardExpr);
                    if (targetIsConnector) {
                        // Transition to "null" as an intermediate step, to allow the current state to
                        // be left and the transition effect to execute, before the guards
                        // of the connector state's outgoing transitions are evaluated.
                        method.formatLine("%s    %s;", Utils.indent(indentLevel),
                                buildTransitionCommand("null", effect));

                        var connectorTransitions = targetState.transitions();
                        generateTransitionStatements(method, connectorTransitions, stateMap,
                                indentLevel + 1, visitedConnectors, true);
                    } else {
                        method.formatLine("%s    %s;", Utils.indent(indentLevel),
                                buildTransitionCommand(targetName, effect));
                        method.formatLine("%s    return true;", Utils.indent(indentLevel));
                    }
                    method.formatLine("%s}", Utils.indent(indentLevel));
                    break;
                case None<String> _:
                    if (targetIsConnector) {
                        // TODO: fix code duplication here as it it essentially the same as in the some
                        // case above. Maybe extract a method for this.
                        // Transition to "null" as an intermediate step, to allow the current state to
                        // be left and the transition effect to execute, before the guards
                        // of the connector state's outgoing transitions are evaluated.
                        method.formatLine("%s%s;", Utils.indent(indentLevel),
                                buildTransitionCommand("null", effect));

                        var connectorTransitions = targetState.transitions();
                        generateTransitionStatements(method, connectorTransitions, stateMap,
                                indentLevel, visitedConnectors, true);
                    } else {
                        method.formatLine("%s%s;", Utils.indent(indentLevel),
                                buildTransitionCommand(targetName, effect));
                        method.formatLine("%sreturn true;", Utils.indent(indentLevel));
                    }
            }
        }
    }

    private static String buildTransitionCommand(String target, KOptional<String> effect) {
        return switch (effect) {
            case None<String> _ -> String.format("transitionTo(%s)", target);
            case Some(var effect_string) -> String.format("transitionTo(%s, () -> { %s; })", target, effect_string);
        };
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

class JavaMethod implements AutoCloseable {
    private final PrintStream output;
    private final int indentLevel;
    private boolean closed = false;

    public JavaMethod(PrintStream output, int indentLevel, String returnType, String methodName, String methodArgs) {
        this(output, indentLevel, returnType, methodName, methodArgs, false);
    }

    public JavaMethod(PrintStream output, int indentLevel, String returnType, String methodName, String methodArgs,
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
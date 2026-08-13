package de.cau.cs.kieler.kicooo.generators;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.cau.cs.kieler.kicooo.KOptional;
import de.cau.cs.kieler.kicooo.Utils;
import de.cau.cs.kieler.kicooo.KOptional.None;
import de.cau.cs.kieler.kicooo.KOptional.Some;
import de.cau.cs.kieler.kicooo.model.Action;
import de.cau.cs.kieler.kicooo.model.Region;
import de.cau.cs.kieler.kicooo.model.State;
import de.cau.cs.kieler.kicooo.model.Transition;
import de.cau.cs.kieler.kicooo.model.Variable;

public class TypeScriptGenerator implements IGenerator {

    final String BASE_MODULE;

    public TypeScriptGenerator(String baseModule) {
        this.BASE_MODULE = baseModule;
    }

    @Override
    public void createStaticFiles(Path outputFolder) {
        // TODO: This method should place the State and Region base classes in the
        // output folder. For now, it does none of that.
    }

    @Override
    public void createMainClass(State mainRootState, Path outputFolder) {
        var filePath = outputFolder.resolve("index.ts");
        String className = mainRootState.getClassName();

        var variables = mainRootState.variables();

        try (var output = new PrintStream(filePath.toFile())) {

            for (var imp : List.of(
                    "* as readline from 'node:readline'",
                    "{ stdin as input, stdout as output } from 'node:process'")) {
                output.format("import %s;\n", imp);
            }
            output.format("import { %s } from './%s';\n", className, className);
            output.println();

            output.println("type Variables = {");
            for (var variable : variables) {
                String varName = variable.name();
                String varType = sctxTypeToTypeScriptType(variable.type());
                String cardinalitySuffix = variable.cardinalities().stream()
                        .map(dim -> "[]")
                        .collect(Collectors.joining());
                output.format("    %s: %s%s;\n", varName, varType, cardinalitySuffix);
            }
            output.println("    '#ticktime': number,");
            output.println("};");

            output.println("export async function cli(): Promise<void> {\n");

            output.format("    const { model, context } = %s(false);\n", className);
            output.println("""
                        let ticktime = 0;

                        const rl = readline.createInterface({ input, output });

                        const sendVariables = (): void => {
                            const variables: Variables = { ...context, '#ticktime': ticktime };
                            console.log(JSON.stringify(variables));
                        };

                        const receiveVariables = (line: string): void => {
                            try {
                                const json = JSON.parse(line) as Partial<Variables>;
                    """);

            for (var variable : variables) {
                String varName = variable.name();
                output.format("            if (json.%s !== undefined) { context.%s = json.%s; }\n", varName, varName,
                        varName);
            }

            output.println("""
                                if (typeof json['#ticktime'] === 'number') ticktime = json['#ticktime'];
                            }  catch {
                                return;
                            }
                        };

                        model.reset();
                        sendVariables();

                        for await (const line of rl) {
                            receiveVariables(line);

                            const start = process.hrtime.bigint();
                            model.tick();
                            ticktime = Number(process.hrtime.bigint() - start) // convert to milliseconds

                            sendVariables();
                        }
                    }

                    if (require.main === module) {
                        cli().catch((err) => {
                            console.error(err);
                            process.exit(1);
                        });
                    }
                    """);
        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void processRootState(State state, Path outputFolder) {
        String className = state.getClassName();
        var filePath = outputFolder.resolve(className + ".ts");

        // Create a PrintStream, add the boilerplate content, then pass it to
        // processState and processRegion to fill in the details.
        try (var output = new PrintStream(filePath.toFile())) {
            output.format("""
                    import {
                            State,
                            Region,
                            SimpleState,
                            RegionImpl,
                            StateImpl,
                        } from '%s';

                    """, BASE_MODULE);

            processState(state, output, 0, true);

            // TODO: determine if this should rather be export default
            output.format("\nexport { %s };\n", className);

        } catch (FileNotFoundException e) {
            System.err.println("Error writing file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void processState(State state, PrintStream output, int indentLevel, boolean isRoot) {
        System.out.println("Processing state: " + state.id());

        if (!state.isComplex() && !isRoot) {
            // Simple states are not represented as an Instance of stateImpl over a closure,
            // but are an instance
            // of SimpleState.
            // We still need to emit the relevant constructor call, but do not need the
            // IIFE.

            output.format("%sconst %s = new SimpleState(%b);\n", Utils.indent(indentLevel), state.getClassName(),
                    state.isFinal());
            return;
        }

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
            throw new UnsupportedOperationException("Not yet supported Feature: references.");
        } else {
            output.format("\n%sconst %s = (function %s(_isFinal: boolean) {\n\n", Utils.indent(indentLevel),
                    state.getClassName(), state.getClassName());
        }
        // Add the label as a comment for clarity
        output.format("%s// Label: %s\n", Utils.indent(indentLevel + 1), label);

        for (var variable : variables) {
            String varName = variable.name();
            String varType = sctxTypeToTypeScriptType(variable.type());
            String cardinalitySuffix = variable.cardinalities().stream()
                    .map(dim -> "[]")
                    .collect(Collectors.joining());

            output.append(Utils.indent(indentLevel + 1));
            output.format("let %s: %s%s;\n", varName, varType, cardinalitySuffix);
        }
        if (!variables.isEmpty()) {
            output.append("\n");
            output.append(Utils.indent(indentLevel + 1));
            output.append("const localReset = function localReset() {\n");

            for (var variable : variables) {
                String varName = variable.name();
                String varType = sctxTypeToTypeScriptType(variable.type());
                List<Integer> arrayDimensions = variable.cardinalities();
                String defaultValue = switch (varType) {
                    case "number" -> "0";
                    case "boolean" -> "false";
                    case "String" -> "null"; // TODO: current semantics set to null. Maybe "" is better?
                    default -> "null";
                };
                Optional<String> maybeInitialValue = variable.initialValue().map(Object::toString);
                String initialValue;
                if (arrayDimensions.size() > 0) {
                    if (maybeInitialValue.isPresent()) {
                        initialValue = maybeInitialValue.get().replaceAll("{", "[").replaceAll("}", "]");
                    } else {
                        // create a new array of the given dimensions filled with the default value.
                        // Efficiently.
                        var initialValueBuilder = new StringBuilder();
                        for (int i = 0; i < arrayDimensions.size(); i++) {
                            initialValueBuilder.append("new Array(")
                                    .append(arrayDimensions.get(i))
                                    .append(").fill(");
                            if (i == arrayDimensions.size() - 1) {
                                initialValueBuilder.append(defaultValue)
                                        .append(")");
                            } else {
                                initialValueBuilder.append("undefined")
                                        .append(").map(() => ");
                            }
                        }
                        for (int i = 1; i < arrayDimensions.size(); i++) {
                            initialValueBuilder.append(")");
                        }
                        initialValue = initialValueBuilder.toString();
                    }

                } else {
                    initialValue = maybeInitialValue.orElse(defaultValue);
                }
                output.append(Utils.indent(indentLevel + 2));
                output.format("%s = %s;\n", varName, initialValue);
            }
            output.format("%s}\n", Utils.indent(indentLevel + 1));

            output.println();
            output.println(Utils.indent(indentLevel + 1) + "localReset();\n");

            if (isRoot) {
                output.println();
                // create context object for the root state
                output.format("%sconst context = {\n", Utils.indent(indentLevel + 1));
                for (var variable : variables) {
                    String varName = variable.name();
                    String varType = sctxTypeToTypeScriptType(variable.type());
                    // getters and setters to allow access to the variables inside the closure like
                    // a context object
                    output.format("%s    get %s() { return %s;},\n", Utils.indent(indentLevel + 1), varName, varName);
                    output.format("%s    set %s(value: %s) { %s = value;},\n", Utils.indent(indentLevel + 1), varName,
                            varType, varName);
                }
                output.format("%s};\n", Utils.indent(indentLevel + 1));
            }
        }

        // now add the regions and their states

        for (var region : regions) {
            processRegion(region, output, indentLevel + 1);
        }

        // and construct the list of regions for this state
        output.format("%s    const regions = [%s];\n", indent, regions.stream()
                .map(r -> r.getClassName())
                .collect(Collectors.joining(", ")));

        if (!entryActions.isEmpty()) {
            // outputMethodStart(output, indent, "void", "onEntry", "");
            try (var _ = new TypescriptFunction(output, indentLevel, "onEntry", "", KOptional.of("void"))) {
                processEntryExitActions(output, entryActions, indent);
            }
            // output.format("%s }\n", indent);
        }

        if (!duringActions.isEmpty()) {
            try (var function = new TypescriptFunction(output, indentLevel, "onTick", "", KOptional.of("void"))) {
                for (Action action : duringActions) {
                    KOptional<String> guard = action.guard();
                    String effect = action.action();
                    // Typescript will probably not support immediate actions, as to implement
                    // LeanCharts semantics.
                    // TODO: discuss if this is actually a reasonable limitation.
                    // boolean isImmediate = action.isImmediate();

                    // if (!isImmediate) {
                    // guard = KOptional.of(switch (guard) {
                    // case None<String> _ -> "delayedEnabled";
                    // case Some(var g) -> "delayedEnabled && (" + g + ")";
                    // });
                    // }

                    switch (guard) {
                        case Some(String g):
                            function.formatLine("if (%s) {", g);
                            function.formatLine("    %s;", effect);
                            function.formatLine("}");
                            break;
                        case None<String> _:
                            function.formatLine("%s;", effect);
                            break;
                    }
                }
            }
        }

        if (!exitActions.isEmpty()) {
            try (var _ = new TypescriptFunction(output, indentLevel, "onExit", "", KOptional.of("void"))) {
                processEntryExitActions(output, exitActions, indent);
            }
        }

        // TODO: unsupported for now, implement soon-ish
        // if (reference instanceof Some(var ref)) {
        // var parameters = ref.parameters();

        // try (var method = new JavaMethod(output, indentLevel, "void",
        // "copyVariablesIn", "")) {
        // for (String parameter : parameters) {
        // String[] parts = parameter.split("to");
        // String input_var = parts[0].strip();
        // String output_var = parts[1].strip();
        // method.formatLine("this.reference.%s = %s;", output_var, input_var);
        // }
        // }

        // try (var method = new JavaMethod(output, indentLevel, "void",
        // "copyVariablesOut", "")) {
        // for (String parameter : parameters) {
        // String[] parts = parameter.split("to");
        // String input_var = parts[0].strip();
        // String output_var = parts[1].strip();
        // method.formatLine("%s = this.reference.%s;", input_var, output_var);
        // }
        // }
        // }

        // construct the state object for this state

        output.format("%s    const model: State = new StateImpl(_isFinal, regions, {\n", indent);
        if (!entryActions.isEmpty()) {
            output.format("%s        onEntry,\n", indent);
        }
        if (!duringActions.isEmpty()) {
            output.format("%s        onTick,\n", indent);
        }
        if (!exitActions.isEmpty()) {
            output.format("%s        onExit,\n", indent);
        }
        if (!variables.isEmpty()) {
            output.format("%s        localReset,\n", indent);
        }
        output.format("%s    });\n", indent);

        if (isRoot) {
            output.format("%s    return { model, context };\n", indent);
            output.format("%s});\n", indent, "");
        } else {
            output.format("%s    return model;\n", indent);
            output.format("%s})(%b);\n", indent, state.isFinal());
        }

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

    private void processRegion(Region region, PrintStream output, int indentLevel) {
        System.out.println("Processing region: " + region.id());

        String id = region.id();
        String label = region.label();
        List<State> states = region.states();
        var connectorStateNames = states.stream().filter(State::isConnector).map(State::getClassName)
                .collect(Collectors.toSet());

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
                .filter(s -> !s.isConnector())
                .flatMap(state -> state.transitions().stream())
                .anyMatch(Transition::isImmediate);

        if (hasImmediateTransitions) {
            System.err.println("Warning: Region " + id
                    + " has immediate transitions. They are not supported by this code generator.");
        }

        String className = region.getClassName();
        String indent = "    ".repeat(indentLevel);

        output.format("\n%sconst %s = (function %s() {\n", indent, className, className);

        output.format("%s    // Label: %s\n\n", indent, label);

        // process all states in the region, including simple and complex states
        for (var state : states) {
            processState(state, output, indentLevel + 1, false);
        }

        output.append("\n");
        output.format("%s    const states: State[] = [%s];\n", indent, states.stream()
                .map(s -> s.getClassName())
                .collect(Collectors.joining(", ")));
        // TODO: this won't work with initial connectors. This should be fixed.
        output.format("%s    const initialState: State = %s;\n", indent, initialStateName);

        // process all strong abort transitions if there are any
        boolean hasStrongTransitions = strongTransitions.values().stream().anyMatch(list -> !list.isEmpty());
        if (hasStrongTransitions) {
            try (var function = new TypescriptFunction(output, indentLevel, "handlePreemptiveTransitions",
                    "this: RegionImpl", KOptional.of("boolean"))) {
                processTransitonMap(function, strongTransitions, connectorStateNames);
                function.addLine("return false;");
            }
        }

        // process all weak abort transitions if there are any
        boolean hasWeakTransitions = weakTransitions.values().stream().anyMatch(list -> !list.isEmpty());
        if (hasWeakTransitions) {
            try (var function = new TypescriptFunction(output, indentLevel, "handleNonPreemptiveTransitions",
                    "this: RegionImpl", KOptional.of("boolean"))) {
                processTransitonMap(function, weakTransitions, connectorStateNames);
                function.addLine("return false;");
            }
        }

        output.format("\n%s    const region: Region = new RegionImpl(states, initialState, {\n", indent);
        if (hasStrongTransitions) {
            output.format("%s        handlePreemptiveTransitions,\n", indent);
        }
        if (hasWeakTransitions) {
            output.format("%s        handleNonPreemptiveTransitions,\n", indent);
        }
        output.format("%s    });\n", indent);

        output.format("\n%s    return region;\n", indent);

        output.format("%s})()\n", indent);
    }

    private static void processTransitonMap(TypescriptFunction function, Map<String, List<Transition>> transitionMap,
            Set<String> connectorStateNames) {
        for (var entry : transitionMap.entrySet()) {
            String stateName = entry.getKey();
            if (connectorStateNames.contains(stateName)) {
                // skip connector states, they are not represented as a state object and thus
                // cannot be the active state
                continue;
            }
            List<Transition> transitions = entry.getValue();
            if (!transitions.isEmpty()) {
                function.formatLine("if (this.activeState === %s) {", stateName);
                generateTransitionStatements(function, connectorStateNames, transitions, transitionMap, 1);
                function.addLine("}");
            }
        }
    }

    private static void generateTransitionStatements(TypescriptFunction function, Set<String> connectorStateNames,
            List<Transition> transitions, Map<String, List<Transition>> transitionMap, int indentLevel) {
        for (var transition : transitions) {
            KOptional<String> guard = transition.guard();
            String target = Utils.formatClassName(transition.targetID());
            boolean targetIsConnector = connectorStateNames.contains(target);
            KOptional<String> effect = transition.action();

            boolean isTermination = transition.preemption().isTermination();

            if (isTermination) {
                guard = KOptional.of(switch (guard) {
                    case None<String> _ -> "this.activeState.isTerminated()";
                    case Some(var value) -> "this.activeState.isTerminated() && (" + value + ")";
                });
            }

            switch (guard) {
                case Some(var guardExpr):
                    function.formatLine("%sif (%s) {", Utils.indent(indentLevel), guardExpr);
                    if (targetIsConnector) {
                        // Transition to "null" as an intermediate step, to allow the current state to
                        // be left and the transition effect to execute, before the guards
                        // of the connector state's outgoing transitions are evaluated.
                        function.formatLine("%s    %s;", Utils.indent(indentLevel),
                                buildTransitionCommand("null", effect));
                        // TODO: this will currently only follow weak transitions from a connector state
                        // when it is entered by a weak transition,
                        // and weak transitions when it is entered by a strong transition. This is not
                        // correct, and needs to be fixed.
                        // There is no semantic difference between strong and weak transitions from a
                        // connector state, so we should follow all transitions regardless of the
                        // preemption type of the transition that led to the connector state.
                        // TODO: fix.
                        var connectorTransitions = transitionMap.get(target);
                        generateTransitionStatements(function, connectorStateNames, connectorTransitions, transitionMap,
                                indentLevel + 1);
                    } else {
                        function.formatLine("%s    %s;", Utils.indent(indentLevel),
                                buildTransitionCommand(target, effect));
                        function.formatLine("%s    return true;", Utils.indent(indentLevel));
                    }
                    function.formatLine("%s}", Utils.indent(indentLevel));
                    break;
                case None<String> _:
                    function.formatLine("%s%s;", Utils.indent(indentLevel), buildTransitionCommand(target, effect));
                    function.formatLine("%sreturn true;", Utils.indent(indentLevel));
            }
        }
    }

    private static String buildTransitionCommand(String target, KOptional<String> effect) {
        return switch (effect) {
            case None<String> _ -> String.format("this.transitionTo(%s)", target);
            case Some(var effect_string) ->
                String.format("this.transitionTo(%s, () => { %s; })", target, effect_string);
        };
    }

    private static String sctxTypeToTypeScriptType(String sctxType) {
        // float is mapped to double, because we probably have the memory.
        return switch (sctxType) {
            case "int" -> "number";
            case "bool" -> "boolean";
            case "string" -> "string";
            case "float" -> "number";
            case "double" -> "number";
            default -> "any"; // TODO: Handle unknown types more gracefully, e.g., by generating a custom
                              // class or throwing an error.
        };
    }

}

class TypescriptFunction implements AutoCloseable {
    private final PrintStream output;
    private final int indentLevel;
    private boolean closed = false;

    public TypescriptFunction(PrintStream output, int indentLevel, String methodName, String methodArgs,
            KOptional<String> returnType) {
        this.output = output;
        this.indentLevel = indentLevel;
        output.append("\n");
        output.append(Utils.indent(indentLevel + 1));
        output.format("const %s = function %s(%s)%s {\n", methodName, methodName, methodArgs,
                returnType.map(rt -> ": " + rt).orElse(""));
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
            output.append("};\n");
        }
    }
}

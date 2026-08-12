

package de.cau.cs.kieler.kicooo.model;

import java.util.List;

import de.cau.cs.kieler.kicooo.KOptional;
import de.cau.cs.kieler.kicooo.Utils;
import mjson.Json;


public final record State(
    String id,
    String label,
    List<Action> entryActions,
    List<Action> exitActions,
    List<Action> duringActions,
    List<Transition> transitions,
    List<Variable> variables,
    boolean isInitial,
    boolean isFinal,
    boolean isConnector,
    List<Region> regions,
    KOptional<Reference> reference) {
        
    public boolean isComplex() {
        boolean hasActions = !entryActions.isEmpty() || !exitActions.isEmpty() || !duringActions.isEmpty();
        boolean hasRegions = !regions.isEmpty();
        boolean hasReference = reference.isPresent();
        return hasActions || hasRegions || hasReference;
    }

    public String getClassName() {
        return Utils.formatClassName(id);
    }

    public static State fromJson(Json json) {
        String id = Utils.getJsonStringByKey(json, "id")
                .orElseThrow(() -> new IllegalArgumentException("State is missing required 'id' field."));
        String label = Utils.getJsonStringByKey(json, "label").orElse(id);
            
        List<Action> actions = Utils.getRequiredJsonListByKey(json, "actions", "State")
                .stream().map(Action::fromJson).toList();
            
        List<Action> entryActions = actions.stream()
                .filter(action -> action.type().equals(Action.ActionType.ENTRY))
                .toList();
        List<Action> exitActions = actions.stream()
                .filter(action -> action.type().equals(Action.ActionType.EXIT))
                .toList();
        List<Action> duringActions = actions.stream()
                .filter(action -> action.type().equals(Action.ActionType.DURING))
                .toList();
        
        List<Transition> transitions = Utils.getRequiredJsonListByKey(json, "transitions", "State")
                .stream().map(Transition::fromJson).toList();

        List<Variable> variables = Utils.getRequiredJsonListByKey(json, "variables", "State")
            .stream().map(Variable::fromJson).toList();

        boolean isInitial = Utils.getRequiredJsonBooleanByKey(json, "isInitial", "State");
        boolean isFinal = Utils.getRequiredJsonBooleanByKey(json, "isFinal", "State");
        boolean isConnector = Utils.getRequiredJsonBooleanByKey(json, "isConnector", "State");
        
        List<Region> regions = Utils.getRequiredJsonListByKey(json, "regions", "State")
                .stream().map(Region::fromJson).toList();

        KOptional<Reference> reference = KOptional.ofNullable(json.at("reference")).map(Reference::fromJson);

        return new State(
            id,
            label,
            entryActions,
            exitActions,
            duringActions,
            transitions,
            variables,
            isInitial,
            isFinal,
            isConnector,
            regions,
            reference
        );
    }

    public static record Reference(String target, List<String> parameters) {
    
        public static Reference fromJson(Json json) {
            String target = Utils.getRequiredJsonStringByKey(json, "targetID", "Reference");
            List<String> parameters = Utils.getRequiredJsonListByKey(json, "parameters", "Reference")
                    .stream().map(Json::asString).toList();
            return new Reference(target, parameters);
        }
    }
}

package de.cau.cs.kieler.kicooo.model;

import de.cau.cs.kieler.kicooo.KOptional;
import de.cau.cs.kieler.kicooo.Utils;
import mjson.Json;

public record Action(KOptional<String> label, ActionType type, boolean isImmediate, KOptional<String> guard, String action) {

    public static Action fromJson(Json json) {
        KOptional<String> label = KOptional.over(Utils.getJsonStringByKey(json, "label"));
        ActionType type = ActionType.fromJsonAction(json);
        boolean isImmediate = Utils.getRequiredJsonBooleanByKey(json, "isImmediate", "Action");
        KOptional<String> guard = KOptional.over(Utils.getJsonStringByKey(json, "guard"));
        String action = Utils.getRequiredJsonStringByKey(json, "action", "Action");
        return new Action(label, type, isImmediate, guard, action);
    }

    public static enum ActionType {
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
            String typeStr = Utils.getRequiredJsonStringByKey(action, "type", "Action");
            return fromString(typeStr);
        }
}
}

package de.cau.cs.kieler.kicooo.model;

import de.cau.cs.kieler.kicooo.KOptional;
import de.cau.cs.kieler.kicooo.Utils;
import mjson.Json;

public final record Transition(KOptional<String> label, String targetID, boolean isImmediate, PreemptionType preemption, boolean history, KOptional<String> guard, KOptional<String> action) {
    
    public static Transition fromJson(Json json) {
        KOptional<String> label = KOptional.over(Utils.getJsonStringByKey(json, "label"));
        String targetID = Utils.getRequiredJsonStringByKey(json, "targetID", "Transition");
        boolean isImmediate = Utils.getRequiredJsonBooleanByKey(json, "isImmediate", "Transition");
        PreemptionType preemption = PreemptionType.fromJsonTransition(json);
        boolean history = Utils.getRequiredJsonBooleanByKey(json, "history", "Transition");
        KOptional<String> guard = KOptional.over(Utils.getJsonStringByKey(json, "guard"));
        KOptional<String> action = KOptional.over(Utils.getJsonStringByKey(json, "action"));
        return new Transition(label, targetID, isImmediate, preemption, history, guard, action);
    }

    public static enum PreemptionType {
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
            String preemptionStr = Utils.getRequiredJsonStringByKey(transition, "preemption");
            return fromString(preemptionStr);
        }

        public boolean isTermination() {
            return this == TERMINATION;
        }

        public boolean isStrong() {
            return this == STRONG;
        }
    }
}
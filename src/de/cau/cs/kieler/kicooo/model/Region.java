package de.cau.cs.kieler.kicooo.model;

import java.util.List;

import de.cau.cs.kieler.kicooo.Utils;
import mjson.Json;

public final record Region(String id, String label, List<State> states, State initialState) {

    public String getClassName() {
        return Utils.formatClassName(id);
    }

    public static Region fromJson(Json json) {
        String id = Utils.getRequiredJsonStringByKey(json, "id", "Region");
        String label = Utils.getRequiredJsonStringByKey(json, "label", "Region");
        List<State> states = Utils.getRequiredJsonListByKey(json, "states", "Region")
                .stream().map(State::fromJson).toList();

        var initialStates = states.stream()
                .filter(State::isInitial)
                .toList();
        if (initialStates.size() != 1) {
            throw new IllegalArgumentException("Region " + id + " must have exactly one initial state.");
        }
        State initialState = initialStates.getFirst();
        
        return new Region(id, label, states, initialState);
    }
}
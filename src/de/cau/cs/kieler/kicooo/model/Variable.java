package de.cau.cs.kieler.kicooo.model;

import java.util.List;
import java.util.Optional;

import de.cau.cs.kieler.kicooo.Utils;
import mjson.Json;

public final record Variable(String name, String type, Optional<Object> initialValue, boolean isInput, boolean isOutput, List<Integer> cardinalities) {

    public static Variable fromJson(Json json) {
        String name = Utils.getRequiredJsonStringByKey(json, "id", "Variable");
        String type = Utils.getRequiredJsonStringByKey(json, "type", "Variable");
        Optional<Object> initialValue = Optional.ofNullable(json.at("initialValue")).map(Utils::extractPrimitiveValue);
        boolean isInput = Utils.getRequiredJsonBooleanByKey(json, "isInput", "Variable");
        boolean isOutput = Utils.getRequiredJsonBooleanByKey(json, "isOutput", "Variable");
        List<Integer> cardinalities = Utils.getRequiredJsonListByKey(json, "cardinalities", "Variable")
                .stream().map(Json::asInteger).toList();
        return new Variable(name, type, initialValue, isInput, isOutput, cardinalities);
    }
}
package de.cau.cs.kieler.kicooo;

import java.util.List;
import java.util.Optional;

import mjson.Json;

public class Utils {

    static Optional<List<Json>> getJsonListByKey(Json json, String key) {
        return Optional.ofNullable(json.at(key)).map(Json::asJsonList);
    }

    static Optional<String> getJsonStringByKey(Json json, String key) {
        return Optional.ofNullable(json.at(key)).map(Json::asString);
    }

    static Optional<Boolean> getJsonBooleanByKey(Json transition, String key) {
        return Optional.ofNullable(transition.at(key)).map(Json::asBoolean);
    }

    static String uppercaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    static String formatClassName(String id) {
        return uppercaseFirst(id.replaceAll("[^a-zA-Z0-9]", "_"));
    }

    static String indent(int level) {
        return "    ".repeat(level);
    }

}

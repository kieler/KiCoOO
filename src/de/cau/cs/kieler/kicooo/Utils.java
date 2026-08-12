package de.cau.cs.kieler.kicooo;

import java.util.List;
import java.util.Optional;

import mjson.Json;

public class Utils {

    public static Optional<List<Json>> getJsonListByKey(Json json, String key) {
        return Optional.ofNullable(json.at(key)).map(Json::asJsonList);
    }
    
    public static List<Json> getRequiredJsonListByKey(Json json, String key) {
        return getRequiredJsonListByKey(json, key, "JSON");
    }

    public static List<Json> getRequiredJsonListByKey(Json json, String key, String what) {
        return getJsonListByKey(json, key).orElseThrow(() -> new IllegalArgumentException(what + " is missing required field '" + key + "'."));
    }

    public static Optional<String> getJsonStringByKey(Json json, String key) {
        return Optional.ofNullable(json.at(key)).filter(Json::isString).map(Json::asString);
    }

    public static String getRequiredJsonStringByKey(Json json, String key) {
        return getRequiredJsonStringByKey(json, key, "JSON");
    }

    public static String getRequiredJsonStringByKey(Json json, String key, String what) {
        return getJsonStringByKey(json, key).orElseThrow(() -> new IllegalArgumentException(what + " is missing required field '" + key + "'."));
    }

    public static Optional<Boolean> getJsonBooleanByKey(Json json, String key) {
        return Optional.ofNullable(json.at(key)).filter(Json::isBoolean).map(Json::asBoolean);
    }

    public static boolean getRequiredJsonBooleanByKey(Json json, String key) {
        return getRequiredJsonBooleanByKey(json, key, "JSON");
    }

    public static boolean getRequiredJsonBooleanByKey(Json json, String key, String what) {
        return getJsonBooleanByKey(json, key).orElseThrow(() -> new IllegalArgumentException(what + " is missing required boolean field '" + key + "'."));
    }

    public static String uppercaseFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String formatClassName(String id) {
        return uppercaseFirst(id.replaceAll("[^a-zA-Z0-9]", "_"));
    }

    public static String indent(int level) {
        return "    ".repeat(level);
    }

    public static Object extractPrimitiveValue(Json v) {
        if (v.isBoolean()) {
            return v.asBoolean();
        } else if (v.isNumber()) {
            return v.asInteger();
        } else if (v.isString()) {
            return v.asString();
        } else {
            return v.getValue(); // Return the raw value for other types (e.g., arrays, objects)
        }
    }
}

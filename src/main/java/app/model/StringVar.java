package app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringVar {
    final String name;
    final String value;

    public StringVar(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static List<StringVar> vars(String... variables) {
        return new ArrayList<StringVar>(){{
            for (int i = 0; i < variables.length; i=i+2) {
                add(new StringVar(variables[i], variables[i+1]));
            }
        }};
    }

    public static Map<String, String> maps(String... variables) {
        return new HashMap<String, String>(){{
            for (int i = 0; i < variables.length; i=i+2) {
                put(variables[i], variables[i+1]);
            }
        }};
    }

    public static String surroundWithBraces(String value) {
        return "{" + value + "}";
    }

    public static String joinWithComma(String... values) {
        return String.join(",", values);
    }

    public static String joinWithComma(List<String> values) {
        return joinWithComma(values.toArray(new String[0]));
    }

    public static String superKeyValuePair(String key, String value) {
        return "\"" + key + "\":{" + value + "}";
    }

    public static String keyValuePair(String key, String value) {
        return keyValuePair(key, value, true);
    }

    public static String keyValuePair(StringVar var) {
        return keyValuePair(var, true);
    }

    private static String keyValuePair(StringVar var, boolean noBraces) {
        return keyValuePair(var.name, var.value, noBraces);
    }

    private static String keyValuePair(String key, String value, boolean noBraces) {
        if (noBraces) {
            return "\"" + key + "\":\"" + value + "\"";
        }

        return surroundWithBraces(key + ":" + value);
    }

}

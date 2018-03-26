package app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringVar {
    public static final String CURLY = "curly";
    public static final String SQUARE = "square";
    final String name;
    final String value;

    public StringVar(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static List<StringVar> Vars(String... variables) {
        return new ArrayList<StringVar>() {{
            for (int i = 0; i < variables.length; i = i + 2) {
                add(new StringVar(variables[i], variables[i + 1]));
            }
        }};
    }

    public static Map<String, String> Maps(String... variables) {
        return new HashMap<String, String>() {{
            for (int i = 0; i < variables.length; i = i + 2) {
                put(variables[i], variables[i + 1]);
            }
        }};
    }

    public static String SurroundWithBraces(String value) {
        return SurroundWithBraces(value, CURLY);
    }

    public static String SurroundWithBraces(String value, String type) {
        if (type.equals(SQUARE)) {
            return "[" + value + "]";
        }
        return "{" + value + "}";
    }

    public static String SurroundWithQuotes(String value) {
        return "\"" + value + "\"";
    }

    public static String JoinWith(String delim, String... values) {
        return String.join(delim, values);
    }

    public static String JoinWith(String delim, List<String> values) {
        return JoinWith(delim, values.toArray(new String[0]));
    }

    public static String JoinWithComma(String... values) {
        return JoinWith(",", values);
    }

    public static String JoinWithComma(List<String> values) {
        return JoinWith(",", values);
    }

    public static String SuperKeyValuePair(String key, String value) {
        return "\"" + key + "\":{" + value + "}";
    }

    public static String KeyValuePair(String key, String value) {
        return KeyValuePair(key, value, true);
    }

    public static String KeyValuePair(StringVar var) {
        return KeyValuePair(var, true);
    }
    public static String KeyArrayValuePair(StringVar var) {
        return "\"" + var.name + "\":" + var.value + "";
    }

    private static String KeyValuePair(StringVar var, boolean noBraces) {
        return KeyValuePair(var.name, var.value, noBraces);
    }

    private static String KeyValuePair(String key, String value, boolean noBraces) {
        if (noBraces) {
            return "\"" + key + "\":\"" + value + "\"";
        }

        return SurroundWithBraces(key + ":" + value);
    }

    public static String StripFirstAndLast(String text) {
        return text.substring(1,text.length()-1);
    }

    public static String StripQuotes(String text) {
        return StripFirstAndLast(text);
    }

    public static String StripSquareBraces(String text) {
        return StripFirstAndLast(text);
    }

    public static String StripBraces(String text) {
        return StripFirstAndLast(text);
    }

    public static String extractStringKeyFromJson(String key, String json) {
        return json.split("\""+key+"\":\"", 2)[1].split("\"", 2)[0];
    }
    public static String extractArrayKeyFromJson(String key, String json) {
        return SurroundWithBraces(json.split("\""+key+"\":\\[", 2)[1].split("]", 2)[0], "square");
    }

}

package app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static app.model.StringVar.*;

public class VariableManager {
    final List<StringVar> fields = new ArrayList<>();
    final Map<String, String> fieldMap = new HashMap<>();

    VariableManager(String... keyValues) {
        this.fields.addAll(Vars(keyValues));
        this.fieldMap.putAll(Maps(keyValues));
    }

    public String jsonString() {
        return SurroundWithBraces(JoinWithComma(fields.stream().map(x -> KeyValuePair(x)).collect(Collectors.toList())));
    }

    public String get(String varName) {
        return fieldMap.get(varName);
    }

    public void set(String varName, String value) {
        fieldMap.replace(varName, value);

        List<StringVar> newFields = fields.stream().map(var -> {
            if (var.name.equals(varName)) {
                var = new StringVar(varName, value);
            }
            return var;
        }).collect(Collectors.toList());
        fields.clear();
        fields.addAll(newFields);

    }

    public void add(StringVar var) {
        fields.add(var);
        fieldMap.put(var.name, var.value);
    }
}


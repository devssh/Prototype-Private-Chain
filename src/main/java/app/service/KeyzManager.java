package app.service;

import app.model.Keyz;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyzManager {
    List<Keyz> keyz;
    final String keyFile;

    public KeyzManager(String keyFile) throws Exception {
        this.keyFile = keyFile;
        keyz = getKeys(true);
    }

    private List<Keyz> getKeys(boolean eager) throws Exception {
        if (eager) {
            List<String> keyStrings = Files.readAllLines(Paths.get(keyFile));
            List<Keyz> keys = new ArrayList<>();
            for (int i = 0; i < keyStrings.size(); i = i + 3) {
                keys.add(new Keyz(keyStrings.get(i), keyStrings.get(i + 1), keyStrings.get(i + 2)));
            }
            return keys;
        }

        return keyz;
    }


    public Keyz getKey(String owner) throws Exception {
        List<Keyz> keyzFound = keyz.stream().filter(keyzX -> keyzX.owner.equals(owner)).collect(Collectors.toList());

        if (keyzFound.size() == 1) {
            return keyzFound.get(0);
        }

        if (keyzFound.size() > 1) {
            throw new Exception("Too many keyz with that owner");
        }

        List<Keyz> newKeyz = getKeys(true);
        if (newKeyz.size() > keyz.size()) {
            keyz = newKeyz;
            return getKey(owner);
        }

        throw new Exception("Invalid key owner");

    }

    public void writeKey(String owner) {

    }
}

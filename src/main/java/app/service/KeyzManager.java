package app.service;

import app.model.Keyz;

import java.util.List;
import java.util.stream.Collectors;

import static app.service.FileUtils.ReadKeys;

public class KeyzManager {
    public static List<Keyz> Keys = ReadKeys();

    public KeyzManager() throws Exception {
    }

    public static Keyz GetKey(String owner) throws Exception {
        List<Keyz> keyzFound = Keys.stream().filter(keyzX -> keyzX.owner.equals(owner)).collect(Collectors.toList());

        if (keyzFound.size() == 1) {
            return keyzFound.get(0);
        }

        if (keyzFound.size() > 1) {
            throw new Exception("Too many keyz with that owner");
        }

        List<Keyz> newKeyz = ReadKeys();
        if (newKeyz.size() > Keys.size()) {
            Keys.clear();
            Keys.addAll(newKeyz);
            return GetKey(owner);
        }

        throw new Exception("Invalid key owner");

    }

    public static Keyz GetOwner(String publicKey) throws Exception {
        List<Keyz> keyzFound = Keys.stream().filter(keyzX -> keyzX.publicKey.equals(publicKey)).collect(Collectors.toList());

        if (keyzFound.size() == 1) {
            return keyzFound.get(0);
        }

        if (keyzFound.size() > 1) {
            throw new Exception("Too many owners for that key");
        }

        List<Keyz> newKeyz = ReadKeys();
        if (newKeyz.size() > Keys.size()) {
            Keys.clear();
            Keys.addAll(newKeyz);
            return GetOwner(publicKey);
        }

        throw new Exception("Invalid key owner");
    }

    public void writeKey(String owner) {
        //TODO: Using Keyz.generateKey
    }
}

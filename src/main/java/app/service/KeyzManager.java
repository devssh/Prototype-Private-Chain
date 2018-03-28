package app.service;

import app.model.Keyz;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KeyzManager {
    public static List<Keyz> Keys = GetKeys(app.utils.Properties.USERS_DAT, app.utils.Properties.AUTHORITIES_DAT);
    public static final List<Keyz> Users = GetKeys(app.utils.Properties.USERS_DAT);
    public static final List<Keyz> Authorities = GetKeys(app.utils.Properties.AUTHORITIES_DAT);

    public KeyzManager() throws Exception {
    }

    private static List<Keyz> GetKeys(String... keyFiles) {
        List<Keyz> keys = new ArrayList<>();
        for (String keyFile : keyFiles) {
            List<String> keyStrings = FileUtils.ReadFile(keyFile);
            for (int i = 0; i < keyStrings.size(); i = i + 3) {
                try {
                    keys.add(new Keyz(keyStrings.get(i), keyStrings.get(i + 1), keyStrings.get(i + 2)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return keys;

    }

    public static Keyz GetKey(String owner) throws Exception {
        List<Keyz> keyzFound = Keys.stream().filter(keyzX -> keyzX.owner.equals(owner)).collect(Collectors.toList());

        if (keyzFound.size() == 1) {
            return keyzFound.get(0);
        }

        if (keyzFound.size() > 1) {
            throw new Exception("Too many keyz with that owner");
        }

        List<Keyz> newKeyz = GetKeys();
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

        List<Keyz> newKeyz = GetKeys();
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

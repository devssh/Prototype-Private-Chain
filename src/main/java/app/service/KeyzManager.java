package app.service;

import app.model.Keyz;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class KeyzManager {
    public static List<Keyz> Keys;
    public static List<Keyz> Users;
    public static List<Keyz> Authorities;

    public KeyzManager() throws Exception {
        Keys = GetKeys(true, app.utils.Properties.USERS_DAT, app.utils.Properties.AUTHORITIES_DAT);
        Users = GetKeys(true, app.utils.Properties.USERS_DAT);
        Authorities = GetKeys(true, app.utils.Properties.AUTHORITIES_DAT);
    }

    private static List<Keyz> GetKeys(boolean eager, String... keyFiles) throws Exception {
        if (eager) {
            List<Keyz> keys = new ArrayList<>();
            for (String keyFile : keyFiles) {
                List<String> keyStrings = FileUtils.readFile(keyFile);
                for (int i = 0; i < keyStrings.size(); i = i + 3) {
                    keys.add(new Keyz(keyStrings.get(i), keyStrings.get(i + 1), keyStrings.get(i + 2)));
                }
            }
            return keys;
        }

        return Keys;
    }


    public static Keyz GetKey(String owner) throws Exception {
        List<Keyz> keyzFound = Keys.stream().filter(keyzX -> keyzX.owner.equals(owner)).collect(Collectors.toList());

        if (keyzFound.size() == 1) {
            return keyzFound.get(0);
        }

        if (keyzFound.size() > 1) {
            throw new Exception("Too many keyz with that owner");
        }

        List<Keyz> newKeyz = GetKeys(true);
        if (newKeyz.size() > Keys.size()) {
            Keys = newKeyz;
            return GetKey(owner);
        }

        throw new Exception("Invalid key owner");

    }

    public void writeKey(String owner) {
        //TODO: Using Keyz.generateKey
    }
}

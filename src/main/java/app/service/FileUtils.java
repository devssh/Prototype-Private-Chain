package app.service;

import app.model.Block;
import app.model.Keyz;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static app.model.StringVar.ArrayOfObjects;

public class FileUtils {
    private static final String USERS_DAT = "users.dat";
    private static final String BLOCKS_DAT = "blocks.dat";

    public static List<Block> ReadBlockchain() {
        try {
            String blockchain = Files.readAllLines(Paths.get(BLOCKS_DAT)).get(0);
            return Arrays.asList(blockchain.substring(1, blockchain.length() - 1)
                    .split("\\{\"blockSign\":", -1)).stream().filter(str -> str.length() > 1)
                    .map(str -> {
                        if (str.endsWith(",")) {
                            return "{\"blockSign\":" + str.substring(0, str.length() - 1);
                        }
                        return "{\"blockSign\":" + str;
                    }).map(Block::Deserialize).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<Keyz> ReadKeys() {
        try {
            String users = Files.readAllLines(Paths.get(USERS_DAT)).get(0);
            return Arrays.asList(users.substring(1, users.length() - 1)
                    .split("\\{\"name\":", -1)).stream().filter(str -> str.length() > 1)
                    .map(str -> {
                        if (str.endsWith(",")) {
                            return "{\"name\":" + str.substring(0, str.length() - 1);
                        }
                        return "{\"name\":" + str;
                    }).map(Keyz::Deserialize).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public static void AppendBlocks(String... blocks) throws Exception {
        //TODO: better way to do this
        Writer output = new BufferedWriter(new FileWriter(BLOCKS_DAT, true));
        output.append(ArrayOfObjects(blocks));
        output.flush();
        output.close();
    }
}

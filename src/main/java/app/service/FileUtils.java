package app.service;

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

import static app.model.StringVar.*;
import static app.utils.BlockManager.GetBlocks;
import static app.utils.Properties.BLOCKS_DAT;

public class FileUtils {
    public static List<String> ReadFile(String filename) {
        try {
            String blockchain = Files.readAllLines(Paths.get(filename)).get(0);
            return Arrays.asList(blockchain.substring(1, blockchain.length() - 1)
                    .split("\\{\"blockSign\":", -1)).stream().filter(str -> str.length() > 1)
                    .map(str -> {
                        if (str.endsWith(",")) {
                            return "{\"blockSign\":" + str.substring(0, str.length() - 1);
                        }
                        return "{\"blockSign\":" + str;
                    }).collect(Collectors.toList());
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

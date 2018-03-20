package app.utils;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;
import app.service.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static app.utils.Properties.BLOCKS_DAT;

public class BlockManager {

    public static boolean IsValid(String block) throws Exception {
        return Block.Deserialize(block).verify();
    }

    public static List<String> GetBlocks() {
        return FileUtils.readFile(BLOCKS_DAT);
    }

    public static List<Block> GetBlockObjects() throws Exception {
        List<String> blockStrings = Files.readAllLines(Paths.get(BLOCKS_DAT));
        List<Block> blocks = new ArrayList<>();
        for (String blockString : blockStrings) {
            blocks.add(Block.Deserialize(blockString));

        }
        return blocks;
    }


    public static List<String> AppendBlocks(String... blocks) throws Exception {
        //TODO: better way to do this
        Writer output = new BufferedWriter(new FileWriter(BLOCKS_DAT, true));
        for (String block : blocks) {
            output.append(block + "\n");
            output.flush();
        }

        output.close();

        return GetBlocks();
    }

//  TODO: Write helper for these
//    public String initBlockchain(String keyFile, String message, String owner, String aadhar) throws Exception {
//        Keyz keysMiner = authoritiesManager.getKey("Miner"),
//                keysDev = authoritiesManager.getKey("Dev"),
//                keysRajiv = authoritiesManager.getKey("Rajiv");
//
//        String genesis = createGenesisBlock(keysMiner, message, owner, aadhar);
//
//        return SurroundWithBraces(JoinWithComma(genesis));
//    }

//
//    public String createGenesisBlock(Keyz key, Txn txn) throws Exception {
//        return createForm(key,"", "");
//    }

    public static String CreateBlock(Keyz key, String previousHash, Txn txn) throws Exception {
        return new Block(key, previousHash, txn).toString();
    }


    public static String ExtractSignature(String block) throws Exception {
        //Pattern signature = Pattern.compile(".*\"Sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return Block.Deserialize(block).sign;
    }


}

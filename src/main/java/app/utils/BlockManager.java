package app.utils;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static app.model.Block.Deserialize;
import static app.service.FileUtils.ReadFile;
import static app.utils.Properties.BLOCKS_DAT;

public class BlockManager {

    public static boolean IsValid(String block) throws Exception {
        return Deserialize(block).verify();
    }

    public static List<String> GetBlocks() {
        return ReadFile(BLOCKS_DAT);
    }

    public static List<Txn> GetTxns() {
        List<Txn> txns = new CopyOnWriteArrayList<>();
        ReadFile(BLOCKS_DAT).stream().forEach(blockString -> {
            try {
                txns.addAll(Arrays.asList(Deserialize(blockString).txns));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return txns;
    }

    public static List<Block> GetBlockObjects() throws Exception {
        List<String> blockStrings = ReadFile(BLOCKS_DAT);
        List<Block> blocks = new ArrayList<>();
        for (String blockString : blockStrings) {
            blocks.add(Deserialize(blockString));

        }
        return blocks;
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
//        return redeemForm(key,"", "");
//    }

    public static String CreateBlock(Keyz key, String previousHash, Txn txn) throws Exception {
        return new Block(key, previousHash, txn).toString();
    }


    public static String ExtractSignature(String block) throws Exception {
        //Pattern signature = Pattern.compile(".*\"Sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return Deserialize(block).sign;
    }


}

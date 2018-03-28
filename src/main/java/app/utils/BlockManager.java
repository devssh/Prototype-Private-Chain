package app.utils;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static app.model.Block.Deserialize;
import static app.service.FileUtils.ReadBlockchain;

public class BlockManager {


    public static List<Txn> GetFlattenedTxns() {
        List<Txn> txns = new CopyOnWriteArrayList<>();
        ReadBlockchain().stream().forEach(block -> {
            try {
                txns.addAll(Arrays.asList(block.txns));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return txns;
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

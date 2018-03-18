package app.utils;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;
import app.service.KeyzManager;
import app.service.SignService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BlockManager {
    KeyzManager authorities;
    SignService signService;
    KeyzManager users;

    public BlockManager(KeyzManager authorities, KeyzManager users) throws Exception {
        this.authorities = authorities;
        this.users = users;
        this.signService = new SignService(authorities, users);
    }

    public boolean isValid(String block) throws Exception {
        return Block.Deserialize(block).verify();
    }

    public List<String> getBlocks(String blockFile) throws Exception {
        return Files.readAllLines(Paths.get(blockFile));
    }


    public List<String> appendBlocks(String blockFile, String... blocks) throws Exception {
        //TODO: better way to do this
        Writer output = new BufferedWriter(new FileWriter(blockFile, true));
        for (String block : blocks) {
            output.append(block + "\n");
            output.flush();
        }

        output.close();

        return getBlocks(blockFile);
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
//        return createBlock(key,"", "");
//    }

    public String createBlock(Keyz key, String previousHash, Txn txn) throws Exception {
        return new Block(key, previousHash, txn).toString();
    }


    public String extractSignature(String block) throws Exception {
        //Pattern signature = Pattern.compile(".*\"Sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return Block.Deserialize(block).sign;
    }

}

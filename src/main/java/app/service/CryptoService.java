package app.service;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;
import app.utils.BlockManager;

import java.util.List;

import static app.model.StringVar.*;
import static app.utils.BlockManager.IsValid;

public class CryptoService {
    String messageKey = "message";
    String ownerKey = "owner";
    String aadharKey = "aadhar";
    int difficulty = 3;
    String difficultyCharacter = "0";


    String blockFile;

    List<Keyz> keys;
    Keyz keysMiner,
            keysDev,
            keysRajiv;

    public KeyzManager authoritiesManager, usersManager;
    BlockManager blockManager;

    public CryptoService(String authoritiesFile, String usersFile, String blockFile) throws Exception {
        this.authoritiesManager = new KeyzManager(authoritiesFile);
        this.usersManager = new KeyzManager(usersFile);
        this.blockManager = new BlockManager(authoritiesManager, usersManager);
        this.blockFile = blockFile;
        this.keysMiner = authoritiesManager.getKey("Miner");
        this.keysDev = authoritiesManager.getKey("Dev");
        this.keysRajiv = authoritiesManager.getKey("Rajiv");
    }


    public String getBlockchain() throws Exception {
        List<String> blocks = blockManager.getBlocks(blockFile);

        return SurroundWithBraces(JoinWithComma(blocks));
    }

    public Block addBlock(Txn txn, String signedBy) throws Exception {
        List<String> blocks = blockManager.getBlocks(blockFile);
        String prevHash = blockManager.extractSignature(blocks.get(blocks.size() - 1));

        Block block = new Block(authoritiesManager.getKey(signedBy), prevHash, txn);

        List<String> newBlocks = blockManager.getBlocks(blockFile);
        String newPrevHash = blockManager.extractSignature(newBlocks.get(newBlocks.size() - 1));


        if (!newPrevHash.equals(prevHash)) {
            return addBlock(txn, signedBy);
        } else {
            blockManager.appendBlocks(blockFile, block.toString());
            return block;
        }

    }

    public String showAuthorized() {
        return SurroundWithBraces(JoinWithComma(
                authoritiesManager.keyz.stream().map(key -> SuperKeyValuePair(key.owner, KeyValuePair("publicKey", key.publicKey))).toArray(String[]::new)
        ));
    }


    public String verifyAllSignatures() throws Exception {
        List<String> blocks = blockManager.getBlocks(blockFile);

        String genesis = blocks.get(0);
        boolean isValid = Block.Deserialize(genesis).verify();

        for (int i = 1; i < blocks.size(); i++) {
            String block = blocks.get(i);
            isValid = isValid & IsValid(block);
        }

        return String.valueOf(isValid);
    }

//
//    public String showGenesis(String message, String owner, String aadhar) throws Exception {
//        return blockManager.createGenesisBlock(keysDev, message, owner, aadhar);
//    }

    public String generateKeyString() throws Exception {
        Keyz key = Keyz.generateKey();
        return "Public Key: " + key.publicKey +
                "\nPrivate Key: " + key.privateKey;
    }

    public String getStats() {
        return "Difficulty: " + difficulty +
                "\nHashRate: 1GH/Sec" +
                "\nCost per transaction(block): $0.00038" +
                "\nBlock Time: 5 seconds on average";
    }
}

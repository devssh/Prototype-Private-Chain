package app.service;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static app.model.StringVar.*;
import static app.service.KeyzManager.GetKey;
import static app.service.KeyzManager.Users;
import static app.utils.BlockManager.*;

public class CryptoService {
    int difficulty = 3;
    String difficultyCharacter = "0";


    public CryptoService() throws Exception {
    }


    public String getBlockchain() throws Exception {
        List<Block> blocks = GetBlockObjects();

        return SurroundWithBraces(JoinWithComma(blocks.stream().map(Block::toString).collect(Collectors.toList())));
    }

    public Block addBlock(String signedBy, Txn txn) throws Exception {
        List<Block> blocks = GetBlockObjects();
        String prevHash = blocks.get(blocks.size() - 1).sign;

        if (blocks.stream().filter(block ->
                Arrays.stream(block.txnids).filter(txnid ->
                        txn.varMan.get("txnid").equals(txnid)
                ).collect(Collectors.toList()).size() == 1
        ).collect(Collectors.toList()).size() == 1) {
            throw new Exception("Cannot double spend");
        }

        Block block = new Block(GetKey(signedBy), prevHash, txn);

        List<String> newBlocks = GetBlocks();
        String newPrevHash = ExtractSignature(newBlocks.get(newBlocks.size() - 1));


        if (!newPrevHash.equals(prevHash)) {
            return addBlock(signedBy, txn);
        } else {
            AppendBlocks(block.toString());
            return block;
        }

    }

    public String showAuthorized() {
        return SurroundWithBraces(JoinWithComma(
                KeyzManager.Keys.stream().map(key -> SuperKeyValuePair(key.owner, KeyValuePair("publicKey", key.publicKey))).toArray(String[]::new)
        ));
    }

    public String showUsers() {
        return SurroundWithBraces(JoinWithComma(
                Users.stream().map(key -> SuperKeyValuePair(key.owner, KeyValuePair("publicKey", key.publicKey))).toArray(String[]::new)
        ));
    }


    public String verifyAllSignatures() throws Exception {
        List<String> blocks = GetBlocks();

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

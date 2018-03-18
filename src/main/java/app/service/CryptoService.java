package app.service;

import app.utils.CryptoUtils;
import javafx.util.Pair;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

public class CryptoService {
    CryptoUtils cryptoUtils = new CryptoUtils();
    String messageKey = "message";
    String ownerKey = "owner";
    String aadharKey = "aadhar";
    int difficulty = 3;
    String difficultyCharacter = "0";


    String keyFile;
    String blockFile;

    List<String> keys;
    Pair<String, String> keysMiner,
            keysDev,
            keysRajiv;


    public CryptoService(String keyFile, String blockFile) throws Exception {
        this.keyFile = keyFile;
        this.blockFile = blockFile;
        this.keys = Files.readAllLines(Paths.get(keyFile));
        this.keysMiner = new Pair<>(keys.get(0), keys.get(1));
        this.keysDev = new Pair<>(keys.get(2), keys.get(3));
        this.keysRajiv = new Pair<>(keys.get(4), keys.get(5));
    }


    public String getBlockchain() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get(keyFile));
        Pair<String, String> keysMiner = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        return cryptoUtils.surroundWithBraces(cryptoUtils.addComma(blocks));
    }

    public String addBlock(String message, String owner, String aadhar) throws Exception {
        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        String prevHash = cryptoUtils.extractSignature(blocks.get(blocks.size() - 1));

        int i = -1;
        String block = "";
        String expectedDifficulty = "";
        for (int j = 0; j < difficulty; j++) {
            expectedDifficulty = expectedDifficulty + difficultyCharacter;
        }

        while (i < 1000000) {
            i = i + 1;
            block = cryptoUtils.createBlock(keysDev.getKey(), keysDev.getValue(), message + "nonce:" + i, owner, aadhar, prevHash);
            String sign = block.split(":")[0].substring(1).split("\"")[0];
            int siglen = sign.length();

            if (sign.substring(siglen - difficulty, siglen).equals(expectedDifficulty)) {
                break;
            }
        }

        List<String> newBlocks = cryptoUtils.getBlocks(blockFile);
        String newPrevHash = cryptoUtils.extractSignature(newBlocks.get(newBlocks.size() - 1));


        if (!newPrevHash.equals(prevHash)) {
            System.out.println("here");
            return addBlock(message, owner, aadhar);
        } else {
            cryptoUtils.appendBlocks(blockFile, block);
            return "Blockdata: " + message + "nonce:" + i + owner + aadhar + prevHash + " \nPublicKey: " + keysDev.getKey();
        }

    }

    public String showAuthorized() throws Exception {
        return cryptoUtils.surroundWithBraces(cryptoUtils.addComma(
                cryptoUtils.superKeyValuePair("Dev", cryptoUtils.keyValuePair("publicKey", keysMiner.getKey())),
                cryptoUtils.superKeyValuePair("Miner1", cryptoUtils.keyValuePair("publicKey", keysDev.getKey())),
                cryptoUtils.superKeyValuePair("Rajiv", cryptoUtils.keyValuePair("publicKey", keysRajiv.getKey()))
        ));
    }


    public String verifyAllSignatures() throws Exception {
        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        String genesis = blocks.get(0);
        boolean isValid = cryptoUtils.verify(cryptoUtils.extract(messageKey, genesis) + cryptoUtils.extract(ownerKey, genesis) + cryptoUtils.extract(aadharKey, genesis),
                keysMiner.getKey(), cryptoUtils.extractSignature(genesis));

        for (int i = 1; i < blocks.size(); i++) {
            String block = blocks.get(i);
            String previousHash = cryptoUtils.extractSignature(blocks.get(i - 1));
            isValid = isValid & (cryptoUtils.isValid(block, previousHash, keysMiner.getKey()) |
                    cryptoUtils.isValid(block, previousHash, keysDev.getKey()) |
                    cryptoUtils.isValid(block, previousHash, keysRajiv.getKey())
            );
        }

        return String.valueOf(isValid);
    }

    public boolean verify(String data, String pubkey, String sign) throws Exception {
        return cryptoUtils.verify(data, pubkey, sign);
    }

    public String showGenesis(String message, String owner, String aadhar) throws Exception {
        return cryptoUtils.createGenesisBlock(keysDev.getKey(), keysDev.getValue(), message, owner, aadhar);
    }

    public String generateKeyString() throws Exception {
        Pair<PublicKey, PrivateKey> key = cryptoUtils.generateKeys();
        return "Public Key: " + cryptoUtils.encodeKeyToString(key.getKey()) +
                "\nPrivate Key: " + cryptoUtils.encodeKeyToString(key.getValue());
    }

    public String getStats() {
        return "Difficulty: " + difficulty +
                "\nHashRate: 1GH/Sec" +
                "\nCost per transaction(block): $0.00038"+
                "\nBlock Time: 5 seconds on average";
    }
}

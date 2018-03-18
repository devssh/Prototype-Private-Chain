package app.service;

import app.model.Keyz;
import app.utils.CryptoUtils;

import java.util.List;

import static app.model.StringVar.*;

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

    KeyzManager keyzManager;
    CryptoUtils cryptoUtils;

    public CryptoService(String keyFile, String blockFile) throws Exception {
        this.keyzManager = new KeyzManager(keyFile);
        this.cryptoUtils = new CryptoUtils(keyzManager);
        this.blockFile = blockFile;
        this.keysMiner = keyzManager.getKey("Miner");
        this.keysDev = keyzManager.getKey("Dev");
        this.keysRajiv = keyzManager.getKey("Rajiv");
    }


    public String getBlockchain() throws Exception {
        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        return surroundWithBraces(joinWithComma(blocks));
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
            block = cryptoUtils.createBlock(keysDev.publicKey, keysDev.privateKey, message + "nonce:" + i, owner, aadhar, prevHash);
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
            return "Blockdata: " + message + "nonce:" + i + owner + aadhar + prevHash + " \nPublicKey: " + keysDev.publicKey;
        }

    }

    public String showAuthorized() throws Exception {
        return surroundWithBraces(joinWithComma(
                superKeyValuePair(keysMiner.owner, keyValuePair("publicKey", keysMiner.publicKey)),
                superKeyValuePair(keysDev.owner, keyValuePair("publicKey", keysDev.publicKey)),
                superKeyValuePair(keysRajiv.owner, keyValuePair("publicKey", keysRajiv.publicKey))
        ));
    }


    public String verifyAllSignatures() throws Exception {
        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        String genesis = blocks.get(0);
        boolean isValid = cryptoUtils.verify(cryptoUtils.extract(messageKey, genesis) + cryptoUtils.extract(ownerKey, genesis) + cryptoUtils.extract(aadharKey, genesis),
                keysMiner.publicKey, cryptoUtils.extractSignature(genesis));

        for (int i = 1; i < blocks.size(); i++) {
            String block = blocks.get(i);
            String previousHash = cryptoUtils.extractSignature(blocks.get(i - 1));
            isValid = isValid & (cryptoUtils.isValid(block, previousHash, keysMiner.publicKey) |
                    cryptoUtils.isValid(block, previousHash, keysDev.publicKey) |
                    cryptoUtils.isValid(block, previousHash, keysRajiv.publicKey)
            );
        }

        return String.valueOf(isValid);
    }

    public boolean verify(String data, String pubkey, String sign) throws Exception {
        return cryptoUtils.verify(data, pubkey, sign);
    }

    public String showGenesis(String message, String owner, String aadhar) throws Exception {
        return cryptoUtils.createGenesisBlock(keysDev.publicKey, keysDev.privateKey, message, owner, aadhar);
    }

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

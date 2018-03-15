package app.service;

import app.utils.CryptoUtils;
import javafx.util.Pair;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CryptoService {

    CryptoUtils cryptoUtils = new CryptoUtils();

    String keyFile = "keys.dat";
    String blockFile = "blocks.dat";
    List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
    Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
            keysDev = new Pair<>(keys.get(2), keys.get(3)),
            keysRajiv = new Pair<>(keys.get(4), keys.get(5));




    public CryptoService(String keyFile, String blockFile) throws Exception {
        this.keyFile = keyFile;
        this.blockFile = blockFile;
    }


    public String getBlockchain() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get(keyFile));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        return cryptoUtils.surroundWithBraces(cryptoUtils.addComma(blocks));
    }

    public String addBlock(String message, String owner, String aadhar) throws Exception {


        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        String prevHash = cryptoUtils.extractSignature(blocks.get(blocks.size() - 1));
        cryptoUtils.appendBlocks(blockFile, cryptoUtils.createBlock(keysDev.getKey(), keysDev.getValue(), message, owner, aadhar, prevHash));

        return "Blockdata: " + message + owner + aadhar + prevHash + " \nPublicKey: " + keysDev.getKey();
    }

    public String showAuthorized() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));


        return cryptoUtils.surroundWithBraces(cryptoUtils.addComma(
                cryptoUtils.superKeyValuePair("Dev", cryptoUtils.keyValuePair("publicKey", keysAdmin.getKey())),
                cryptoUtils.superKeyValuePair("Devashish", cryptoUtils.keyValuePair("publicKey", keysDev.getKey())),
                cryptoUtils.superKeyValuePair("Rajiv", cryptoUtils.keyValuePair("publicKey", keysRajiv.getKey()))
        ));
    }


    public String verifyAllSignatures() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = cryptoUtils.getBlocks(blockFile);

        String genesis = blocks.get(0);
        boolean isValid = cryptoUtils.verify(cryptoUtils.extract("message", genesis) + cryptoUtils.extract("owner", genesis) + cryptoUtils.extract("aadhar", genesis),
                keysAdmin.getKey(), cryptoUtils.extractSignature(genesis));

        for (int i = 1; i < blocks.size(); i++) {
            String block = blocks.get(i);
            String previousHash = cryptoUtils.extractSignature(blocks.get(i - 1));
            isValid = isValid & (cryptoUtils.isValid(block, previousHash, keysAdmin.getKey()) |
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
}

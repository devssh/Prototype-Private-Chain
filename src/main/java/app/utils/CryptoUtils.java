package app.utils;

import app.model.Keyz;
import app.service.KeyzManager;
import app.service.SignService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static app.model.StringVar.*;

public class CryptoUtils {
    String messageKey="message";
    String ownerKey="owner";
    String aadharKey="aadhar";

    KeyzManager keyzManager;
    SignService signService;

    public CryptoUtils(KeyzManager keyzManager) throws Exception {
        this.keyzManager = keyzManager;
        this.signService = new SignService(keyzManager);
    }

    public boolean isValid(String block, String previousHash, String publicKey) throws Exception {
        return SignService.verify(extract(messageKey, block)
                        + extract(ownerKey, block)
                        + extract(aadharKey, block)
                        + previousHash,
                publicKey, extractSignature(block));
    }

    public List<String> getBlocks(String blockFile) throws Exception {
        return Files.readAllLines(Paths.get(blockFile));
    }


    public List<String> appendBlocks(String blockFile, String... blocks) throws Exception {
        Writer output = new BufferedWriter(new FileWriter(blockFile, true));
        for (String block : blocks) {
            output.append(block + "\n");
            output.flush();
        }

        output.close();

        return getBlocks(blockFile);
    }


    public String initBlockchain(String keyFile, String message, String owner, String aadhar) throws Exception {
        Keyz keysMiner = keyzManager.getKey("Miner"),
                keysDev = keyzManager.getKey("Dev"),
                keysRajiv = keyzManager.getKey("Rajiv");

        String genesis = createGenesisBlock(keysMiner, message, owner, aadhar);

        return surroundWithBraces(joinWithComma(genesis));
    }


    public String createGenesisBlock(Keyz key, String message, String owner, String aadhar) throws Exception {
        return createBlock(key, message, owner, aadhar, "");
    }

    public String createBlock(Keyz key, String message, String owner, String aadhar, String previousHash) throws Exception {
        String sign = signService.sign(key.privateKeyz, message + owner + aadhar + previousHash);

        //System.out.println(verify(message + owner + aadhar + previousHash, publicKey, sign));

        return blockFormat(sign, message, owner, aadhar);
    }

    private String blockFormat(String sign, String message, String owner, String aadhar) {
        return "\"" + sign + "\":" + surroundWithBraces(joinWithComma(keyValuePair(messageKey, message), keyValuePair(ownerKey, owner),
                keyValuePair(aadharKey, aadhar)));
    }

    public String extractSignature(String block) {
        //Pattern signature = Pattern.compile(".*\"sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return block.split("\":")[0].substring(1);
    }

    public String extract(String field, String block) {
        return block.split("\"" + field + "\":\"")[1].split("\"")[0];
    }



}

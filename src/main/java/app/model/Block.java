package app.model;

import java.util.Arrays;

import static app.model.StringVar.*;
import static app.service.SignService.Sign;

public class Block extends Verifiable {
    public final String sign;
    public final String publicKey;
    public final String data;
    public final VariableManager varMan;
    public String[] txnids;
    public static final int DIFFICULTY = 3;
    public static final String DIFFICULTY_CHARACTER = "0";

    Block(String sign, String nonce, String publicKey, String prevHash, Txn... txns) throws Exception {
        this.varMan = new VariableManager(
                "nonce", nonce,
                "publicKey", publicKey,
                "prevHash", prevHash,
                "data", Txn.Serialize(txns)
        );
        this.sign = sign;
        this.publicKey = publicKey;
        this.data = JoinWith("", nonce, publicKey, prevHash, Txn.SerializeForSign(txns));
        this.txnids = Arrays.stream(txns).map(txn -> txn.varMan.get("txnid")).toArray(String[]::new);
    }

    public Block(Keyz key, String prevHash, Txn... txns) throws Exception {
        //TODO: Extract the mining logic
        int i = -1;
        String block = "";
        String expectedDifficulty = "";
        for (int j = 0; j < DIFFICULTY; j++) {
            expectedDifficulty = expectedDifficulty + DIFFICULTY_CHARACTER;
        }

        String sign = "", data = "";

        while (i < 1000000) {
            i = i + 1;
            data = JoinWith("", String.valueOf(i), key.publicKey, prevHash, Txn.SerializeForSign(txns));
            sign = Sign(key.privateKey, data);
            int siglen = sign.length();

            if (sign.substring(siglen - DIFFICULTY, siglen).equals(expectedDifficulty)) {
                break;
            }
        }
        this.varMan = new VariableManager(
                "nonce", String.valueOf(i),
                "publicKey", key.publicKey,
                "prevHash", prevHash,
                "data", Txn.Serialize(txns)
        );
        this.sign = sign;
        this.publicKey = key.publicKey;
        this.data = data;
        //TODO: extract orphans and concurrent issue handling
    }

    public static Block Deserialize(String block) throws Exception {
        String sign = extractStringKeyFromJson("blockSign", block);
        String nonce = extractStringKeyFromJson("nonce", block);
        String publicKey = extractStringKeyFromJson("publicKey", block);
        String prevHash = extractStringKeyFromJson("prevHash", block);
        Txn[] txns = Txn.Deserialize(extractArrayKeyFromJson("data", block));
        return new Block(sign, nonce, publicKey, prevHash, txns);
    }

    @Override
    public String toString() {
        return "{" + JoinWith(",", KeyValuePair(new StringVar("blockSign", sign)), varMan.jsonString().substring(1));
    }

}

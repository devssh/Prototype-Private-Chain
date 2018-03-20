package app.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static app.model.StringVar.*;
import static app.service.SignService.Sign;

public class Block {
    final VariableManager varMan;
    public final String sign;
    public final Chunk chunk;
    public Object[] txnids;

    Block(String sign, String nonce, String publicKey, String prevHash, Txn... txns) throws Exception {
        this.varMan = new VariableManager(
                "nonce", nonce,
                "publicKey", publicKey,
                "prevHash", prevHash,
                "data", Txn.Serialize(txns)
        );
        this.sign = sign;
        this.chunk = new Chunk(sign, publicKey, JoinWith("", nonce, publicKey, prevHash, Txn.SerializeForSign(txns)));
        this.txnids = Arrays.stream(txns).map(txn -> txn.varMan.get("txnid")).toArray();
    }

    public Block(Keyz key, String prevHash, Txn... txns) throws Exception {
        //TODO: Extract the mining logic
        int difficulty = 3;
        String difficultyCharacter = "0";

        int i = -1;
        String block = "";
        String expectedDifficulty = "";
        for (int j = 0; j < difficulty; j++) {
            expectedDifficulty = expectedDifficulty + difficultyCharacter;
        }

        String sign = "", data = "";

        while (i < 1000000) {
            i = i + 1;
            data = JoinWith("", String.valueOf(i), key.publicKey, prevHash, Txn.SerializeForSign(txns));
            sign = Sign(key.privateKey, data);
            int siglen = sign.length();

            if (sign.substring(siglen - difficulty, siglen).equals(expectedDifficulty)) {
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
        this.chunk = new Chunk(sign, key.publicKey, data);
        //TODO: extract orphans and concurrent issue
    }

    public static Block Deserialize(String block) throws Exception {
        String sign = StripQuotes(block.split(":")[0]);
        String nonce = extractStringKeyFromJson("nonce", block);
        String publicKey = extractStringKeyFromJson("publicKey", block);
        String prevHash = extractStringKeyFromJson("prevHash", block);
        Txn[] txns = Txn.Deserialize(extractArrayKeyFromJson("data", block));
        return new Block(sign, nonce, publicKey, prevHash, txns);
    }

    @Override
    public String toString() {
        return SurroundWithQuotes(sign) + ":" + varMan.jsonString();
    }

    public boolean verify() throws Exception {
        return chunk.verify();
    }
}

package app.model;

import static app.model.StringVar.*;
import static app.service.SignService.Sign;
import static app.utils.MiscUtils.GetDateTimeNow;

public class Block extends Verifiable {
    public static final int DIFFICULTY = 3;
    public static final String DIFFICULTY_CHARACTER = "0";

    public static final String BLOCK_SIGN = "blockSign";
    public static final String NONCE = "nonce";
    public static final String PREV_HASH = "prevHash";
    public static final String BLOCK_CREATED_AT = "blockCreatedAt";

    public final String sign;
    public final String publicKey;
    public final String data;
    public final VariableManager varMan;
    public Txn[] txns;

    Block(String sign, String nonce, String publicKey, String prevHash, String createdAt, Txn... txns) {
        this.varMan = new VariableManager(
                BLOCK_SIGN, sign,
                NONCE, nonce,
                PUBLIC_KEY, publicKey,
                PREV_HASH, prevHash,
                BLOCK_CREATED_AT, createdAt,
                DATA, Txn.Serialize(txns)
        );
        this.sign = sign;
        this.publicKey = publicKey;
        this.data = JoinWith("", nonce, publicKey, prevHash, createdAt, Txn.SerializeForSign(txns));
        this.txns = txns;
    }

    public Block(Keyz key, String prevHash, Txn... txns) throws Exception {
        //TODO: Extract the mining logic
        int i = -1;
        String expectedDifficulty = "";
        for (int j = 0; j < DIFFICULTY; j++) {
            expectedDifficulty = expectedDifficulty + DIFFICULTY_CHARACTER;
        }

        String sign = "", data = "", createdAt = GetDateTimeNow();

        while (i < Integer.MAX_VALUE) {
            i = i + 1;
            data = JoinWith("", String.valueOf(i), key.publicKey, prevHash, createdAt, Txn.SerializeForSign(txns));
            sign = Sign(key.privateKey, data);
            int siglen = sign.length();

            if (sign.substring(siglen - DIFFICULTY, siglen).equals(expectedDifficulty)) {
                break;
            }
        }
        this.varMan = new VariableManager(
                BLOCK_SIGN, sign,
                NONCE, String.valueOf(i),
                PUBLIC_KEY, key.publicKey,
                PREV_HASH, prevHash,
                BLOCK_CREATED_AT, createdAt,
                DATA, Txn.Serialize(txns)
        );
        this.sign = sign;
        this.publicKey = key.publicKey;
        this.data = data;
        //TODO: extract orphans and concurrent issue handling
    }

    public static Block Deserialize(String block) {
        String sign = ExtractStringKeyFromJson(BLOCK_SIGN, block);
        String nonce = ExtractStringKeyFromJson(NONCE, block);
        String publicKey = ExtractStringKeyFromJson(PUBLIC_KEY, block);
        String prevHash = ExtractStringKeyFromJson(PREV_HASH, block);
        String createdAt = ExtractStringKeyFromJson(BLOCK_CREATED_AT, block);
        Txn[] txns = Txn.Deserialize(extractArrayKeyFromJson(DATA, block));
        return new Block(sign, nonce, publicKey, prevHash, createdAt, txns);
    }

    @Override
    public String toString() {
        return varMan.jsonString();
    }

}

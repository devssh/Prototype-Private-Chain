package app.model;

import java.util.Map;

import static app.model.StringVar.*;
import static app.service.MiningService.Mine;
import static app.service.SignService.Sign;
import static app.utils.MiscUtils.GetDateTimeNow;

public class Block extends Verifiable {
    public static final String BLOCK_SIGN = "blockSign";
    public static final String NONCE = "nonce";
    public static final String DEPTH = "depth";
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
        Map<String, String> miningData = Mine(key, prevHash, Txn.SerializeForSign(txns));

        String sign = miningData.get(BLOCK_SIGN);

        this.varMan = new VariableManager(
                BLOCK_SIGN, sign,
                NONCE, miningData.get(NONCE),
                PUBLIC_KEY, key.publicKey,
                PREV_HASH, prevHash,
                BLOCK_CREATED_AT, miningData.get(BLOCK_CREATED_AT),
                DATA, Txn.Serialize(txns)
        );
        this.sign = sign;
        this.publicKey = key.publicKey;
        this.data = miningData.get(DATA);
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

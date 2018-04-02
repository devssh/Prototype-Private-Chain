package app.service;

import app.model.Keyz;
import app.model.Txn;

import java.util.HashMap;
import java.util.Map;

import static app.model.Block.BLOCK_CREATED_AT;
import static app.model.Block.BLOCK_SIGN;
import static app.model.Block.NONCE;
import static app.model.StringVar.JoinWith;
import static app.model.Verifiable.DATA;
import static app.service.SignService.Sign;
import static app.utils.MiscUtils.GetDateTimeNow;

public class MiningService {
    public static final int DIFFICULTY = 3;
    public static final String DIFFICULTY_CHARACTER = "0";

    public static Map<String, String> Mine(Keyz key, String prevHash, String serializedTxns) throws Exception {
        int i = -1;
        String expectedDifficulty = "";
        for (int j = 0; j < DIFFICULTY; j++) {
            expectedDifficulty = expectedDifficulty + DIFFICULTY_CHARACTER;
        }

        String sign = "", createdAt = GetDateTimeNow(), data = JoinWith("", String.valueOf(i), key.publicKey, prevHash, createdAt, serializedTxns);

        while (i < Integer.MAX_VALUE) {
            i = i + 1;

            sign = Sign(key.privateKey, data);
            int siglen = sign.length();

            if (sign.substring(siglen - DIFFICULTY, siglen).equals(expectedDifficulty)) {
                break;
            }
        }

        final String signature = sign, nonce = String.valueOf(i);
        return new HashMap<String, String>() {{
            put(BLOCK_SIGN, signature);
            put(BLOCK_CREATED_AT, createdAt);
            put(NONCE, nonce);
            put(DATA, data);
        }};
    }
}

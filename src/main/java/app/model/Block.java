package app.model;

import app.service.KeyzManager;

import java.util.Arrays;
import java.util.stream.Collectors;

import static app.model.StringVar.*;

public class Block {
    final VariableManager varMan;
    final KeyzManager keyzManager;
    final String sign;
    final Chunk chunk;

    public Block(KeyzManager keyzManager, String nonce, String sign, String signedBy, String prevHash, String createdAt, Txn... txns) throws Exception {
        this.keyzManager = keyzManager;
        Keyz key = keyzManager.getKey(signedBy);

        this.varMan = new VariableManager(
                "nonce", nonce,
                "signedBy", signedBy,
                "publicKey", key.publicKey,
                "prevHash", prevHash,
                "createdAt", createdAt,
                "data", surroundWithBraces(joinWithComma(Arrays.stream(txns).map(Txn::toString).collect(Collectors.toList())), "square")
        );
        this.sign = sign;
        this.chunk = new Chunk(sign, key.publicKey, joinWith("", Arrays.stream(txns).map(txn -> txn.sign).collect(Collectors.toList())));
    }

    @Override
    public String toString() {
        return sign + ":" + varMan.jsonString();
    }

    public boolean verify() throws Exception {
        return chunk.verify();
    }
}

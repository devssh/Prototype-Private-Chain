package app.model;

import app.service.KeyzManager;

public class Block {
    final VariableManager variableManager;
    final KeyzManager keyzManager;

    public Block(KeyzManager keyzManager, String sign, Txn txn, String signedBy, String prevHash) throws Exception {
        this.keyzManager = keyzManager;
        Keyz key = keyzManager.getKey(signedBy);
        this.variableManager = new VariableManager(
            "sign", sign,
                "signedBy", signedBy,
                "publicKey", key.publicKey,
                "prevHash", prevHash,
                "data", txn.toString()
        );
    }


}

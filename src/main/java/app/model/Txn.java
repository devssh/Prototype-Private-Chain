package app.model;

public class Txn {
    final VariableManager varMan;
    final String sign;

    public Txn(String sign, String publicKey, String txnid, String email, String location, String createdAt) {
        this.varMan = new VariableManager(
                "sign", sign,
                "publicKey", publicKey,
                "txnid", txnid,
                "email", email,
                "location", location,
                "createdAt", createdAt
        );
        this.sign = sign;
    }

    @Override
    public String toString() {
        return varMan.jsonString();
    }
}

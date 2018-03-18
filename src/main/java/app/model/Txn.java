package app.model;

public class Txn {
    final VariableManager varMan;

    public Txn(String txnid, String email, String location, String createdAt) {
        this.varMan=new VariableManager(
                "txnid", txnid,
                "email", email,
                "location", location,
                "createdAt", createdAt
        );
    }

    @Override
    public String toString() {
        return varMan.jsonString();
    }
}

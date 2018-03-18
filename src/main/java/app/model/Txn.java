package app.model;

import java.util.Arrays;
import java.util.stream.Collectors;

import static app.model.StringVar.*;

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

    public static String Serialize(Txn[] txns) {
        return SurroundWithBraces(JoinWithComma(Arrays.stream(txns).map(Txn::toString).collect(Collectors.toList())), "square");
    }

    public static Txn[] Deserialize(String txns) {
        return Arrays.stream(StripSquareBraces(txns).split(",")).map(txnString -> {
            String txn = StripBraces(txnString);
            String sign = extractStringKeyFromJson("sign", txn);
            String publicKey = extractStringKeyFromJson("publicKey", txn);
            String txnid = extractStringKeyFromJson("txnid", txn);
            String email = extractStringKeyFromJson("email", txn);
            String location = extractStringKeyFromJson("location", txn);
            String createdAt = extractStringKeyFromJson("createdAt", txn);

            return new Txn(sign, publicKey, txnid, email, location, createdAt);
        }).toArray(Txn[]::new);
    }

    public static String SerializeForSign(Txn[] txns) {
        return JoinWith("", Arrays.stream(txns).map(txn -> txn.sign).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return varMan.jsonString();
    }
}

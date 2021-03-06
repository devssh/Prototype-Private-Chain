package app.model;

import java.util.Arrays;
import java.util.stream.Collectors;

import static app.model.HashString.hashString;
import static app.model.StringVar.*;

public class Txn extends Verifiable {
    public static final String REDEEM = "redeem";
    public static final String CREATE = "create";

    public static final String SIGN = "sign";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String TXNID = "txnid";
    public static final String LOCATION = "location";
    public static final String CREATED_AT = "createdAt";
    public static final String TYPE = "type";
    public static final String EMAIL = "email";

    public final VariableManager varMan;
    public final String sign;
    public final String publicKey;
    public final String data;

    public Txn(String sign, String publicKey, String txnid, String email, String location, String createdAt, String type) {
        this.sign = sign;
        this.publicKey = publicKey;
        this.data = txnid + email + location + createdAt + type;
        if (type.equals(REDEEM)) {
            this.varMan = new VariableManager(
                    SIGN, sign,
                    PUBLIC_KEY, publicKey,
                    TXNID, txnid,
                    LOCATION, location,
                    CREATED_AT, createdAt,
                    TYPE, type
            );
        } else {
            this.varMan = new VariableManager(
                    SIGN, sign,
                    PUBLIC_KEY, publicKey,
                    TXNID, txnid,
                    EMAIL, email,
                    CREATED_AT, createdAt,
                    TYPE, type
            );
        }
    }

    public static String Serialize(Txn[] txns) {
        return SurroundWithBraces(JoinWithComma(Arrays.stream(txns).map(Txn::toString).collect(Collectors.toList())), "square");
    }

    public static Txn[] Deserialize(String txns) {
        String[] txnStrings = StripSquareBraces(txns).split("\\{");
        return Arrays.stream(Arrays.copyOfRange(txnStrings, 1, txnStrings.length)).map(txnString -> {
            String txn = txnString.split("}", 2)[0];
            String sign = ExtractStringKeyFromJson("sign", txn);
            String publicKey = ExtractStringKeyFromJson("publicKey", txn);
            String txnid = ExtractStringKeyFromJson("txnid", txn);
            String type = ExtractStringKeyFromJson("type", txn);
            String createdAt = ExtractStringKeyFromJson("createdAt", txn);
            if (type.equals(REDEEM)) {
                String location = ExtractStringKeyFromJson("location", txn);
                return new Txn(sign, publicKey, txnid, "", location, createdAt, type);
            } else {
                String email = ExtractStringKeyFromJson("email", txn);
                return new Txn(sign, publicKey, txnid, email, "", createdAt, type);
            }


        }).toArray(Txn[]::new);
    }

    public static String SerializeForSign(Txn[] txns) {
        return JoinWith("", Arrays.stream(txns).map(txn -> txn.sign).collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return varMan.jsonString();
    }

    public static boolean areEqual(Txn one, Txn other) {
        return one.varMan.get(TXNID).equals(other.varMan.get(TXNID)) && one.varMan.get(TYPE).equals(other.varMan.get(TYPE));
    }

    public static HashString EqualityString(String txnid, String type) {
        return hashString(txnid + type);
    }

    public HashString equalityString() {
        return hashString(varMan.get(TXNID) + varMan.get(TYPE));
    }
}

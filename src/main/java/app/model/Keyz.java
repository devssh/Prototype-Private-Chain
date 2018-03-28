package app.model;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static app.model.StringVar.ExtractStringKeyFromJson;

public class Keyz {
    public static final String NAME = "name";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";
    public final String owner;
    public final String publicKey;
    public final String privateKey;
    public final PublicKey publicKeyz;
    public final PrivateKey privateKeyz;
    public VariableManager varMan;

    public Keyz(String owner, String publicKey, String privateKey) throws Exception {
        this.owner = owner;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.publicKeyz = decodePublicKeyFromString(publicKey);
        this.privateKeyz = decodePrivateKeyFromString(privateKey);
        this.varMan = new VariableManager(
                NAME, owner,
                PUBLIC_KEY, publicKey,
                PRIVATE_KEY, privateKey
        );
    }

    public Keyz(String owner, PublicKey publicKeyz, PrivateKey privateKeyz) {
        this.owner = owner;
        this.publicKeyz = publicKeyz;
        this.privateKeyz = privateKeyz;
        this.publicKey = encodeKeyToString(publicKeyz);
        this.privateKey = encodeKeyToString(privateKeyz);
    }


    public static String encodeKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static PublicKey decodePublicKeyFromString(String key) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePublic(new X509EncodedKeySpec(decodeKeyFromString(key)));
    }

    public static PrivateKey decodePrivateKeyFromString(String key) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePrivate(new PKCS8EncodedKeySpec(decodeKeyFromString(key)));
    }

    private static byte[] decodeKeyFromString(String key) throws UnsupportedEncodingException {
        return Base64.getDecoder().decode(key.getBytes("UTF-8"));
    }

    public static Keyz generateKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        //TODO: Use TRNG somehow
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();

        return new Keyz("random", pub, priv);
    }

    public static Keyz Deserialize(String key) {
        Keyz keyz = null;
        try {
            keyz = new Keyz(ExtractStringKeyFromJson(NAME, key),
                    ExtractStringKeyFromJson(PUBLIC_KEY, key),
                    ExtractStringKeyFromJson(PRIVATE_KEY, key));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyz;
    }

    @Override
    public String toString() {
        // aka serialize
        return varMan.jsonString();
    }
}

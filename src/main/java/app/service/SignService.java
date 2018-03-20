package app.service;

import app.model.Keyz;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import static app.service.KeyzManager.GetKey;

public class SignService {
    public static final String signatureAlgo = "SHA1withECDSA";
    public static final String ENCODING = "UTF-8";
    public static final int RADIX = 16;

    public static KeyzManager KeyzManager;

    static {
        try {
            KeyzManager = new KeyzManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SignService() throws Exception {
    }

    public static String Sign(PrivateKey privateKey, String message) throws Exception {
        Signature dsa = Signature.getInstance(signatureAlgo);
        dsa.initSign(privateKey);
        dsa.update(message.getBytes(ENCODING));
        return new BigInteger(1, dsa.sign()).toString(RADIX);
    }

    public static String SignWith(String signedBy, String message) throws Exception {
        return Sign(GetKey(signedBy).privateKeyz, message);
    }

    public static String Sign(String privKey, String message) throws Exception {
        return Sign(Keyz.decodePrivateKeyFromString(privKey), message);
    }

    public static boolean Verify(String blockMessage, PublicKey publicKey, String sign) throws Exception {
        Signature ecdsa = Signature.getInstance(signatureAlgo);
        ecdsa.initVerify(publicKey);
        ecdsa.update(blockMessage.getBytes(ENCODING));
        return ecdsa.verify(DatatypeConverter.parseHexBinary(sign));
    }

    public static boolean Verify(String blockMessage, String pubKey, String sign) throws Exception {
        return Verify(blockMessage, Keyz.decodePublicKeyFromString(pubKey), sign);
    }

    public static boolean VerifyWith(String blockMessage, String signedBy, String sign) throws Exception {
        return Verify(blockMessage, GetKey(signedBy).publicKeyz, sign);
    }

}

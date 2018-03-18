package app.service;

import app.model.Keyz;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class SignService {
    public static final String signatureAlgo = "SHA1withECDSA";
    public static final String ENCODING = "UTF-8";
    public static final int RADIX = 16;

    final KeyzManager keyzManager;

    public  SignService(KeyzManager keyzManager) {
        this.keyzManager = keyzManager;
    }

    public String sign(PrivateKey privateKey, String message) throws Exception {
        Signature dsa = Signature.getInstance(signatureAlgo);
        dsa.initSign(privateKey);
        dsa.update(message.getBytes(ENCODING));
        return new BigInteger(1, dsa.sign()).toString(RADIX);
    }

    public String signWith(String signedBy, String message) throws Exception {
        return sign(keyzManager.getKey(signedBy).privateKeyz, message);
    }

    public String sign(String privKey, String message) throws Exception {
        return sign(Keyz.decodePrivateKeyFromString(privKey), message);
    }

    public static boolean verify(String blockMessage, PublicKey publicKey, String sign) throws Exception {
        Signature ecdsa = Signature.getInstance(signatureAlgo);
        ecdsa.initVerify(publicKey);
        ecdsa.update(blockMessage.getBytes(ENCODING));
        return ecdsa.verify(DatatypeConverter.parseHexBinary(sign));
    }

    public static boolean verify(String blockMessage, String pubKey, String sign) throws Exception {
        return verify(blockMessage, Keyz.decodePublicKeyFromString(pubKey), sign);
    }

    public boolean verifyWith(String blockMessage, String signedBy, String sign) throws Exception {
        return verify(blockMessage, keyzManager.getKey(signedBy).publicKeyz, sign);
    }

}

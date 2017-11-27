package hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@RestController
public class SignController {

    @GetMapping(value = "/sign", produces = "application/json")
    public Object index() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        /*
         * Create a Signature object and initialize it with the private key
         */

        Signature dsa = Signature.getInstance("SHA1withECDSA");
        Signature dsa2 = Signature.getInstance("SHA1withECDSA");

        dsa.initSign(priv);

        String str = "This is string to sign";
        byte[] strByte = str.getBytes("UTF-8");
        dsa.update(strByte);

        /*
         * Now that all the data to be signed has been read in, generate a
         * signature for it
         */

        byte[] realSig = dsa.sign();

        String signature = new BigInteger(1, realSig).toString(16);


        System.out.println("Signature: " + signature);
        dsa2.initVerify(pub);
        dsa2.update(strByte);
        //return dsa2.verify(realSig);
        return dsa2.verify(DatatypeConverter.parseHexBinary(signature));
    }

    private String sign(String privKey, String message) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        PrivateKey privateKey = fact.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKey.getBytes())));

        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initSign(privateKey);
        dsa.update(message.getBytes("UTF-8"));
        return new BigInteger(1, dsa.sign()).toString(16);
    }

    private boolean verify(String message, String pubKey, String sign) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        PublicKey publicKey = fact.generatePublic(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pubKey.getBytes())));

        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initVerify(publicKey);
        dsa.update(message.getBytes("UTF-8"));
        return dsa.verify(sign.getBytes());
    }

}
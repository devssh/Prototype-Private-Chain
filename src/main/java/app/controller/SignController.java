package app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.*;

@RestController
public class SignController {

    @GetMapping(value = "/signTest", produces = "application/json")
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

        String str = "This is string to Sign";
        byte[] strByte = str.getBytes("UTF-8");
        dsa.update(strByte);

        /*
         * Now that all the data to be signed has been read in, generate a
         * signature for it
         */

        byte[] realSig = dsa.sign();

        String signature = new BigInteger(1, realSig).toString(16);


        dsa2.initVerify(pub);
        dsa2.update(strByte);
        //return dsa2.Verify(realSig);
        return dsa2.verify(DatatypeConverter.parseHexBinary(signature));
    }

}
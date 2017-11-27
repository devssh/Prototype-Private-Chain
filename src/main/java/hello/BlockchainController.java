package hello;

import javafx.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@RestController
public class BlockchainController {

    @GetMapping(value = "/land", produces = "application/json")
    public String landBlockchain() throws Exception {
        return blockchain();
    }

    public String blockchain() throws Exception{
        /*
        Pair<PublicKey, PrivateKey> keysAdmin = generateKeys();
        Pair<PublicKey, PrivateKey> keysDev = generateKeys();
        Pair<PublicKey, PrivateKey> keysRajiv = generateKeys();

        List<String> keys = new ArrayList<String>(){{
           add(encodeKeyToString(keysAdmin.getKey()));
           add(encodeKeyToString(keysAdmin.getValue()));
           add(encodeKeyToString(keysDev.getKey()));
           add(encodeKeyToString(keysDev.getValue()));
           add(encodeKeyToString(keysRajiv.getKey()));
           add(encodeKeyToString(keysRajiv.getValue()));
        }};
        Files.write(Paths.get("keys.dat"), keys);
        */
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
        keysDev = new Pair<>(keys.get(2), keys.get(3)),
        keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        String genesis = createGenesisBlock(keysAdmin.getKey(), keysAdmin.getValue());
        String firstBlock = createBlock(keysDev.getKey(), keysDev.getValue(), extractPreviousHash(genesis));

        return "{" + addComma(genesis, firstBlock) + "}";
    }


    public String createGenesisBlock(String publicKey, String privateKey) throws Exception {
        String message = "Genesis Block";
        String owner = "Rajiv";
        String aadhar = "12582";

        String sign = sign(privateKey, message+owner+aadhar);

        System.out.println(verify(message + owner + aadhar, publicKey, sign));

        return blockFormat(sign, message, owner, aadhar);
    }

    public String createBlock(String publicKey, String privateKey, String previousHash) throws Exception {
        String message = "Plot 1, Shastri Nagar, Adyar, Chennai 600023";
        String owner = "Devashish";
        String aadhar = "19452";
        String sign = sign(privateKey, message + owner + aadhar + previousHash);
        System.out.println(verify(message + owner + aadhar + previousHash, publicKey, sign));
        return blockFormat(sign, message, owner, aadhar);
    }

    public String blockFormat(String sign, String message, String owner, String aadhar) {
        return "\"" + sign + "\":" + surroundWithBraces(addComma(keyValuePair("message", message), keyValuePair("owner", owner),
                keyValuePair("aadhar", aadhar)));
    }

    private String extractPreviousHash(String block) {
        //Pattern signature = Pattern.compile(".*\"sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return block.split("\":")[0].substring(1);
    }

    private String surroundWithBraces(String value) {
        return "{" + value + "}";
    }

    private String addComma(String... values) {
        return String.join(",", values);
    }

    private String keyValuePair(String key, String value) {
        return keyValuePair(key, value, true);
    }

    private String keyValuePair(String key, String value, boolean noBraces) {
        if (noBraces) {
            return "\"" + key + "\":\"" + value + "\"";
        }
        return surroundWithBraces(key + ":" + value);
    }

    private Pair<PublicKey, PrivateKey> generateKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        //TODO: Use TRNG somehow
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();

        return new Pair<>(pub, priv);
    }

    private String sign(PrivateKey privateKey, String message) throws Exception {
        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initSign(privateKey);
        dsa.update(message.getBytes("UTF-8"));
        return new BigInteger(1, dsa.sign()).toString(16);
    }

    private String sign(String privKey, String message) throws Exception {
        return sign(decodePrivateKeyFromString(privKey), message);
    }

    private boolean verify(String blockMessage, PublicKey publicKey, String sign) throws Exception {
        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initVerify(publicKey);
        dsa.update(blockMessage.getBytes("UTF-8"));
        return dsa.verify(DatatypeConverter.parseHexBinary(sign));
    }

    private boolean verify(String blockMessage, String pubKey, String sign) throws Exception {
        return verify(blockMessage, decodePublicKeyFromString(pubKey), sign);
    }

    private String encodeKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    private PublicKey decodePublicKeyFromString(String key) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePublic(new X509EncodedKeySpec(decodeKeyFromString(key)));
    }

    private PrivateKey decodePrivateKeyFromString(String key) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePrivate(new PKCS8EncodedKeySpec(decodeKeyFromString(key)));
    }

    private byte[] decodeKeyFromString(String key) throws UnsupportedEncodingException {
        return Base64.getDecoder().decode(key.getBytes("UTF-8"));
    }

}
package app.utils;

import javafx.util.Pair;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class CryptoUtils {

    public boolean isValid(String block, String previousHash, String publicKey) throws Exception {
        return verify(extract("message", block)
                        + extract("owner", block)
                        + extract("aadhar", block)
                        + previousHash,
                publicKey, extractSignature(block));
    }

    public List<String> getBlocks() throws Exception {
        return Files.readAllLines(Paths.get("blocks.dat"));
    }

    public List<String> appendBlocks(String... blocks) throws Exception {
        Writer output = new BufferedWriter(new FileWriter("blocks.dat", true));
        for (String block : blocks) {
            output.append(block + "\n");
            output.flush();
        }

        output.close();

        return getBlocks();
    }

    public String blockchain() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        String genesis = createGenesisBlock(keysAdmin.getKey(), keysAdmin.getValue());

        String message = "Coupon201KX";
        String owner = "Devashish";
        String aadhar = "19452";
        String firstBlock = createBlock(keysDev.getKey(), keysDev.getValue(), message, owner, aadhar, extractSignature(genesis));

        return surroundWithBraces(addComma(genesis, firstBlock));
    }


    public String createGenesisBlock(String publicKey, String privateKey) throws Exception {
        String message = "Genesis Block";
        String owner = "Rajiv";
        String aadhar = "12582";

        return createBlock(publicKey, privateKey, message, owner, aadhar, "");
    }

    public String createBlock(String publicKey, String privateKey, String message, String owner, String aadhar, String previousHash) throws Exception {
        String sign = sign(privateKey, message + owner + aadhar + previousHash);

        System.out.println(verify(message + owner + aadhar + previousHash, publicKey, sign));

        return blockFormat(sign, message, owner, aadhar);
    }

    public String blockFormat(String sign, String message, String owner, String aadhar) {
        return "\"" + sign + "\":" + surroundWithBraces(addComma(keyValuePair("message", message), keyValuePair("owner", owner),
                keyValuePair("aadhar", aadhar)));
    }

    public String extractSignature(String block) {
        //Pattern signature = Pattern.compile(".*\"sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return block.split("\":")[0].substring(1);
    }

    public String extract(String field, String block) {
        return block.split("\"" + field + "\":\"")[1].split("\"")[0];
    }

    public String surroundWithBraces(String value) {
        return "{" + value + "}";
    }

    public String addComma(String... values) {
        return String.join(",", values);
    }

    public String addComma(List<String> values) {
        return addComma(values.toArray(new String[0]));
    }

    public String superKeyValuePair(String key, String value) {
        return "\"" + key + "\":{" + value + "}";
    }

    public String keyValuePair(String key, String value) {
        return keyValuePair(key, value, true);
    }

    public String keyValuePair(String key, String value, boolean noBraces) {
        if (noBraces) {
            return "\"" + key + "\":\"" + value + "\"";
        }

        return surroundWithBraces(key + ":" + value);
    }

    public Pair<PublicKey, PrivateKey> generateKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        //TODO: Use TRNG somehow
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(256, random);

        KeyPair pair = keyGen.generateKeyPair();
        PublicKey pub = pair.getPublic();
        PrivateKey priv = pair.getPrivate();

        return new Pair<>(pub, priv);
    }

    public String sign(PrivateKey privateKey, String message) throws Exception {
        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initSign(privateKey);
        dsa.update(message.getBytes("UTF-8"));
        return new BigInteger(1, dsa.sign()).toString(16);
    }

    public String sign(String privKey, String message) throws Exception {
        return sign(decodePrivateKeyFromString(privKey), message);
    }

    public boolean verify(String blockMessage, PublicKey publicKey, String sign) throws Exception {
        Signature dsa = Signature.getInstance("SHA1withECDSA");
        dsa.initVerify(publicKey);
        dsa.update(blockMessage.getBytes("UTF-8"));
        return dsa.verify(DatatypeConverter.parseHexBinary(sign));
    }

    public boolean verify(String blockMessage, String pubKey, String sign) throws Exception {
        return verify(blockMessage, decodePublicKeyFromString(pubKey), sign);
    }

    public String encodeKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public PublicKey decodePublicKeyFromString(String key) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePublic(new X509EncodedKeySpec(decodeKeyFromString(key)));
    }

    public PrivateKey decodePrivateKeyFromString(String key) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("EC");
        return fact.generatePrivate(new PKCS8EncodedKeySpec(decodeKeyFromString(key)));
    }

    public byte[] decodeKeyFromString(String key) throws UnsupportedEncodingException {
        return Base64.getDecoder().decode(key.getBytes("UTF-8"));
    }

}

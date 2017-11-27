package hello;

import javafx.util.Pair;
import org.eclipse.jetty.client.api.Response;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
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
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = getBlocks();

        return surroundWithBraces(addComma(blocks));
    }

    //TODO: Change to PostMapping
    @GetMapping(value = "/create", produces = "application/json")
    public Object addBlock(@RequestParam String message, @RequestParam String owner, @RequestParam String aadhar) throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = getBlocks();

        appendBlocks(createBlock(keysDev.getKey(), keysDev.getValue(), message, owner, aadhar, extractSignature(blocks.get(blocks.size() - 1))));
        return Response.SC_OK;
    }

    @GetMapping(value = "/authorized", produces = "application/json")
    public String showAuthorized() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));


        return surroundWithBraces(addComma(
                superKeyValuePair("Dev", keyValuePair("publicKey", keysAdmin.getKey())),
                superKeyValuePair("Devashish", keyValuePair("publicKey", keysDev.getKey())),
                superKeyValuePair("Rajiv", keyValuePair("publicKey", keysRajiv.getKey()))
        ));
    }

    @GetMapping(value = "/verifyAllSignatures", produces = "application/json")
    public String verifyAllSignatures() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = getBlocks();

        String genesis = blocks.get(0);
        boolean isValid = verify(extract("message", genesis) + extract("owner", genesis) + extract("aadhar", genesis),
                keysAdmin.getKey(), extractSignature(genesis));

        for (int i = 1; i < blocks.size(); i++) {
            String block = blocks.get(i);
            String previousHash = extractSignature(blocks.get(i - 1));
            isValid = isValid & (isValid(block, previousHash, keysAdmin.getKey()) |
                    isValid(block, previousHash, keysDev.getKey()) |
                    isValid(block, previousHash, keysRajiv.getKey())
            );
        }

        return String.valueOf(isValid);
    }

    @PostMapping(value = "/verify")
    public String verifySignature(@RequestParam String sign, @RequestParam String pubKey, @RequestParam String data) throws Exception {
        return "<form action=\"verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form><br/><br/>" +
                "Signature Verified: " + verify(data, pubKey, sign);
    }

    @GetMapping(value = "/verify-form")
    public String verifyForm() {
        return "<form action=\"verify\" method=\"post\">" +
                "Signature: <input type=\"text\" name=\"sign\"/><br/><br/>" +
                "BlockData: <input type=\"text\" name=\"data\"/><br/><br/>" +
                "pubKey: <input type=\"text\" name=\"pubKey\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";
    }


    private boolean isValid(String block, String previousHash, String publicKey) throws Exception {
        return verify(extract("message", block)
                        + extract("owner", block)
                        + extract("aadhar", block)
                        + previousHash,
                publicKey, extractSignature(block));
    }

    private List<String> getBlocks() throws Exception {
        return Files.readAllLines(Paths.get("blocks.dat"));
    }

    private List<String> appendBlocks(String... blocks) throws Exception {
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

        String message = "Plot 1, Shastri Nagar, Adyar, Chennai 600023";
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

    private String extractSignature(String block) {
        //Pattern signature = Pattern.compile(".*\"sign\":\"(.*)\".*");
        //return signature.matcher(block).group(0);
        return block.split("\":")[0].substring(1);
    }

    private String extract(String field, String block) {
        return block.split("\"" + field + "\":\"")[1].split("\"")[0];
    }

    private String surroundWithBraces(String value) {
        return "{" + value + "}";
    }

    private String addComma(String... values) {
        return String.join(",", values);
    }

    private String addComma(List<String> values) {
        return addComma(values.toArray(new String[0]));
    }

    private String superKeyValuePair(String key, String value) {
        return "\"" + key + "\":{" + value + "}";
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
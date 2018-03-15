package app.controller;

import app.service.CryptoService;
import javafx.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class BlockchainController {

    CryptoService cryptoService = new CryptoService();

    @GetMapping(value = "/coupons", produces = "application/json")
    public String couponBlockchain() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = cryptoService.getBlocks();

        return cryptoService.surroundWithBraces(cryptoService.addComma(blocks));
    }

    //TODO: Change to PostMapping
    @GetMapping(value = "/create", produces = "application/json")
    public String addBlock(@RequestParam String message, @RequestParam String owner, @RequestParam String aadhar) throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = cryptoService.getBlocks();

        String prevHash = cryptoService.extractSignature(blocks.get(blocks.size() - 1));
        cryptoService.appendBlocks(cryptoService.createBlock(keysDev.getKey(), keysDev.getValue(), message, owner, aadhar, prevHash));

        return "Success: \nBlockdata: " + message+owner+aadhar+prevHash + " \nPublicKey: " + keysDev.getKey();
    }

    @GetMapping(value = "/authorized", produces = "application/json")
    public String showAuthorized() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));


        return cryptoService.surroundWithBraces(cryptoService.addComma(
                cryptoService.superKeyValuePair("Dev", cryptoService.keyValuePair("publicKey", keysAdmin.getKey())),
                cryptoService.superKeyValuePair("Devashish", cryptoService.keyValuePair("publicKey", keysDev.getKey())),
                cryptoService.superKeyValuePair("Rajiv", cryptoService.keyValuePair("publicKey", keysRajiv.getKey()))
        ));
    }

    @GetMapping(value = "/verifyAllSignatures", produces = "application/json")
    public String verifyAllSignatures() throws Exception {
        List<String> keys = Files.readAllLines(Paths.get("keys.dat"));
        Pair<String, String> keysAdmin = new Pair<>(keys.get(0), keys.get(1)),
                keysDev = new Pair<>(keys.get(2), keys.get(3)),
                keysRajiv = new Pair<>(keys.get(4), keys.get(5));

        List<String> blocks = cryptoService.getBlocks();

        String genesis = blocks.get(0);
        boolean isValid = cryptoService.verify(cryptoService.extract("message", genesis) + cryptoService.extract("owner", genesis) + cryptoService.extract("aadhar", genesis),
                keysAdmin.getKey(), cryptoService.extractSignature(genesis));

        for (int i = 1; i < blocks.size(); i++) {
            String block = blocks.get(i);
            String previousHash = cryptoService.extractSignature(blocks.get(i - 1));
            isValid = isValid & (cryptoService.isValid(block, previousHash, keysAdmin.getKey()) |
                    cryptoService.isValid(block, previousHash, keysDev.getKey()) |
                    cryptoService.isValid(block, previousHash, keysRajiv.getKey())
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
                "Signature Verified: " + cryptoService.verify(data, pubKey, sign);
    }

    @GetMapping(value = "/verify-form")
    public String verifyForm() {
        return "<form action=\"verify\" method=\"post\">" +
                "Hash: <input type=\"text\" name=\"sign\"/><br/><br/>" +
                "BlockData: <input type=\"text\" name=\"data\"/><br/><br/>" +
                "pubKey: <input type=\"text\" name=\"pubKey\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";
    }



}

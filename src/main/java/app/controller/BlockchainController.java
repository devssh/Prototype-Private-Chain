package app.controller;

import app.service.CryptoService;
import app.service.KeyzManager;
import app.service.SignService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Optional;

@RestController
public class BlockchainController {

    CryptoService cryptoService = new CryptoService("keys.dat", "blocks.dat");
    String messageKey = "message";
    String ownerKey = "owner";
    String aadharKey = "aadhar";

    public String basicSign = "MFkwEw";

    public BlockchainController() throws Exception {
    }

    @GetMapping(value = "/coupons-explorer", produces = "application/json")
    public String couponBlockchain() throws Exception {
        return cryptoService.getBlockchain();
    }

    //TODO: Change to PostMapping
    @PostMapping(value = "/create")
    public String addBlock(@RequestParam String sign, @RequestParam String message, @RequestParam String owner, @RequestParam String aadhar) throws Exception {
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();
            HashMap<String, String> block = cryptoService.addBlock(message, owner, aadhar);
            return "<form action=\"create\" method=\"post\">" +
                    "Sign: <input type=\"text\" name=\"sign\"/><br/><br/>" +
                    "Message: <input type=\"text\" name=\"message\"/><br/><br/>" +
                    "Owner: <input type=\"text\" name=\"owner\"/><br/><br/>" +
                    "Aadhar: <input type=\"text\" name=\"aadhar\"/><br/><br/>" +
                    "<input type=\"submit\" value=\"Submit\"/>" +
                    "</form><br/><br/>" +

                    "Success: <br/><br/>" +
                    verifyForm(
                            Optional.of(block.get("Signature")),
                            Optional.of(block.get("Blockdata")),
                            Optional.of(block.get("Public Key"))
                    ) +
                    "<br/><br/>Computational time: " + (System.currentTimeMillis() - start) + "ms";
        }
        return "Invalid signature for Dev";
    }

    //TODO: Change to PostMapping
    @GetMapping(value = "/create")
    public String createBlock() throws Exception {
        return "<form action=\"create\" method=\"post\">" +
                "Sign: <input style=\"width:90%\" type=\"text\" name=\"sign\"/><br/><br/>" +
                "Message: <input style=\"width:90%\" type=\"text\" name=\"message\"/><br/><br/>" +
                "Owner: <input style=\"width:90%\" type=\"text\" name=\"owner\"/><br/><br/>" +
                "Aadhar: <input style=\"width:90%\" type=\"text\" name=\"aadhar\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";
    }

    @GetMapping(value = "/authorized", produces = "application/json")
    public String showAuthorized() throws Exception {
        return cryptoService.showAuthorized();
    }

    @GetMapping(value = "/verifyAllSignatures", produces = "application/json")
    public String verifyAllSignatures() throws Exception {
        return cryptoService.verifyAllSignatures();
    }

    @PostMapping(value = "/verify")
    public String verifySignature(@RequestParam String sign, @RequestParam String data, @RequestParam String pubKey) throws Exception {
        return "<form action=\"verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.trim() + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey.trim() + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form><br/><br/>" +
                "Signature Verified: " + SignService.verify(data, pubKey, sign);
    }

    @GetMapping(value = "/verify")
    public String verifyForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> data, @RequestParam Optional<String> pubKey) {
        return "<form action=\"verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.get().trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.get().trim() + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey.get().trim() + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";
    }

    @GetMapping(value = "/showGenesis")
    public String showGenesis(@RequestParam String message, @RequestParam String owner, @RequestParam String aadhar) throws Exception {
        return cryptoService.showGenesis(message, owner, aadhar);
    }

    @GetMapping(value = "/generateKey")
    public String generateKey() throws Exception {
        return cryptoService.generateKeyString();
    }

    @GetMapping(value = "/stats")
    public String getStats() throws Exception {
        return cryptoService.getStats();
    }

    @GetMapping(value="/sign")
    public String getSign(@RequestParam String data) throws Exception{
        SignService signService = new SignService(new KeyzManager(cryptoService.keyzManager.keyFile));
        return signService.signWith("Dev", data);
    }


}

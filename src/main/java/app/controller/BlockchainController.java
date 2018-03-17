package app.controller;

import app.service.CryptoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

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
    @GetMapping(value = "/create", produces = "application/json")
    public String addBlock(@RequestParam String sign, @RequestParam String message, @RequestParam String owner, @RequestParam String aadhar) throws Exception {
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();
            String out = cryptoService.addBlock(message, owner, aadhar);
            return "Success: \n" + out + "\nComputational time: " + (System.currentTimeMillis() - start) + "ms";
        }
        return "Invalid signature for Dev";
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
    public String verifySignature(@RequestParam String sign, @RequestParam String pubKey, @RequestParam String data) throws Exception {
        return "<form action=\"verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.trim() + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey.trim() + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form><br/><br/>" +
                "Signature Verified: " + cryptoService.verify(data, pubKey, sign);
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

    @GetMapping(value = "/showGenesis")
    public String showGenesis(@RequestParam String message, @RequestParam String owner, @RequestParam String aadhar) throws Exception {
        return cryptoService.showGenesis(message, owner, aadhar);
    }

    @GetMapping(value = "/generateKey")
    public String generateKey() throws Exception {
        return cryptoService.generateKeyString();
    }


}

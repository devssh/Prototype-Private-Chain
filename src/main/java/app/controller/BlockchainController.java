package app.controller;

import app.model.Block;
import app.model.TxnDao;
import app.service.CryptoService;
import app.service.KeyzManager;
import app.service.SignService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class BlockchainController {
    public static final String AUTHORITIES_DAT = "authorities.dat";
    public static final String USERS_DAT = "users.dat";
    public static final String BLOCKS_DAT = "blocks.dat";
    SignService signService = new SignService(new KeyzManager(AUTHORITIES_DAT), new KeyzManager(USERS_DAT));
    CryptoService cryptoService = new CryptoService(AUTHORITIES_DAT, USERS_DAT, BLOCKS_DAT);

    public String basicSign = "MFkwEw";

    public BlockchainController() throws Exception {
    }

    @GetMapping(value = "/coupons-explorer", produces = "application/json")
    public String couponBlockchain() throws Exception {
        return cryptoService.getBlockchain();
    }

    @PostMapping(value = "/create")
    public String addBlock(@RequestParam String sign, @ModelAttribute TxnDao txndao) throws Exception {
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();
            Block block = cryptoService.addBlock(txndao.getTxn("Sharath", signService), "Dev");
            return "<form action=\"create\" method=\"post\">" +
                    "Sign: <input style=\"width:90%\" type=\"text\" name=\"Sign\"/><br/><br/>" +
                    "txnid: <input style=\"width:90%\" type=\"text\" name=\"message\"/><br/><br/>" +
                    "email: <input style=\"width:90%\" type=\"text\" name=\"owner\"/><br/><br/>" +
                    "location: <input style=\"width:90%\" type=\"text\" name=\"aadhar\"/><br/><br/>" +
                    "<input type=\"submit\" value=\"Submit\"/>" +
                    "</form>" +
                    "<br/><br/>" +

                    "Success: <br/><br/>" +
                    verifyForm(
                            Optional.of(block.sign),
                            Optional.of(block.chunk.data),
                            Optional.of(block.chunk.publicKey)
                    ) +
                    "<br/><br/>Computational time: " + (System.currentTimeMillis() - start) + "ms";
        }
        return "Invalid signature for Dev";
    }

    @GetMapping(value = "/create")
    public String createBlock(@RequestParam Optional<String> sign, @RequestParam Optional<TxnDao> txndao) throws Exception {
        return "<form action=\"create\" method=\"post\">" +
                "Sign: <input style=\"width:90%\" type=\"text\" name=\"Sign\" value=\""+"\" /><br/><br/>" +
                "txnid: <input style=\"width:90%\" type=\"text\" name=\"message\"/><br/><br/>" +
                "email: <input style=\"width:90%\" type=\"text\" name=\"owner\"/><br/><br/>" +
                "location: <input style=\"width:90%\" type=\"text\" name=\"aadhar\"/><br/><br/>" +
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
        return "<form action=\"Verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"Sign\" value=\"" + sign.trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.trim() + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey.trim() + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form><br/><br/>" +
                "Signature Verified: " + SignService.Verify(data, pubKey, sign);
    }

    @GetMapping(value = "/verify")
    public String verifyForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> data, @RequestParam Optional<String> pubKey) {
        return "<form action=\"Verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"Sign\" value=\"" + sign.get().trim() + "\"/><br/><br/>" +
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
        SignService signService = new SignService(new KeyzManager(cryptoService.authoritiesManager.keyFile));
        return signService.signWith("Dev", data);
    }


}

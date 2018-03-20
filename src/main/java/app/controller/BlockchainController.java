package app.controller;

import app.model.Block;
import app.model.TxnDao;
import app.model.VariableManager;
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
    public String createBlock(@RequestParam String sign, @RequestParam String txnid, @RequestParam String email, @RequestParam String location) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), email.trim(), location.trim());
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();
            Block block = cryptoService.addBlock("Dev", txnDao.getTxn("Sharath", signService));
            return createForm(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()) +
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

    @PostMapping(value = "/create-api")
    public String createBlockApi(@RequestParam String sign, @RequestParam String txnid, @RequestParam String email, @RequestParam String location) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), email.trim(), location.trim());
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();
            Block block = cryptoService.addBlock("Dev", txnDao.getTxn("Sharath", signService));
            return new VariableManager(
                    "sign", block.sign,
                    "data", block.chunk.data,
                    "pubKey", block.chunk.publicKey,
                    "mineTime", String.valueOf(System.currentTimeMillis() - start) + "ms").jsonString();
        }
        return "Invalid signature for Dev";
    }

    @GetMapping(value = "/create")
    public String createForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> txnid, @RequestParam Optional<String> email, @RequestParam Optional<String> location) throws Exception {
        return "<form action=\"create\" method=\"post\">" +
                "Sign: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.orElse("").trim() + "\" /><br/><br/>" +
                "txnid: <input style=\"width:90%\" type=\"text\" name=\"txnid\" value=\"" + txnid.orElse("").trim() + "\" /><br/><br/>" +
                "email: <input style=\"width:90%\" type=\"text\" name=\"email\" value=\"" + email.orElse("").trim() + "\" /><br/><br/>" +
                "location: <input style=\"width:90%\" type=\"text\" name=\"location\" value=\"" + location.orElse("").trim() + "\" /><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";
    }

    @GetMapping(value = "/authorized", produces = "application/json")
    public String showAuthorized() throws Exception {
        return cryptoService.showAuthorized();
    }

    @GetMapping(value = "/users", produces = "application/json")
    public String showUsers() throws Exception {
        return cryptoService.showUsers();
    }

    @GetMapping(value = "/verifyAllSignatures", produces = "application/json")
    public String verifyAllSignatures() throws Exception {
        return cryptoService.verifyAllSignatures();
    }

    @PostMapping(value = "/verify")
    public String verifySignature(@RequestParam String sign, @RequestParam String data, @RequestParam String pubKey) throws Exception {
        return verifyForm(Optional.of(sign), Optional.of(data), Optional.of(pubKey)) +
                "<br/><br/>" +
                "Signature Verified: " + SignService.Verify(data, pubKey, sign);
    }

    @PostMapping(value = "/verify-api")
    public String verifySignatureApi(@RequestParam String sign, @RequestParam String data, @RequestParam String pubKey) throws Exception {
        return new VariableManager(
                "sign", sign,
                "data", data,
                "pubKey", pubKey,
                "verified", String.valueOf(SignService.Verify(data, pubKey, sign))
        ).jsonString();
    }

    @GetMapping(value = "/verify")
    public String verifyForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> data, @RequestParam Optional<String> pubKey) {
        return "<form action=\"verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.orElse("").trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.orElse("").trim() + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey.orElse("").trim() + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";
    }

    @GetMapping(value = "/createGenesis")
    public String showGenesis() throws Exception {
        return new Block(signService.keyzManager.getKey("Dev"), "", new TxnDao("007", "devs@gmail.com", "Store 40, San Jose, California").getTxn("Sharath", signService)).toString();
    }

    @GetMapping(value = "/generateKey")
    public String generateKey() throws Exception {
        return cryptoService.generateKeyString();
    }

    @GetMapping(value = "/stats")
    public String getStats() throws Exception {
        return cryptoService.getStats();
    }

    @GetMapping(value = "/sign")
    public String getSign(@RequestParam String data) throws Exception {
        SignService signService = new SignService(new KeyzManager(cryptoService.authoritiesManager.keyFiles), new KeyzManager(cryptoService.usersManager.keyFiles));
        return signService.signWith("Dev", data);
    }


}

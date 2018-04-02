package app.controller;

import app.model.*;
import app.service.CryptoService;
import app.service.SignService;
import app.utils.HtmlUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static app.model.Block.BLOCK_SIGN;
import static app.model.Blockchain.*;
import static app.model.StringVar.Join;
import static app.model.StringVar.Vars;
import static app.model.Txn.*;
import static app.model.Verifiable.DATA;
import static app.model.Verifiable.PUBLIC_KEY;
import static app.model.Verifiable.SIGN;
import static app.service.AuthService.CheckValidSign;
import static app.service.FileUtils.ReadBlockchain;
import static app.service.HtmlService.Header;
import static app.service.HtmlService.HomeRedirectButton;
import static app.service.KeyzManager.GetKey;
import static app.service.SignService.SignWith;
import static app.utils.Exceptions.DOUBLE_SPEND_ATTEMPTED;
import static app.utils.Exceptions.FAILED_TO_CREATE_TXN;
import static app.utils.HtmlUtils.*;

@RestController
public class BlockchainController {
    public static final String TIMEOUT_TRANSACTION_NOT_CONFIRMED_YET_CONSIDER_REATTEMPTING_TO_CREATE_IT = "Timeout: Transaction not confirmed yet, consider reattempting to create it";
    final CryptoService cryptoService = new CryptoService();

    public BlockchainController() throws Exception {
        InitCompletedTxns();
    }

    @Scheduled(fixedRate = 1000)
    public void createBlock() throws Exception {
        MineBlock();
    }

    @GetMapping(value = "/coupons", produces = "application/json")
    public String blockchain() throws Exception {
        return cryptoService.getBlockchain();
    }

    @GetMapping(value = "/blockchain", produces = "application/json")
    public String rawdata() {
        return "TODO";
    }

    @GetMapping(value = "/")
    public String blockExplorer() throws Exception {
        List<Block> blocks = ReadBlockchain();
        Collections.reverse(blocks);
        List<String> blockchainHTML = new ArrayList<>();
        for (int depth = 0; depth < blocks.size(); depth++) {
            blockchainHTML.add(HtmlUtils.TableRows(blocks.get(depth), depth + 1));
        }
        return "<div style=\"font-size:15px\" >" +
                Header() +
                "<br/>Difficulty is set to 3 zeros in hexadecimal ~ 16 * 16 * 16 = 4096 nonces to find block ~ $" +
                String.valueOf(COST_PER_BLOCK * 1000000) + " per 1,000,000 blocks. " +
                "<br/>Avg block time is 1 second so cost to modify block created today will be $" + String.valueOf(COST_PER_BLOCK * 1000000) + " in ~ 11.5 days at this difficulty. " +
                "Increasing difficulty to 4 increases that cost to ~ "+String.valueOf(COST_PER_BLOCK * 1000000)+" x 16 = $" + String.valueOf(COST_PER_BLOCK * 1000000 * 16) +" in 11.5 days."+
                "<br/>Increasing difficulty to 18 zeros like current Bitcoin difficulty increases that cost to $47,158,741 in 1 hour."+
                "<table>" + Join(blockchainHTML) + "</table>" +
                "</div>";
    }

    @GetMapping(value = "/block/{blockSign}")
    public String getBlock(@PathVariable("blockSign") String blockSign) throws Exception {
        Block block = Block.Deserialize(cryptoService.getBlock(blockSign));
        return verifyForm(Optional.of(block.sign), Optional.of(block.data), Optional.of(block.publicKey));
    }

    @GetMapping(value = "/users", produces = "application/json")
    public String showUsers() {
        return cryptoService.showUsers();
    }

    @GetMapping(value = "/create")
    public String createForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> txnid, @RequestParam Optional<String> email) {
        return "Create token - Careful, the email will be sent, leave blank if not needed" +
                Form("create", "post", "Create Redeemable Token",
                        Vars(Txn.SIGN, sign.orElse(""), TXNID, txnid.orElse(""), EMAIL, email.orElse("")).toArray(new StringVar[3]));
    }

    @PostMapping(value = "/create")
    public String createBlock(@RequestParam String sign, @RequestParam String txnid, @RequestParam String email) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), email.trim(), "");
        if (CheckValidSign(sign)) {

            Txn createTxn = txnDao.getTxn("Sharath", CREATE);
            if (ADD_TXN_TO_UTXOSET(createTxn)) {
                return "<div id=\"response\">Transaction to create " + txnid.trim() + " has been submitted for processing...</div>" +
                        Ajax("/verifyTxnCreated/" + txnid.trim(), "response");
            }
            return "Token already exists and cannot be created again";
        }
        return "Invalid signature for " + "Dev";
    }

    @PostMapping(value = "/createApi")
    public String createBlockApi(@RequestParam String sign, @RequestParam String txnid, @RequestParam String email) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), email, "");
        if (CheckValidSign(sign)) {
            Long start = System.currentTimeMillis();

            int count = ReadBlockchain().size();
            Txn createTxn = txnDao.getTxn("Sharath", CREATE);
            if (!ADD_TXN_TO_UTXOSET(createTxn)) {
                throw new Exception("Cannot create duplicate txn");
            }
            while (ReadBlockchain().size() == count) {
                Thread.sleep(500);
            }
            List<Block> blocks = ReadBlockchain();

            Block block = blocks.get(blocks.size() - 1);
            return new VariableManager(
                    "sign", block.sign,
                    "data", block.data,
                    "pubKey", block.publicKey,
                    "mineTime", String.valueOf(System.currentTimeMillis() - start) + "ms").jsonString();
        }
        return "Invalid signature for Dev";
    }

    @GetMapping(value = "/verifyTxnCreated/{txnid}")
    public String confirmTxnCreate(@PathVariable("txnid") String txnid) {
        long timeout = 30000L, startTime = System.currentTimeMillis(), endTime = startTime + timeout;
        Txn completedTxnThatIsNeeded;
        do {
            completedTxnThatIsNeeded = GetCompletedTxn(txnid, CREATE);
        } while (completedTxnThatIsNeeded == null && System.currentTimeMillis() < endTime);

        try {
            String blockByTxn = cryptoService.getBlockByTxn(txnid, CREATE);
            Block block = Block.Deserialize(blockByTxn);
            return createForm(Optional.empty(), Optional.empty(), Optional.empty()) +
                    "Successfully created the block <br/><br/><br/>" +
                    verifyForm(
                            Optional.of(block.sign),
                            Optional.of(block.data),
                            Optional.of(block.publicKey)
                    );
        } catch (Exception e) {
            e.printStackTrace();
            FAILED_TO_CREATE_TXN.printStackTrace();
        }

        return TIMEOUT_TRANSACTION_NOT_CONFIRMED_YET_CONSIDER_REATTEMPTING_TO_CREATE_IT;
    }

    @GetMapping(value = "/redeem")
    public String redeemForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> txnid, @RequestParam Optional<String> location) {
        return "Redeem token<br/><br/>" +
                Form("redeem", "post", "Redeem token at location",
                        Vars(Txn.SIGN, sign.orElse(""), TXNID, txnid.orElse(""), LOCATION, location.orElse("")).toArray(new StringVar[3]));
    }

    @PostMapping(value = "/redeem")
    public String createRedeemBlock(@RequestParam String sign, @RequestParam String txnid, @RequestParam String location) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), "", location.trim());
        if (CheckValidSign(sign)) {
            Txn redeemTxn = txnDao.getTxn("Sharath", REDEEM);

            if (ADD_TXN_TO_UTXOSET(redeemTxn)) {
                String responseDivId = "response";
                return "<div id=\"" + responseDivId + "\">Transaction to redeem " + txnid.trim() + " has been submitted for processing...</div>" +
                        Ajax("/verifyTxnRedeemed/" + txnid.trim(), responseDivId);
            }
            throw DOUBLE_SPEND_ATTEMPTED;

        }
        return "Invalid signature for Dev";
    }


    @GetMapping(value = "/verifyTxnRedeemed/{txnid}")
    public String confirmTxnRedeem(@PathVariable("txnid") String txnid) throws Exception {
        long timeout = 30000L, startTime = System.currentTimeMillis(), endTime = startTime + timeout;
        Txn completedTxn;
        do {
            completedTxn = GetCompletedTxn(txnid, REDEEM);
        } while (completedTxn == null && System.currentTimeMillis() < endTime);

        if (completedTxn != null) {
            String blockByTxn = cryptoService.getBlockByTxn(txnid, REDEEM);
            Block block = Block.Deserialize(blockByTxn);
            return redeemForm(Optional.empty(), Optional.empty(), Optional.empty()) +
                    "Successfully created the redemption block <br/><br/><br/>" +
                    verifyForm(
                            Optional.of(block.sign),
                            Optional.of(block.data),
                            Optional.of(block.publicKey)
                    );
        }

        return TIMEOUT_TRANSACTION_NOT_CONFIRMED_YET_CONSIDER_REATTEMPTING_TO_CREATE_IT;
    }

    @PostMapping(value = "/redeemApi")
    public String redeemBlockApi(@RequestParam String sign, @RequestParam String txnid, @RequestParam String location) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), "", location.trim());
        if (CheckValidSign(sign)) {
            Long start = System.currentTimeMillis();

            int count = ReadBlockchain().size();
            Txn redeemTxn = txnDao.getTxn("Sharath", REDEEM);
            ADD_TXN_TO_UTXOSET(redeemTxn);
            while (ReadBlockchain().size() == count) {
                Thread.sleep(500);
            }
            List<Block> blocks = ReadBlockchain();

            Block block = blocks.get(blocks.size() - 1);
            return new VariableManager(
                    BLOCK_SIGN, block.sign,
                    VariableManager.DATA, block.data,
                    Txn.PUBLIC_KEY, block.publicKey).jsonString();
        }
        return "Invalid signature for Dev";
    }

    @GetMapping(value = "/verifyAllSignatures", produces = "application/json")
    public String verifyAllSignatures() throws Exception {
        return cryptoService.verifyAllSignatures();
    }

    @PostMapping(value = "/verify")
    public String verifySignature(@RequestParam String sign, @RequestParam String data, @RequestParam String publicKey) throws Exception {
        return verifyForm(Optional.of(sign), Optional.of(data), Optional.of(publicKey)) +
                "<br/><br/>" +
                "Signature Verified: " + SignService.Verify(data, publicKey, sign);
    }

    @PostMapping(value = "/verifyApi")
    public String verifySignatureApi(@RequestParam String sign, @RequestParam String data, @RequestParam String publicKey) throws Exception {
        return new VariableManager(
                SIGN, sign,
                DATA, data,
                PUBLIC_KEY, publicKey,
                "verified", String.valueOf(SignService.Verify(data, publicKey, sign))
        ).jsonString();
    }

    @GetMapping(value = "/verify")
    public String verifyForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> data, @RequestParam Optional<String> publicKey) {
        return HomeRedirectButton() +
                "Signature Verification utility using ECDSA" +
                "<form action=\"/verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.orElse("").trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.orElse("").trim() + "\"/><br/><br/>" +
                "publicKey: <input style=\"width:90%\" type=\"text\" name=\"publicKey\" value=\"" + publicKey.orElse("").trim() + "\"/><br/><br/>" +
                "<input type=\"submit\" value=\"Verify Signature\"/>" +
                "</form>";
    }

    @GetMapping(value = "/createGenesis")
    public String showGenesis() throws Exception {
        return new Block(GetKey("Dev"), "", new TxnDao("CoupMLK20", "devs@gmail.com", "").getTxn("Sharath", "create")).toString();
    }

    @GetMapping(value = "/generateKey")
    public String generateKey() throws Exception {
        return cryptoService.generateKeyString();
    }

    @GetMapping(value = "/stats")
    public String getStats() {
        return cryptoService.getStats();
    }

    @GetMapping(value = "/sign")
    public String getSign(@RequestParam String data) throws Exception {
        return SignWith("Dev", data);
    }


}

package app.controller;

import app.model.*;
import app.service.CryptoService;
import app.service.SignService;
import app.utils.HtmlUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static app.model.Block.BLOCK_SIGN;
import static app.model.StringVar.Join;
import static app.model.StringVar.Vars;
import static app.model.Txn.*;
import static app.model.Verifiable.DATA;
import static app.model.Verifiable.PUBLIC_KEY;
import static app.model.Verifiable.SIGN;
import static app.service.HtmlService.Header;
import static app.service.HtmlService.HomeRedirectButton;
import static app.service.KeyzManager.GetKey;
import static app.service.MailSenderService.SendMailWithQRCode;
import static app.service.QRCodeService.GenerateQRCodeImage;
import static app.service.SignService.SignWith;
import static app.utils.BlockManager.GetBlockObjects;
import static app.utils.BlockManager.GetTxns;
import static app.utils.Exceptions.DOUBLE_SPEND_ATTEMPTED;
import static app.utils.HtmlUtils.Ajax;
import static app.utils.HtmlUtils.Form;
import static app.utils.Properties.basicSign;

@RestController
public class BlockchainController {
    public static final String MY_QRCODE_PNG = "MyQRCode.png";
    //TODO: extract this elsewhere
    public boolean processing = false;
    public final CopyOnWriteArrayList<Txn> utxoSet = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<Txn> completedTxns = new CopyOnWriteArrayList<>();

    final CryptoService cryptoService = new CryptoService();

    public BlockchainController() throws Exception {
        //TODO: verify no repeat txns
        completedTxns.addAll(GetTxns());
    }

    @Scheduled(fixedRate = 1000)
    public void createBlock() throws Exception {
        if (!processing) {
            int n = utxoSet.size();
            if (n > 0) {
                processing = true;
                CopyOnWriteArrayList<Txn> txnsInBlock = new CopyOnWriteArrayList<>();
                txnsInBlock.addAll(utxoSet.subList(0, n));
                cryptoService.addBlock("Dev", txnsInBlock.toArray(new Txn[n]));
                utxoSet.removeAll(txnsInBlock);
                completedTxns.addAll(txnsInBlock);
                processing = false;
            }
            if (utxoSet.size() > 0) {
                createBlock();
            }

        }
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
        List<Block> blocks = GetBlockObjects();
        return "<div style=\"font-size:15px\" >" +
                Header() +
                "<table>" + Join(blocks.stream().map(HtmlUtils::TableRows).collect(Collectors.toList())) + "</table>" +
                "</div>";
    }

    @GetMapping(value = "/block/{blockSign}")
    public String getBlock(@PathVariable("blockSign") String blockSign) throws Exception {
        Block block = Block.Deserialize(cryptoService.getBlock(blockSign));
        return verifyForm(Optional.of(block.sign), Optional.of(block.data), Optional.of(block.publicKey));
    }

    @GetMapping(value = "/authorized", produces = "application/json")
    public String showAuthorized() {
        return cryptoService.showAuthorized();
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
        if (sign.equals(basicSign)) {

            Txn createTxn = txnDao.getTxn("Sharath", CREATE);
            if (completedTxns.stream().filter(txn -> txn.varMan.get(TXNID).equals(createTxn.varMan.get(TXNID)) &&
                    txn.varMan.get(TYPE).equals(CREATE)).collect(Collectors.toList()).size() == 0) {
                utxoSet.add(createTxn);

                try {
                    GenerateQRCodeImage(createTxn.varMan.get(TXNID), 350, 350, MY_QRCODE_PNG);
                    SendMailWithQRCode(createTxn.varMan.get(EMAIL), "Coupon Testing Server - News America Marketing", "Hello, you have received a QR code from Yuval");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "<div id=\"response\">Transaction to create " + txnid.trim() + " has been submitted for processing...</div>" +
                        Ajax("/verifyTxnCreated/" + txnid.trim(), "response");
            }
            return "Token already exists and cannot be created again";
        }
        return "Invalid signature for Dev";
    }

    @PostMapping(value = "/createApi")
    public String createBlockApi(@RequestParam String sign, @RequestParam String txnid, @RequestParam String email) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), email, "");
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();

            int count = GetBlockObjects().size();
            Txn createTxn = txnDao.getTxn("Sharath", CREATE);
            utxoSet.add(createTxn);
            while (GetBlockObjects().size() == count) {
                Thread.sleep(500);
            }
            List<Block> blocks = GetBlockObjects();

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
    public String confirmTxnCreate(@PathVariable("txnid") String txnid) throws Exception {
        long timeout = 30000L, startTime = System.currentTimeMillis(), endTime = startTime + timeout;
        List<Txn> completedTxnList = new CopyOnWriteArrayList<>();
        do {
            completedTxnList.addAll(completedTxns.stream().filter(txn ->
                    txn.varMan.get(TXNID).equals(txnid) && txn.varMan.get(TYPE).equals(CREATE)
            ).collect(Collectors.toList()));
        } while (completedTxnList.size() == 0 && System.currentTimeMillis() < endTime);

        if (System.currentTimeMillis() > endTime && completedTxnList.size() == 0) {
            return "Timeout: Transaction not confirmed yet";
        }

        String blockByTxn = cryptoService.getBlockByTxn(txnid, CREATE);

        Block block = Block.Deserialize(blockByTxn);
        return createForm(Optional.empty(), Optional.empty(), Optional.empty()) +
                "Successfully created the block <br/><br/><br/>" +
                verifyForm(
                        Optional.of(block.sign),
                        Optional.of(block.data),
                        Optional.of(block.publicKey)
                );
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
        if (sign.equals(basicSign)) {
            Txn redeemTxn = txnDao.getTxn("Sharath", REDEEM);

            if (completedTxns.stream().filter(txn -> txn.varMan.get(TXNID).equals(redeemTxn.varMan.get(TXNID)) &&
                    txn.varMan.get(TYPE).equals(REDEEM)).collect(Collectors.toList()).size() == 0) {
                utxoSet.add(redeemTxn);
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
        List<Txn> completedTxnList = new CopyOnWriteArrayList<>();
        do {
            completedTxnList.addAll(completedTxns.stream().filter(txn ->
                    txn.varMan.get(TXNID).equals(txnid) && txn.varMan.get(TYPE).equals(REDEEM)
            ).collect(Collectors.toList()));
        } while (completedTxnList.size() == 0 && System.currentTimeMillis() < endTime);

        if (System.currentTimeMillis() > endTime && completedTxnList.size() == 0) {
            return "Timeout: Transaction not confirmed yet";
        }

        Block block = Block.Deserialize(cryptoService.getBlockByTxn(txnid, REDEEM));

        return redeemForm(Optional.empty(), Optional.empty(), Optional.empty()) +
                "Successfully created a redemption block <br/><br/><br/>" +
                verifyForm(
                        Optional.of(block.sign),
                        Optional.of(block.data),
                        Optional.of(block.publicKey)
                );
    }

    @PostMapping(value = "/redeemApi")
    public String redeemBlockApi(@RequestParam String sign, @RequestParam String txnid, @RequestParam String location) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), "", location.trim());
        if (sign.equals(basicSign)) {
            Long start = System.currentTimeMillis();

            int count = GetBlockObjects().size();
            Txn redeemTxn = txnDao.getTxn("Sharath", REDEEM);
            utxoSet.add(redeemTxn);
            while (GetBlockObjects().size() == count) {
                Thread.sleep(500);
            }
            List<Block> blocks = GetBlockObjects();

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

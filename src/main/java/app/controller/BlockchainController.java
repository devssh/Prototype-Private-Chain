package app.controller;

import app.model.Block;
import app.model.Txn;
import app.model.TxnDao;
import app.model.VariableManager;
import app.service.CryptoService;
import app.service.SignService;
import app.utils.HtmlUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static app.model.StringVar.JoinWith;
import static app.model.Txn.*;
import static app.service.KeyzManager.GetKey;
import static app.service.SignService.SignWith;
import static app.utils.BlockManager.GetBlockObjects;
import static app.utils.BlockManager.GetTxns;
import static app.utils.Exceptions.DOUBLE_SPEND_ATTEMPTED;
import static app.utils.HtmlUtils.Ajax;
import static app.utils.HtmlUtils.RedirectButton;
import static app.utils.Properties.basicSign;

@RestController
public class BlockchainController {
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

    @GetMapping(value = "/coupons-explorer")
    public String blockExplorer() throws Exception {
        List<Block> blocks = GetBlockObjects();
        return "<div style=\"font-size:15px\" >" +
                RedirectButton("Create Redeemable Token", "create") +
                RedirectButton("Redeem Token", "redeem") +
                RedirectButton("See Authorized Miners", "authorized") +
                RedirectButton("See User Information", "users") +
                JoinWith("", blocks.stream().map(HtmlUtils::Table).collect(Collectors.toList())) +
                "</div>";
    }

    @GetMapping(value = "/block/{blockSign}")
    public String getBlock(@PathVariable("blockSign") String blockSign) throws Exception {
        Block block = Block.Deserialize(cryptoService.getBlock(blockSign));
        return verifyForm(Optional.of(block.sign), Optional.of(block.data), Optional.of(block.publicKey)) + "<br/><br/>" + block.toString();
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
        return "<form action=\"create\" method=\"post\">" +
                "Sign: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.orElse("").trim() + "\" /><br/><br/>" +
                "txnid: <input style=\"width:90%\" type=\"text\" name=\"txnid\" value=\"" + txnid.orElse("").trim() + "\" /><br/><br/>" +
                "email: <input style=\"width:90%\" type=\"text\" name=\"email\" value=\"" + email.orElse("").trim() + "\" /><br/><br/>" +
                "<input type=\"submit\" value=\"Create redeemable token\"/>" +
                "</form>";
    }

    @PostMapping(value = "/create")
    public String createBlock(@RequestParam String sign, @RequestParam String txnid, @RequestParam String email) throws Exception {
        TxnDao txnDao = new TxnDao(txnid.trim(), email.trim(), "");
        if (sign.equals(basicSign)) {

            Txn createTxn = txnDao.getTxn("Sharath", CREATE);
            if (completedTxns.stream().filter(txn -> txn.varMan.get(TXNID).equals(createTxn.varMan.get(TXNID)) &&
                    txn.varMan.get(TYPE).equals(CREATE)).collect(Collectors.toList()).size() == 0) {
                utxoSet.add(createTxn);
                return "<div id=\"response\">Transaction to create " + txnid.trim() + " has been submitted for processing</div>" +
                        "<script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js\"></script> \n" +
                        "<script type=\"text/javascript\">\n" +
                        "    $(document).ready(function () {\n" +
                        "        $.ajax({\n" +
                        "            type: 'GET',\n" +
                        "            url: '/verifyTxnCreated/" + txnid.trim() + "',\n" +
                        "            dataType: \"text\",\n" +
                        "            crossDomain: true,\n" +
                        "            success: function (response) {\n" +
                        "                $(\"#response\").html(response);\n" +
                        "            },\n" +
                        "            error: function (request, status, error) {\n" +
                        "                alert(error);\n" +
                        "            }\n" +
                        "        });\n" +
                        "    });\n" +
                        "</script>";
            }
            return "Transaction already exists and cannot be created again";
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
                ) +
                "<br/><br/>Computational time: " + ("todo ") + "ms";
    }

    @GetMapping(value = "/redeem")
    public String redeemForm(@RequestParam Optional<String> sign, @RequestParam Optional<String> txnid, @RequestParam Optional<String> location) {
        return "Redeem token<br/><br/>" +
                "<form action=\"redeem\" method=\"post\">" +
                "Sign: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.orElse("").trim() + "\" /><br/><br/>" +
                "txnid: <input style=\"width:90%\" type=\"text\" name=\"txnid\" value=\"" + txnid.orElse("").trim() + "\" /><br/><br/>" +
                "location: <input style=\"width:90%\" type=\"text\" name=\"location\" value=\"" + location.orElse("").trim() + "\" /><br/><br/>" +
                "<input type=\"submit\" value=\"Redeem token at location\"/>" +
                "</form>";
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
                return "<div id=\""+responseDivId+"\">Transaction to redeem " + txnid.trim() + " has been submitted for processing</div>" +
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
                ) +
                "<br/><br/>Computational time: " + ("TODO ") + "ms";
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
                    "sign", block.sign,
                    "data", block.data,
                    "pubKey", block.publicKey,
                    "mineTime", String.valueOf(System.currentTimeMillis() - start) + "ms").jsonString();
        }
        return "Invalid signature for Dev";
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

    @PostMapping(value = "/verifyApi")
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
        return "Signature Verification utility using ECDSA" +
                "<form action=\"/verify\" method=\"post\">" +
                "Signature: <input style=\"width:90%\" type=\"text\" name=\"sign\" value=\"" + sign.orElse("").trim() + "\"/><br/><br/>" +
                "BlockData: <input style=\"width:90%\" type=\"text\" name=\"data\" value=\"" + data.orElse("").trim() + "\"/><br/><br/>" +
                "pubKey: <input style=\"width:90%\" type=\"text\" name=\"pubKey\" value=\"" + pubKey.orElse("").trim() + "\"/><br/><br/>" +
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

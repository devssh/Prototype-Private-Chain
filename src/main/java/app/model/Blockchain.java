package app.model;

import app.service.CryptoService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;

import java.util.concurrent.ConcurrentHashMap;

import static app.model.Txn.*;
import static app.service.MailSenderService.sendMail;
import static app.service.PassKitService.createPass;
import static app.utils.BlockManager.GetFlattenedTxns;
import static app.utils.Console.Println;

public class Blockchain {
    public static final ConcurrentHashMap<HashString, Txn> utxoSet = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<HashString, Txn> completedTxns = new ConcurrentHashMap<>();
    public static boolean processing = false;
    public static final CryptoService cryptoService = new CryptoService();

    public Blockchain() throws Exception {
        Println("Blockchain initialization error");
    }

    public static void InitCompletedTxns() {
        GetFlattenedTxns().forEach(txn -> completedTxns.put(txn.equalityString(), txn));
    }

    public static void MineBlock() throws Exception {
        if (!processing) {
            ConcurrentHashMap<HashString, Txn> utxoSubSet = new ConcurrentHashMap<>(utxoSet);
            int n = utxoSubSet.size();
            if (n > 0) {
                processing = true;
                ConcurrentHashMap<HashString, Txn> txnsInBlock = new ConcurrentHashMap<>();
                txnsInBlock.putAll(utxoSubSet);
                cryptoService.mineBlock("Dev", txnsInBlock.values().toArray(new Txn[n]));
                utxoSet.keySet().removeAll(txnsInBlock.keySet());
                completedTxns.putAll(txnsInBlock);
                //TODO: extract this from here, send the qr code without saving to file
                try {
                    for (HashString hashString : txnsInBlock.keySet()) {
                        Txn minedTxn = txnsInBlock.get(hashString);
                        if (minedTxn.varMan.get(TYPE).equals(CREATE)) {
                            //TODO: Assign unique number to serial number
                            InputStreamSource pkpass = createPass(minedTxn.varMan.get(TXNID));
                            sendMail(minedTxn.varMan.get(EMAIL), "Coupon Testing Server - News America Marketing",
                                    "Hello, you have received a discount coupon from Yuval. ", pkpass);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                processing = false;
            }
            if (utxoSet.size() > 0) {
                MineBlock();
            }

        }
    }

    public static boolean ADD_TXN_TO_UTXOSET(Txn txn) {
        for (HashString hashString : completedTxns.keySet()) {
            if (txn.equalityString().equals(hashString)) {
                return false;
            }
        }
        utxoSet.put(txn.equalityString(), txn);
        return true;
    }

    public static Txn GetCompletedTxn(String txnid, String type) {
        return completedTxns.get(EqualityString(txnid, type));
    }
}

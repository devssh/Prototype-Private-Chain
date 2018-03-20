package app.model;

import java.time.LocalDateTime;

import static app.service.KeyzManager.GetKey;
import static app.service.SignService.SignWith;

public class TxnDao {
    public final String txnid;
    public final String email;
    public final String location;
    private final String createdAt;

    public TxnDao(String txnid, String email, String location) {
        this.txnid = txnid;
        this.email = email;
        this.location = location;
        this.createdAt = LocalDateTime.now().toString();
    }

    public Txn getTxn(String signWith) throws Exception {
        return new Txn(SignWith(signWith, SerializeForHashing(this)),
                GetKey(signWith).publicKey, txnid, email, location, createdAt);
    }


    public static String SerializeForHashing(TxnDao txnDao) {
        return txnDao.txnid + txnDao.email + txnDao.location + txnDao.createdAt;
    }
}

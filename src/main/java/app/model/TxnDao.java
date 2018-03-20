package app.model;

import app.service.SignService;

import java.time.LocalDateTime;

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

    public Txn getTxn(String signWith, SignService signService) throws Exception {
        return new Txn(signService.signWith(signWith, SerializeForHashing(this)),
                signService.keyzManager.getKey(signWith).publicKey, txnid, email, location, createdAt);
    }


    public static String SerializeForHashing(TxnDao txnDao) {
        return txnDao.txnid + txnDao.email + txnDao.location + txnDao.createdAt;
    }
}

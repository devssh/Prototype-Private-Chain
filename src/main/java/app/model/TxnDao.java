package app.model;

import app.service.SignService;

import java.time.LocalDateTime;

public class TxnDao {
    final String txnid;
    final String email;
    final String location;
    final String createdAt;

    public TxnDao(String txnid, String email, String location) {
        this.txnid = txnid;
        this.email = email;
        this.location = location;
        this.createdAt = LocalDateTime.now().toString();
    }

    public Txn getTxn(String userName, SignService signService) throws Exception {
        return new Txn(signService.signWith(userName, SerializeForHashing(this)),
                signService.keyzManager.getKey(userName).publicKey, txnid, email, location, createdAt);
    }


    public static String SerializeForHashing(TxnDao txnDao) {
        return txnDao.txnid + txnDao.email + txnDao.location + txnDao.createdAt;
    }
}

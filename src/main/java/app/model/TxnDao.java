package app.model;

import static app.model.Txn.CREATE;
import static app.model.Txn.REDEEM;
import static app.service.CryptoService.IsCreatable;
import static app.service.CryptoService.IsRedeemable;
import static app.service.KeyzManager.GetKey;
import static app.service.SignService.SignWith;
import static app.utils.MiscUtils.GetDateTimeNow;

public class TxnDao {
    //TODO: Move to Txn class
    public final String txnid;
    public final String email;
    public final String location;
    private final String createdAt;

    public TxnDao(String txnid, String email, String location) {
        this.txnid = txnid;
        this.email = email;
        this.location = location;
        this.createdAt = GetDateTimeNow();
    }

    public Txn getTxn(String signWith, String type) throws Exception {
        if(type.equals(REDEEM) && !IsRedeemable(this.txnid)) {
            throw new Exception("Cannot redeem token which does not exist");
        }
        if(type.equals(CREATE) && !IsCreatable(this.txnid)) {
            throw new Exception("The coupon already exists");
        }
        return new Txn(SignWith(signWith, SerializeForSign(this)),
                GetKey(signWith).publicKey, txnid, email, location, createdAt, type);
    }


    public static String SerializeForSign(TxnDao txnDao) {
        return txnDao.txnid + txnDao.email + txnDao.location + txnDao.createdAt;
    }
}

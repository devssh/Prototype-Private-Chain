package app.model;

import org.junit.Test;

import static app.model.Blockchain.GetCompletedTxn;
import static app.model.Blockchain.InitCompletedTxns;
import static app.model.HashString.hashString;
import static app.model.Txn.CREATE;
import static org.junit.Assert.assertEquals;

public class BlockchainTest {

    public static final String txnid = "Foodie55";

    @Test
    public void shouldGetCompletedTxn() throws Exception {
        InitCompletedTxns();
        assertEquals(hashString(txnid + CREATE), GetCompletedTxn(txnid, CREATE).equalityString());
    }
}

package app.model;

import org.junit.Test;

import static app.model.Txn.CREATE;
import static app.service.KeyzManager.GetKey;
import static org.junit.Assert.assertEquals;

public class BlockTest {
    @Test
    public void shouldBeAbleToDeserializeSerializedBlock() throws Exception {
        Block originalBlock = new Block(GetKey("Dev"), "x1", new TxnDao("abc", "xyz@gmail.com", "test").getTxn("Miner", CREATE));
        System.out.println(originalBlock.toString());

        Block deserializedBlock = Block.Deserialize(originalBlock.toString());

        assertEquals(originalBlock.toString(), deserializedBlock.toString());
    }
}

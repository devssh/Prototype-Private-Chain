package app.service;

import app.model.Block;
import app.model.Keyz;
import app.model.Txn;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static app.model.Block.PREV_HASH;
import static app.model.StringVar.*;
import static app.model.Txn.*;
import static app.model.VariableManager.DATA;
import static app.service.FileUtils.AppendBlocks;
import static app.service.FileUtils.ReadBlockchain;
import static app.service.KeyzManager.GetKey;
import static app.service.KeyzManager.Keys;
import static app.utils.Exceptions.*;

public class CryptoService {

    public String getBlockchain() throws Exception {
        List<Block> blocks = ReadBlockchain();

        return ArrayOfObjects(blocks.stream().map(Block::toString).collect(Collectors.toList()));
    }

    public String getBlock(String blockSign) throws Exception {
        List<Block> blocks = ReadBlockchain();

        return blocks.stream().filter(block -> block.sign.equals(blockSign)).map(Block::toString).collect(Collectors.toList()).get(0);
    }

    public String getBlockByTxn(String txnid, String type) throws Exception {
        List<String> blocksHavingTxn = ReadBlockchain().stream().filter(block -> Arrays.stream(block.txns).filter(txn ->
                        txn.varMan.get(TXNID).equals(txnid) && txn.varMan.get(TYPE).equals(type)
                ).collect(Collectors.toList()).size() > 0
        ).map(Block::toString).collect(Collectors.toList());

        if (blocksHavingTxn.size() == 1) {
            return blocksHavingTxn.get(0);
        }

        if(blocksHavingTxn.size()==0) {
            throw BLOCK_NOT_FOUND;
        }

        throw BAD_DATA;
    }

    public Block mineBlock(String signedBy, Txn... txns) throws Exception {
        List<Block> blocks = ReadBlockchain();
        String prevHash = blocks.get(blocks.size() - 1).sign;

        for (Txn txn : txns) {
            if (blocks.stream().filter(block1 ->
                    Arrays.stream(Txn.Deserialize(block1.varMan.get(DATA))).filter(txn1 ->
                            txn1.varMan.get(TXNID).equals(txn.varMan.get(TXNID)) && txn1.varMan.get("type").equals(REDEEM)
                    ).collect(Collectors.toList()).size() > 0
            ).collect(Collectors.toList()).size() > 0) {
                throw DOUBLE_SPEND_ATTEMPTED;
            }
        }

        System.out.println("Mining block");


        Block block = new Block(GetKey(signedBy), prevHash, txns);

        List<Block> newBlocks = ReadBlockchain();
        String newPrevHash = newBlocks.get(newBlocks.size() - 1).sign;

        System.out.println(newPrevHash);
        System.out.println(prevHash);
        System.out.println(block.varMan.get(PREV_HASH));
        if (!newPrevHash.equals(prevHash)) {
            // If orphan then repeat
            System.out.println("Block orphaned");
            return mineBlock(signedBy, txns);
        }

        AppendBlocks(block.toString());
        return block;


    }

    public String showUsers() {
        return SurroundWithBraces(JoinWithComma(
                Keys.stream().map(key -> SuperKeyValuePair(key.owner, KeyValuePair(PUBLIC_KEY, key.publicKey))).toArray(String[]::new)
        ));
    }


    public String verifyAllSignatures() throws Exception {
        List<Block> blocks = ReadBlockchain();

        Block genesis = blocks.get(0);
        boolean isValid = genesis.verify();

        for (int i = 1; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            isValid = isValid & block.verify();
        }

        return String.valueOf(isValid);
    }
    //        return blockManager.createGenesisBlock(keysDev, message, owner, aadhar);
    //    public String showGenesis(String message, String owner, String aadhar) throws Exception {
//

//    }

    public String generateKeyString() throws Exception {
        Keyz key = Keyz.generateKey();
        return "Public Key: " + key.publicKey +
                "\nPrivate Key: " + key.privateKey;
    }

    public String getStats() {
        return "Difficulty: 3" +
                "\nHashRate: 1GH/Sec" +
                "\nCost per transaction(block): $0.00038" +
                "\nBlock Time: 5 seconds on average";
    }

    public static boolean IsRedeemable(String txnid) throws Exception {
        List<Block> blocks = ReadBlockchain();
        return (blocks.stream().filter(block ->
                Arrays.stream(block.txns).filter(txn ->
                        txn.varMan.get(TXNID).equals(txnid)
                ).collect(Collectors.toList()).size() > 0
        ).collect(Collectors.toList()).size() > 0);
    }

    public static boolean IsCreatable(String txnid) throws Exception {
        List<Block> blocks = ReadBlockchain();
        return (blocks.stream().filter(block ->
                Arrays.stream(block.txns).filter(txn ->
                        txn.varMan.get(TXNID).equals(txnid)
                ).collect(Collectors.toList()).size() != 0
        ).collect(Collectors.toList()).size() == 0);
    }
}

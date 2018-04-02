package app.utils;

public class Exceptions {
    public static final Exception DOUBLE_SPEND_ATTEMPTED = new Exception("Double spend attempt detected and stopped");
    public static final Exception FAILED_TO_CREATE_TXN = new Exception("Failed to create transaction");
    public static final Exception BLOCK_NOT_FOUND = new Exception("Block not found");
    public static final Exception BAD_DATA = new Exception("Double spending has been found in corrupt data, ensure initialized correctly");
}

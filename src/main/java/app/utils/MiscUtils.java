package app.utils;

import java.time.LocalDateTime;

public class MiscUtils {
    public static String GetDateTimeNow() {
        return LocalDateTime.now().toString();
    }
}

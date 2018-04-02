package app.service;

import static app.utils.Properties.basicSign;

public class AuthService {
    public static boolean CheckValidSign(String sign) {
        return sign.equals(basicSign);
    }
}

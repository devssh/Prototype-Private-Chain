package app.service;

import static app.utils.HtmlUtils.RedirectButton;

public class HtmlService {
    public static String Header() {
        return RedirectButton("Create Redeemable Token", "create") +
                RedirectButton("Redeem Token", "redeem") +
                RedirectButton("See Authorized Miners", "authorized") +
                RedirectButton("See User Information", "users") + "<br/><br/>";
    }

    public static String HomeRedirectButton() {
        return RedirectButton("Home page", "/") + "<br/><br/>";
    }
}

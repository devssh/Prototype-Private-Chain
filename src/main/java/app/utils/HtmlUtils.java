package app.utils;

import app.model.Block;
import app.model.StringVar;
import app.model.Txn;

import java.util.Arrays;
import java.util.stream.Collectors;

import static app.model.Block.*;
import static app.model.StringVar.Join;
import static app.model.StringVar.JoinWith;
import static app.model.Txn.*;
import static app.model.Txn.PUBLIC_KEY;

public class HtmlUtils {
    public static String TableRows(Block block) {
        Txn[] txns = Txn.Deserialize(block.varMan.get("data"));
        String sign = new StringBuilder(block.sign).reverse().toString();
        return "<tr><td style=\"padding:20px\">" +
                "<table><tr>" +
                TD(Strong("Block Signature:")) + TD(RedirectAnchor(sign, "block/" + block.sign)) +
                "</tr>" +
                "<tr>" +
                TD(block.varMan.get(BLOCK_CREATED_AT)) +
                TD("Prev Sign: " + new StringBuilder(block.varMan.get(PREV_HASH)).reverse().toString()) +
                TD("Nonce: " + block.varMan.get(NONCE)) +
//                TD("Cost to Modify: " + block.varMan.get(DEPTH)) +
                "</tr>" +
                TD(Strong("TXNS")) +
                JoinWith("", Arrays.stream(txns).map(txn -> "<tr>" +
                        TD(txn.varMan.get(CREATED_AT)) +
                        TD("TXN Sign: " + txn.sign) +
                        TD(Strong(txn.varMan.get(TXNID)), 15, 10) +
                        TD("Type: " + txn.varMan.get(TYPE), 15, 10) +
                        "</tr>"
                ).collect(Collectors.toList())) +
                "</table></td></tr>";

    }

    public static String TD(String str) {
        return TD(str, 10, 20);
    }

    public static String TD(String str, int fontSize, int padding) {
        return "<td style=\"padding-right:" + padding + "; font-size:" + fontSize + "px\">" + str + "</td>";
    }

    public static String Strong(String str) {
        return "<strong>" + str + "</strong>";
    }

    public static String RedirectButton(String name, String url) {
        return "<a href=\"" + url + "\" ><input style=\"margin-right:20px; margin-bottom:20px\"type=\"button\" value=\"" + name + "\" /></a>";
    }

    public static String RedirectAnchor(String name, String url) {
        return "<a href=\"" + url + "\" >" + name + "</a>";
    }

    public static String Ajax(String url, String componentToDisplayResultId) {
        return "<script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js\"></script> \n" +
                "<script type=\"text/javascript\">\n" +
                "    $(document).ready(function () {\n" +
                "        $.ajax({\n" +
                "            type: 'GET',\n" +
                "            url: '" + url + "',\n" +
                "            dataType: \"text\",\n" +
                "            crossDomain: true,\n" +
                "            success: function (response) {\n" +
                "                $(\"#" + componentToDisplayResultId + "\").html(response);\n" +
                "            },\n" +
                "            error: function (request, status, error) {\n" +
                "                alert(error);\n" +
                "            }\n" +
                "        });\n" +
                "    });\n" +
                "</script>";
    }

    public static String Form(String url, String method, String buttonText, StringVar... stringVar) {
        String inputs = Join(Arrays.stream(stringVar).map(stringVar1 -> stringVar1.name + ": <input style=\"width:90%\" type=\"text\" name=\"" + stringVar1.name + "\" value=\"" + stringVar1.value + "\" /><br/><br/>").collect(Collectors.toList()));
        return "<form action=\"" + url + "\" method=\"" + method + "\">" +
                inputs +
                "<input type=\"submit\" value=\"" + buttonText + "\"/>" +
                "</form>";
    }

}

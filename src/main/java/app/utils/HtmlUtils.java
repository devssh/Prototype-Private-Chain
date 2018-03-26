package app.utils;

import app.model.Block;
import app.model.Txn;

import java.util.Arrays;
import java.util.stream.Collectors;

import static app.model.StringVar.JoinWith;

public class HtmlUtils {
    public static String Table(Block block) {
        Txn[] txns = Txn.Deserialize(block.varMan.get("data"));
        String sign = new StringBuilder(block.sign).reverse().toString();
        return "<table style=\"font-size:15px\">" +
                "<tr>" +
                TD(Strong("BlockSign")) +
                TD(RedirectAnchor(sign, "block/" + block.sign)) +
                "<table style=\"font-size:15px; padding:5px\" >" +
                TD(Strong("TXNS")) +
                JoinWith("", Arrays.stream(txns).map(txn -> "<tr>" +
                        TD(txn.varMan.get("createdAt"), 10) +
                        TD(Strong(txn.varMan.get("txnid"))) +
                        TD(txn.varMan.get("type")) +
                        "</tr>"
                ).collect(Collectors.toList())) +
                "</table>" +

                "</tr>" +
                "</table>";

    }

    public static String TD(String str) {
        return TD(str, 15);
    }

    public static String TD(String str, int fontSize) {
        return "<td style=\"padding-right:20px; font-size:" + fontSize + "px\">" + str + "</td>";
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
                "                $(\"#"+componentToDisplayResultId+"\").html(response);\n" +
                "            },\n" +
                "            error: function (request, status, error) {\n" +
                "                alert(error);\n" +
                "            }\n" +
                "        });\n" +
                "    });\n" +
                "</script>";
    }
}

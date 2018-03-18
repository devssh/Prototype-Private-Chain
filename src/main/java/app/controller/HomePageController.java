package app.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomePageController {

    @GetMapping(value = "/", produces = "application/json")
    public String index() {
        return "Greetings! Health Check success. \n" +
        "Checkout /coupons-explorer for read only initBlockchain data\n"+
        "Checkout /authorized for public keys of all authoritiesManager\n"+
        "Checkout /create?Sign=ASK_FOR_TOKEN&message=Coupon208KX&owner=Dev&aadhar=122 to create block signed by Authority dev simulated on his server\n"+
        "Checkout /Verify-form to validate signature";
    }


}
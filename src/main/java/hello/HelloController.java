package hello;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {

    @GetMapping(value = "/", produces = "application/json")
    public String index() {
        return "Greetings! Health Check success. \n" +
        "Checkout /coupons for read only blockchain data\n"+
        "Checkout /authorized for public keys of all authorities\n"+
        "Checkout /create?message=Coupon208KX&owner=Dev&aadhar=122 to create block signed by Authority dev simulated on his server\n"+
        "Checkout /verify-form to validate signature";
    }

    @RequestMapping("/aa")
    public String index1() {
        return "This is from /aa";
    }

}
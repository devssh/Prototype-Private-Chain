package app.controller;
import app.service.MailSenderService;
import app.service.PassKitService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
public class HomePageController {

    @GetMapping(value = "/instructions", produces = "application/json")
    public String index() {
        return "Greetings! Health Check success. \n" +
        "Checkout /blockExplorer-explorer for read only initBlockchain data\n"+
        "Checkout /authorized for public keys of all authoritiesManager\n"+
        "Checkout /create?Sign=ASK_FOR_TOKEN&message=Coupon208KX&owner=Dev&aadhar=122 to create block signed by Authority dev simulated on his server\n"+
        "Checkout /Verify-form to validate signature";
    }

    @RequestMapping(value = "/testMail/{email:.+}", method = RequestMethod.GET)
    public String testMail(@PathVariable("email") String email){
        System.out.println(email);
        try {
            PassKitService.createPass("007","Test coupon");
            FileSystemResource file = new FileSystemResource(new File("discountCoupon.pkpass"));
            MailSenderService.sendMail(email,"NAM","Discount Coupon",file);
            return "Success, email was sent";
        }
       catch (Exception e){
          System.out.print(e.getLocalizedMessage());
       }
       return "Error occurred";
    }


}
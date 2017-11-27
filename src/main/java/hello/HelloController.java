package hello;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {

    @GetMapping(value = "/", produces = "application/json")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @RequestMapping("/aa")
    public String index1() {
        return "This is from /aa";
    }

}
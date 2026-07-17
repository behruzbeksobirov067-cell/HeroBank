import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @GetMapping
    public String registerPage() {
        return "register";
    }

    @PostMapping
    public String register(@RequestParam("q") String q) {
        return "redirect:/login";
    }
}
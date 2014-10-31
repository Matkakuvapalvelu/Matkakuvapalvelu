package wadp.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.User;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    @RequestMapping(method = RequestMethod.POST)
    public String createUser(@ModelAttribute User user) {

        return "index";
    }


    @RequestMapping(method = RequestMethod.GET)
    public String showSignUpPage() {

        return "signup";
    }

}

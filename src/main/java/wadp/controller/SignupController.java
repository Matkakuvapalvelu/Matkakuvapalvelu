package wadp.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.form.UserForm;
import javax.validation.Valid;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    @RequestMapping(method = RequestMethod.POST)
    public String createUser(@ModelAttribute("user") @Valid UserForm user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }


        // TODO: Pass username/password to UserService (todo...) that creates and saves new user
        return "redirect:index";
    }


    @RequestMapping(method = RequestMethod.GET)
    public String showSignUpPage(Model model) {
        model.addAttribute("user", new UserForm());
        return "signup";
    }

}

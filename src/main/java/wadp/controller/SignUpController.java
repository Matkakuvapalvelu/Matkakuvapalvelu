package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.form.UserForm;
import wadp.service.UserService;
import wadp.service.UsernameAlreadyTakenException;

import javax.validation.Valid;

@Controller
@RequestMapping("/register")
public class SignUpController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.POST)
    public String createUser(@ModelAttribute("user") @Valid UserForm user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // there is probably a cleaner way to handle this, feels kinda hacky
        try {
            userService.createUser(user.getUsername(), user.getPassword());
        } catch (UsernameAlreadyTakenException ex) {
            bindingResult.addError(new FieldError("user", "username", "Username already taken"));
            return "register";
        }

        return "redirect:index";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showSignUpPage(Model model) {
        model.addAttribute("user", new UserForm());
        return "register";
    }

}

package wadp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.service.UserService;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @RequestMapping(method= RequestMethod.GET)
    public String showProfilePage(Model model) {
        model.addAttribute("user", userService.getAuthenticatedUser());
        return "profile";
    }

}

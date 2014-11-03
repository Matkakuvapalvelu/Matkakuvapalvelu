package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.User;
import wadp.service.UserService;

// Probably should be removed later on. Right now just redirects any unhandled addresses to index
@Controller
@RequestMapping("/index")
public class DefaultController {
    @Autowired
    UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public String showIndex(Model model) {

        User authenticatedUser = userService.getAuthenticatedUser();

        if (authenticatedUser == null) {
            model.addAttribute("message", "No user authenticated");
        } else {
            model.addAttribute("message", "Authenticated user: " +
                    authenticatedUser.getUsername() + " With hashed password: " +
                    authenticatedUser.getPassword() + " and salt: " +
                    authenticatedUser.getSalt());
        }



        return "index";
    }
}

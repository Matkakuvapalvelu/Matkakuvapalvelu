package wadp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.service.NotificationService;
import wadp.service.UserService;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(method= RequestMethod.GET)
    public String showProfilePage(Model model) {
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("unreadnotifications",
                notificationService.getUnreadNotificationCountForUser(userService.getAuthenticatedUser()));
        return "profile";
    }

}

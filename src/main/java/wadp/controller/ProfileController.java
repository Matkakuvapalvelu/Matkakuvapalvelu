package wadp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.User;
import wadp.service.FriendshipService;
import wadp.service.NotificationService;
import wadp.service.UserService;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(method = RequestMethod.GET)
    public String showProfilePage(Model model) {
        addUserDetails(userService.getAuthenticatedUser(), model, true);
        return "profile";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String showProfilePage(@PathVariable Long id, Model model) {
        addUserDetails(userService.getUser(id), model, userService.getAuthenticatedUser().getId() == id);
        return "profile";
    }

    private void addUserDetails(User user, Model model, boolean isLoggedInUser) {
        model.addAttribute("user", user);

        if (isLoggedInUser) {
            model.addAttribute("notifications",
                    notificationService.getUnreadNotificationCountForUser(userService.getAuthenticatedUser()));
        } else {
            model.addAttribute("canrequestfriendship",
                    !friendshipService.areFriends(user, userService.getAuthenticatedUser()));
        }
    }
}

package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.service.FriendshipService;
import wadp.service.NotificationService;
import wadp.service.UserService;


@Controller
@RequestMapping("/friendship")
public class FriendshipController {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private NotificationService notificationService;


    @RequestMapping(value="/request/{id}", method= RequestMethod.POST)
    public String requestFriendship(@PathVariable Long id) {

        // friendship with self is kinda weird, let's not do that
        if (userService.getAuthenticatedUser().getId() == id) {
            return "redirect:/profile/" + id;
        }
        
        friendshipService.createNewFriendshipRequest(userService.getAuthenticatedUser(), userService.getUser(id));
        notificationService.createNewNotification(
                "Friendship request",
                "User " + userService.getAuthenticatedUser().getUsername() + " wants to be your friend!",
                userService.getAuthenticatedUser(),
                userService.getUser(id));


        return "redirect:/profile/" + id;
    }
}

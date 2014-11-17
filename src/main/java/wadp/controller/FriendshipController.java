package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.domain.User;
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

    @RequestMapping(method=RequestMethod.GET)
    public String showFriendsAndRequests(Model model) {
        model.addAttribute("friends", friendshipService.getFriends(userService.getAuthenticatedUser()));
        model.addAttribute("friendRequests", friendshipService.getFriendshipRequests(userService.getAuthenticatedUser()));

        return "friends";
    }

    @RequestMapping(value="/request/accept/{id}", method=RequestMethod.POST)
    public String acceptFriendship(@PathVariable Long id) {

        friendshipService.acceptRequest(id);
        return "redirect:/friends";
    }

    @RequestMapping(value="/request/reject/{id}", method=RequestMethod.POST)
    public String rejectFriendship(@PathVariable Long id) {
        friendshipService.rejectRequest(id);
        return "redirect:/friends";
    }

    @RequestMapping(value="/unfriend/{id}", method=RequestMethod.DELETE)
    public String removeFriendship(@PathVariable Long id) {
        friendshipService.unfriend(id, userService.getAuthenticatedUser());
        return "redirect:/friends";
    }

    @RequestMapping(value="/request/{id}", method= RequestMethod.POST)
    public String requestFriendship(@PathVariable Long id) {

        // friendship with self is kinda weird, let's not do that
        if (userService.getAuthenticatedUser().getId() == id) {
            return "redirect:/profile/" + id;
        }

        friendshipService.createNewFriendshipRequest(userService.getAuthenticatedUser(), userService.getUser(id));

        return "redirect:/profile/" + id;
    }

}

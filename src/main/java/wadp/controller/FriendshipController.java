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

    @RequestMapping(value="/request/accept/{requestId}", method=RequestMethod.POST)
    public String acceptFriendship(@PathVariable Long requestId) {

        friendshipService.acceptRequest(requestId);
        return "redirect:/friendship";
    }

    @RequestMapping(value="/request/reject/{requestId}", method=RequestMethod.POST)
    public String rejectFriendship(@PathVariable Long requestId) {
        friendshipService.rejectRequest(requestId);
        return "redirect:/friendship";
    }

    @RequestMapping(value="/unfriend/{friendID}", method=RequestMethod.DELETE)
    public String removeFriendship(@PathVariable Long friendID) {

        friendshipService.unfriend(userService.getAuthenticatedUser(), userService.getUser(friendID));
        return "redirect:/friendship";
    }

    @RequestMapping(value="/request/{friendId}", method= RequestMethod.POST)
    public String requestFriendship(@PathVariable Long friendId) {

        // friendship with self is kinda weird, let's not do that
        if (userService.getAuthenticatedUser().getId() == friendId) {
            return "redirect:/profile/" + friendId;
        }

        friendshipService.createNewFriendshipRequest(userService.getAuthenticatedUser(), userService.getUser(friendId));

        return "redirect:/profile/" + friendId;
    }

}

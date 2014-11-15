package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.service.NotificationService;
import wadp.service.UserService;

import javax.transaction.Transactional;

@Controller
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;


    @RequestMapping(method=RequestMethod.GET)
    public String showNotificationList(Model model) {
        model.addAttribute("notifications", userService.getAuthenticatedUser().getReceivedNotifications());
        return "notifications";
    }

    @RequestMapping(value="{id}", method=RequestMethod.GET)
    @Transactional
    public String showNotification(@PathVariable Long id, Model model) {
        Notification notification = notificationService.getNotification(id);
        notification.setRead(true);

        model.addAttribute("notification", notification);
        return "notification";
    }

    @RequestMapping(value="{id}", method=RequestMethod.DELETE)
    public String deleteReceivedNotification(@PathVariable Long id) {

        Notification notification = notificationService.getNotification(id);
        if (notification != null && notification.getReceiver().getUsername().equals(
                userService.getAuthenticatedUser().getUsername())) {
            notificationService.deleteNotification(notification);
        }
        return "redirect:/notification";
    }


    // TEST CODE BELOW ---- CAN BE REMOVED (should be removed before final deadline!)

    @RequestMapping(value="/debug", method= RequestMethod.GET)
    public String showDebugNotifcationPage() {
        return "debugNotification";
    }

    @RequestMapping(value="/debug", method=RequestMethod.POST)
    public String addNewDebugNotificationToUser(@RequestParam("notificationText") String text) {

        User user = userService.getAuthenticatedUser();
        notificationService.createNewNotification("Test notification", text, user, user);
        return "redirect:/notification/debug";
    }
}

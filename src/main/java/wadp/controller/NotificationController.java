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
        model.addAttribute("notifications", notificationService.getNotifications(userService.getAuthenticatedUser()));
        return "notifications";
    }

    @RequestMapping(value="{id}", method=RequestMethod.GET)
    @Transactional
    public String showNotification(@PathVariable Long id, Model model) {
        Notification notification = notificationService.getNotification(id);

        if (notification == null
                || userService.getAuthenticatedUser() == null
                || !notification.getReceiver().getUsername().equals(userService.getAuthenticatedUser().getUsername())) {

            model.addAttribute("error", "No such notification exists");

            // view right now is not checking for null correctly, so add some empty data for it
            User dummyUser = new User();
             dummyUser.setUsername("");

            notification  = new Notification();
            notification.setSender(dummyUser);
            notification.setReceiver(dummyUser);
            notification.setNotificationText("");
            notification.setNotificationReason("");

            model.addAttribute("notification", notification);
            return "notification";
        }

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
}

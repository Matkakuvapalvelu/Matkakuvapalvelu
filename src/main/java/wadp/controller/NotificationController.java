package wadp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import wadp.service.NotificationService;

@Controller
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // TEST CODE ---- CAN BE REMOVED (should be removed before final deadline!)
    @RequestMapping(value="/debug", method= RequestMethod.GET)
    public String showDebugNotifcationPage() {
        return "debugNotification";
    }

    @RequestMapping(value="/debug", method=RequestMethod.POST)
    public String addNewDebugNotificationToUser(String text) {

        return "redirect:/notification/debug";
    }
}

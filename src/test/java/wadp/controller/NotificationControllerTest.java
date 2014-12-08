package wadp.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.service.NotificationService;
import wadp.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationControllerTest {
    private final String URI = "/notification";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    private MockMvcTesting mockMvcTesting;

    private User loggedInUser;
    private User other;

    @Before
    public void setUp() {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);

        loggedInUser = userService.createUser("loginuser", "loginuser");
        other = userService.createUser("other", "other");

        notificationService.createNewNotification("notification", "text", other, loggedInUser);
        notificationService.createNewNotification("notification2", "text2", other, loggedInUser);

        notificationService.createNewNotification("notification3", "text3", loggedInUser, other);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

    }

    @Test
    public void notificationsAreAddedToModelWhenRequestingListOfAllNotifications() throws Exception {
        MvcResult res = mockMvcTesting.makeGet(URI, "notifications", "notifications");
        List<Notification> notifications = (List<Notification>)res.getModelAndView().getModel().get("notifications");
        assertEquals(2, notifications.size());

        assertEquals(1,
                notifications
                        .stream()
                        .filter(n -> n.getNotificationText().equals("text"))
                        .collect(Collectors.toList())
                        .size());
        assertEquals(1,
                notifications
                        .stream()
                        .filter(n -> n.getNotificationText().equals("text2"))
                        .collect(Collectors.toList())
                        .size());

    }

    @Test
    public void requestingSingleNotificationReturnsCorrectNotificationThatIsSetToRead() throws Exception {
        Notification notification = notificationService.getNotifications(loggedInUser).get(0);

        MvcResult res = mockMvcTesting.makeGet(URI + "/" + notification.getId(), "notification", "notification");
        Notification modelNotification = (Notification)res.getModelAndView().getModel().get("notification");

        assertEquals(notification.getNotificationReason(), modelNotification.getNotificationReason());
        assertEquals(notification.getNotificationText(), modelNotification.getNotificationText());

        assertTrue(modelNotification.getIsRead());
    }

    @Test
    public void requestingNonExistentNotificationAddsErrorAndEmptyNotificationToModel() throws Exception {
        MvcResult res = mockMvcTesting.makeGet(URI + "/23423", "notification", "notification");
        assertNotNull(res.getModelAndView().getModel().get("error"));
        Notification modelNotification = (Notification) res.getModelAndView().getModel().get("notification");
        assertEquals("", modelNotification.getNotificationText());
        assertEquals("", modelNotification.getNotificationReason());
        assertEquals("", modelNotification.getSender().getUsername());
        assertEquals("", modelNotification.getReceiver().getUsername());
    }


    @Test
    public void requestingOtherPeopleNotificationsAddsErrorAndEmptyNotificationToModel() throws Exception {
        Long id = notificationService.getNotifications(other).get(0).getId();
        MvcResult res = mockMvcTesting.makeGet(URI + "/" + id, "notification", "notification");
        assertNotNull(res.getModelAndView().getModel().get("error"));
        Notification modelNotification = (Notification) res.getModelAndView().getModel().get("notification");
        assertEquals("", modelNotification.getNotificationText());
        assertEquals("", modelNotification.getNotificationReason());
        assertEquals("", modelNotification.getSender().getUsername());
        assertEquals("", modelNotification.getReceiver().getUsername());
    }

    @Test
         public void canDeleteOwnNotifications() throws Exception {
        Long id = notificationService.getNotifications(loggedInUser).get(0).getId();
        mockMvcTesting.makeDelete(URI + "/" + id, "/notification");
        assertNull(notificationService.getNotification(id));
    }

    @Test
    public void cannotDeleteOtherPeoplesNotifications() throws Exception {
        Long id = notificationService.getNotifications(other).get(0).getId();
        mockMvcTesting.makeDelete(URI + "/" + id, "/notification");
        Notification notification = notificationService.getNotification(id);
        assertNotNull(notification);
        assertEquals("notification3", notification.getNotificationReason());
        assertEquals("text3", notification.getNotificationText());

    }
}

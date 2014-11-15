package wadp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.Image;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.repository.ImageRepository;
import wadp.repository.NotificationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    private User sender;
    private User receiver;

    @Before
    public void setUp() {
        sender = userService.createUser("sender", "sender");
        receiver = userService.createUser("receiver", "receiver");
    }


    @Test
    public void newNotificationIsSaved() {
        assertNotNull(notificationRepository.findOne(notificationService.createNewNotification("reason", "text", sender, receiver).getId()));
    }

    @Test
    public void newNotificationHasAttributesSetCorrectly() {
        Notification notification = notificationService.createNewNotification("reason", "text", sender, receiver);

        assertFalse(notification.isRead());
        assertEquals(sender, notification.getSender());
        assertEquals(receiver, notification.getReceiver());
        assertEquals("reason", notification.getNotificationReason());
        assertEquals("text", notification.getNotificationText());
    }

    @Test
    public void getNotificationReturnsNullOnInvalidId() {
        assertNull(notificationService.getNotification(99999l));
    }

    @Test
    public void getNotificationReturnsCorrectNotification() {
        Notification notification = notificationService.createNewNotification("reason", "text", sender, receiver);
        assertEquals(notification, notificationService.getNotification(notification.getId()));
    }



    @Test
    public void canDeleteNotifications() {
        Notification notification = notificationService.createNewNotification("reason", "text", sender, receiver);
        Long id = notification.getId();
        notificationService.deleteNotification(notification);
        assertNull(notificationRepository.findOne(id));
    }



}

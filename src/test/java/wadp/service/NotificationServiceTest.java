package wadp.service;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.repository.NotificationRepository;

import javax.transaction.Transactional;

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

        assertFalse(notification.getIsRead());
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

    @Test
    public void unreadNotificationCountIsZeroWhenNoNotifications() {
        assertEquals(0l, notificationService.getUnreadNotificationCountForUser(receiver));
    }

    @Test
    public void unreadNotificationCountIsCorrectAfterAddingNotifications() {
        notificationService.createNewNotification("reason", "text", sender, receiver);
        notificationService.createNewNotification("reason", "text", sender, receiver);
        notificationService.createNewNotification("reason", "text", sender, receiver);

        assertEquals(3l, notificationService.getUnreadNotificationCountForUser(receiver));
    }

    @Test
    @Transactional
    public void unreadNotificationCountIsCorrectAfterReadingNotifications() {
        Notification notification = notificationService.createNewNotification("reason", "text", sender, receiver);
        notification.setRead(true);

        notificationService.createNewNotification("reason", "text", sender, receiver);

        notification = notificationService.createNewNotification("reason", "text", sender, receiver);
        notification.setRead(true);

        assertEquals(1l, notificationService.getUnreadNotificationCountForUser(receiver));
    }

}

package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.repository.NotificationRepository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service that handles notifications
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Creates new notification that is sent to receiver
     *
     * @param reason Reason for notification. Shown as a title
     * @param text Notification body text
     * @param sender Sending user
     * @param receiver Receiving user
     * @return Instance of notification after it has been saved to database
     */
    @Transactional
    public Notification createNewNotification(String reason, String text, User sender, User receiver) {

        Notification notification = new Notification();

        notification.setNotificationReason(reason);
        notification.setNotificationText(text);
        notification.setSender(sender);
        notification.setReceiver(receiver);
        return notificationRepository.save(notification);
    }


    /**
     * Gets notification with given id
     * @param id Notification id
     * @return Notification with the id or null if no notification with id exists
     */
    public Notification getNotification(Long id) {
        return notificationRepository.findOne(id);
    }

    /**
     * Deletes the notification
     * @param notification notification to be deleted
     */
    public void deleteNotification(Notification notification) {
        notificationRepository.delete(notification);
    }

    /**
     * Returns unread notification count for the user
     * @param user User whose unread notification count will bew returned
     * @return Number of unread notifications
     */
    public long getUnreadNotificationCountForUser(User user) {
        return notificationRepository.getUnreadNotificationCountForUser(user);
    }

    /**
     * Gets all the notifications (read and unread) of the user
     * @param receiver User whose notifications will be returned
     * @return List of all notifications
     */
    public List<Notification> getNotifications(User receiver) {
        return notificationRepository.findNotificationsByReceiver(receiver);
    }

    public void updateNotification(Notification n) {
        notificationRepository.save(n);
    }
}

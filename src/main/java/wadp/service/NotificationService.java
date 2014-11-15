package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.repository.NotificationRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;


    @Transactional
    public Notification createNewNotification(String reason, String text, User sender, User receiver) {

        Notification notification = new Notification();

        notification.setNotificationReason(reason);
        notification.setNotificationText(text);
        notification.setSender(sender);
        notification.setReceiver(receiver);

        sender.getSentNotifications().add(notification);
        receiver.getReceivedNotifications().add(notification);
        return notificationRepository.save(notification);
    }


    public Notification getNotification(Long id) {
        return notificationRepository.findOne(id);
    }

    public void deleteNotification(Notification notification) {
        notificationRepository.delete(notification);
    }

    public long getUnreadNotificationCountForUser(User user) {
        return notificationRepository.getUnreadNotificationCountForUser(user);
    }
}

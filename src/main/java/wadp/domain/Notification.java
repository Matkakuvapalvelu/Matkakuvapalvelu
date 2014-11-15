package wadp.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Notification extends AbstractPersistable<Long> {

    @ManyToOne
    private User receiver;

    @ManyToOne
    private User sender;

    private String notificationText;

    private String notificationReason;

    private boolean isRead;


    public Notification() {
        isRead = false;
    }


    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }

    public String getNotificationReason() {
        return notificationReason;
    }

    public void setNotificationReason(String notificationReason) {
        this.notificationReason = notificationReason;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}

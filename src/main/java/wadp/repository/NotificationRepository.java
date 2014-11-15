package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wadp.domain.Notification;
import wadp.domain.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver=?1 AND n.isRead=false")
    Long getUnreadNotificationCountForUser(User receiver);
}

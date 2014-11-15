package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}

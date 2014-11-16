package wadp.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import sun.security.util.PendingException;

import javax.persistence.Entity;

@Entity
public class Friendship extends AbstractPersistable<Long> {


    public enum Status {
        PENDING,
        ACCEPTED;
    }

    private User sourceUser;
    private User targetUser;
    private Status status;

}

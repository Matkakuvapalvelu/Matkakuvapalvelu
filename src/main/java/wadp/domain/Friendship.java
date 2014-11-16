package wadp.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import sun.security.util.PendingException;

import javax.persistence.*;

@Entity
public class Friendship extends AbstractPersistable<Long> {


    public enum Status {
        PENDING,
        ACCEPTED;
    }


    @ManyToOne
    private User sourceUser;

    @ManyToOne
    private User targetUser;

    @Enumerated(EnumType.STRING)
    private Status status;

    public User getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}

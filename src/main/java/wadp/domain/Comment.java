package wadp.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * Comment domain class. Contain comment text and reference to the user who
 * posted it Has a visibility boolean for moderation.
 *
 */
@Entity
public class Comment extends AbstractPersistable<Long> {

    private String commentText;

    private boolean visible;

    @ManyToOne
    private User poster;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    public Comment() {
        this.visible = true;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public User getUser() {
        return poster;
    }

    public void setUser(User user) {
        this.poster = user;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

}

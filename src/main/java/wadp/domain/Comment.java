package wadp.domain;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import org.springframework.data.jpa.domain.AbstractPersistable;
/**
 * Comment domain class. Contain comment text and reference to the user who posted it
 * Has a visibility boolean for moderation.
 * 
 */
@Entity
public class Comment extends AbstractPersistable<Long> {
    
    private String commentText;

    
    private boolean visible;

    @OneToMany
    private User user;

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
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    

}

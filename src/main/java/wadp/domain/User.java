package wadp.domain;

import java.util.List;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * User domain class. Contains any information tied to single user account
 */
@Entity
@Table(name = "USER_TABLE")
public class User extends AbstractPersistable<Long> {

    @Column(unique = true)
    private String username;
    private String password;

    private String userRole;

    @OneToMany
    private List<Comment> comments;

    public User() {
        userRole = "USER";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean passwordEquals(String plaintextPassword) {
        return BCrypt.checkpw(plaintextPassword, password);
    }
}

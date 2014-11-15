package wadp.domain;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Trip> trips;

    @OneToMany(mappedBy="receiver")
    private List<Notification> receivedNotifications; // any notifications received (private messages, friendship requests etc)

    @OneToMany(mappedBy="sender")
    private List<Notification> sentNotifications; // basically sent private messages
    
    public User() {
        trips = new ArrayList<>();

        receivedNotifications = new ArrayList<>();
        sentNotifications = new ArrayList<>();

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

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public boolean passwordEquals(String plaintextPassword) {
        return BCrypt.checkpw(plaintextPassword, password);
    }

    public List<Notification> getSentNotifications() {
        return sentNotifications;
    }

    public void setSentNotifications(List<Notification> sentNotifications) {
        this.sentNotifications = sentNotifications;
    }

    public List<Notification> getReceivedNotifications() {
        return receivedNotifications;
    }

    public void setReceivedNotifications(List<Notification> receivedNotifications) {
        this.receivedNotifications = receivedNotifications;
    }
}

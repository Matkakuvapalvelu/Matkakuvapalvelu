package wadp.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;

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
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date signupDate;

    @OneToMany
    private List<Comment> comments;

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    private List<Trip> trips;

    private Long profilePicId;

    public User() {
        trips = new ArrayList<>();
        comments = new ArrayList<>();
        userRole = "USER";
        signupDate = new Date();
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

    public Long getProfilePicId() {
        return profilePicId;
    }

    public void setProfilePicId(Long profilePicId) {
        this.profilePicId = profilePicId;
    }
    
    public Date getSignupDate() {
        return signupDate;
    }

    public void setSignupDate(Date signupDate) {
        this.signupDate = signupDate;
    }
}

package wadp.domain;


import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class User extends AbstractPersistable<Long> {

    @Column(unique = true)
    private String username;
    private String password;

    private String userRole;


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


    public boolean passwordEquals(String plaintextPassword) {
        return BCrypt.checkpw(plaintextPassword, password);
    }
}

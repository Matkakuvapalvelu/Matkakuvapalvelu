package wadp.domain.form;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import wadp.validation.FieldMatch;

import javax.validation.constraints.NotNull;

/**
 * Form object for new user creation. Necessary fields are passed to UserService that will create User object that will
 * be saved to database
 */
@FieldMatch(first = "password", second = "confirmpassword", message = "The password fields must match")
public class UserForm {

    @NotNull(message="Username cannot be null")
    @NotBlank(message="Username cannot be empty")
    @Length(min=3, message="Username must contain at least 3 characters")
    private String username;

    @NotNull(message="Password cannot be null")
    @Length(min=8, message="Password must contain at least 8 characters")
    private String password;

    private String confirmpassword;

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
        this.password = password;
    }

    public String getConfirmpassword() {
        return confirmpassword;
    }

    public void setConfirmpassword(String confirmpassword) {
        this.confirmpassword = confirmpassword;
    }
}

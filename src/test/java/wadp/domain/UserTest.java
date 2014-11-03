package wadp.domain;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class UserTest {

    @Test
    public void passwordIsNotSavedAsPlainText() {
        User user = new User();
        user.setPassword("password");
        assertNotEquals("password", user.getPassword());
    }

    @Test
    public void passwordEqualityCheckReturnsTrueOnCorrectPassword() {
        User user = new User();
        user.setPassword("swordfish");
        assertTrue(user.passwordEquals("swordfish"));
    }

    @Test
    public void passwordEqualityReturnsFalseOnWrongPassword() {
        User user = new User();
        user.setPassword("swordfish");
        assertFalse(user.passwordEquals("banana"));
    }

}

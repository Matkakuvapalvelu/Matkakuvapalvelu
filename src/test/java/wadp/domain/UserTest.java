package wadp.domain;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

public class UserTest {

    @Test
    public void passwordIsNotSavedAsPlainText() {
        User user = new User();
        user.setPassword("password");
        assertNotEquals("password", user.getPassword());
    }

}

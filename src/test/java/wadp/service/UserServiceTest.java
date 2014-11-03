package wadp.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.User;
import wadp.repository.UserRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;


    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void newUserIsCreated() {
        service.createUser("Foo", "barbarbar");
        assertNotNull(userRepository.findByUsername("Foo"));
    }

    @Test(expected=UsernameAlreadyTakenException.class)
    public void creatingUserWithSameUsernamethrowsException() {
        service.createUser("Foo", "barbarbar");
        service.createUser("Foo", "asfsdfdasf");
    }

    @Test(expected=AuthenticationException.class)
    public void authenticateThrowsIfUserDoesNotExist() {
        service.authenticate("jamesbond", "007");
    }

    @Test(expected=AuthenticationException.class)
    public void authenticateThrowsIfPasswordIsIncorrect() {
        service.createUser("jamesbond", "007");
        service.authenticate("jamesbond", "42");
    }

    @Test
    public void authenticateReturnsUserWhenUsernameAndPasswordAreCorrect() {
        service.createUser("jamesbond", "007");
        User user = service.authenticate("jamesbond", "007");

        assertEquals("jamesbond", user.getUsername());
    }
}

package wadp.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.User;
import wadp.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    private User loggedInUser;

    @Before
    public void setUp() {

        loggedInUser = service.createUser("loginuser", "loginuser");
        SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

    }

    @After
    public void tearDown() {

    }

    @Test
    public void newUserIsCreated() {
        service.createUser("Foo", "barbarbar");
        assertNotNull(userRepository.findByUsername("Foo"));
    }


    @Test
    public void createUserReturnsReferenceToUser() {
        User user = service.createUser("foo", "jpdopdfkodkoda");
        assertNotNull(userRepository.findOne(user.getId()));
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

    @Test
    public void getAuthenticatedUserReturnsLoggedInUser() {
        User user = service.getAuthenticatedUser();
        assertEquals(loggedInUser.getUsername(), user.getUsername());
    }

    @Test
    public void serviceReturnsListOfAllUsersCorrectly() {
        service.createUser("Foo", "barbarbar");
        service.createUser("Foo2", "barbarbar");
        service.createUser("Foo3", "barbarbar");
        service.createUser("Foo4", "barbarbar");

        List<String> userNames = Arrays.asList("Foo", "Foo2", "Foo3", "Foo4", "loginuser");

        List<User> users = service.getUsers();

        assertEquals(5, users.size());
        for (User u : users) {
            assertTrue(userNames.contains(u.getUsername()));
            userNames = userNames.stream().filter(user -> !user.equals(u.getUsername()) ).collect(Collectors.toList());
        }

    }

}

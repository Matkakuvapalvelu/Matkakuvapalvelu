package wadp.service;

import org.apache.commons.io.IOUtils;
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
import wadp.domain.*;
import wadp.repository.UserRepository;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TripService tripService;

    @Autowired
    private UserRepository userRepository;

    private User loggedInUser;

    @Before
    public void setUp() {

        loggedInUser = userService.createUser("loginuser", "loginuser");
        SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

    }

    @After
    public void tearDown() {

    }

    @Test
    public void newUserIsCreated() {
        userService.createUser("Foo", "barbarbar");
        assertNotNull(userRepository.findByUsername("Foo"));
    }


    @Test
    public void createUserReturnsReferenceToUser() {
        User user = userService.createUser("foo", "jpdopdfkodkoda");
        assertNotNull(userRepository.findOne(user.getId()));
    }

    @Test(expected=UsernameAlreadyTakenException.class)
    public void creatingUserWithSameUsernameThrowsException() {
        userService.createUser("Foo", "barbarbar");
        userService.createUser("Foo", "asfsdfdasf");
    }

    @Test(expected=UsernameAlreadyTakenException.class)
    public void creatingUserWithSameUsernameAndBeginningAndTrailingWhitespaceThrowsException() {
        userService.createUser("Foo", "barbarbar");
        userService.createUser(" Foo     ", "asfsdfdasf");
    }

    @Test(expected=IllegalArgumentException.class)
    public void cannotCreateUserWithEmptyUsername() {
        userService.createUser("", "barbarbar");
    }

    @Test(expected=IllegalArgumentException.class)
    public void cannotCreateUserWithNullUsername() {
        userService.createUser(null, "barbarbar");
    }

    @Test(expected=AuthenticationException.class)
    public void authenticateThrowsIfUserDoesNotExist() {
        userService.authenticate("jamesbond", "007");
    }

    @Test(expected=AuthenticationException.class)
    public void authenticateThrowsIfPasswordIsIncorrect() {
        userService.createUser("jamesbond", "007");
        userService.authenticate("jamesbond", "42");
    }

    @Test
    public void authenticateReturnsUserWhenUsernameAndPasswordAreCorrect() {
        userService.createUser("jamesbond", "007");
        User user = userService.authenticate("jamesbond", "007");

        assertEquals("jamesbond", user.getUsername());
    }

    @Test
    public void getAuthenticatedUserReturnsLoggedInUser() {
        User user = userService.getAuthenticatedUser();
        assertEquals(loggedInUser.getUsername(), user.getUsername());
    }

    @Test
    public void serviceReturnsListOfAllUsersCorrectly() {
        userService.createUser("Foo", "barbarbar");
        userService.createUser("Foo2", "barbarbar");
        userService.createUser("Foo3", "barbarbar");
        userService.createUser("Foo4", "barbarbar");

        List<String> userNames = Arrays.asList("Foo", "Foo2", "Foo3", "Foo4", "loginuser");

        List<User> users = userService.getUsers();

        assertEquals(5, users.size());
        for (User u : users) {
            assertTrue(userNames.contains(u.getUsername()));
            userNames = userNames.stream().filter(user -> !user.equals(u.getUsername()) ).collect(Collectors.toList());
        }
    }

    @Test
    @Transactional
    public void listOfActiveUserRespectsSizeLimitation() {
        createActiveUserData();

        List<User> users = userService.getMostActiveUsers(3);
        assertEquals(3, users.size());

        users = userService.getMostActiveUsers(5);
        assertEquals(5, users.size());

        users = userService.getMostActiveUsers(0);
        assertEquals(0, users.size());
    }

    @Test
    @Transactional
    public void listOfActiveUsersIsOrdered() {
        createActiveUserData();

        List<User> users = userService.getMostActiveUsers(4);
        assertEquals(4, users.size());
        assertEquals("Most_Active", users.get(0).getUsername());
        assertEquals("Second_Most_Active", users.get(1).getUsername());
        assertEquals("Third_Most_Active", users.get(2).getUsername());
        assertEquals("Fourth_Most_Active", users.get(3).getUsername());
    }


    private void createActiveUserData() {
        User mostActive = userService.createUser("Most_Active", "password");
        addPostsAndComments(5, 5, mostActive);

        User secondMostActive = userService.createUser("Second_Most_Active", "password");
        addPostsAndComments(2, 5, secondMostActive);
        User thirdMostActive = userService.createUser("Third_Most_Active", "password");
        addPostsAndComments(6, 0, thirdMostActive);

        User fourthMostActiveMostActive = userService.createUser("Fourth_Most_Active", "password");
        addPostsAndComments(1, 4, fourthMostActiveMostActive);
        User fifthMostActiveMostActive = userService.createUser("Fifth_Most_Active", "password");
        addPostsAndComments(3, 1, fifthMostActiveMostActive);
    }

    // post count must be at least 1 due to how this function is set up
    private void addPostsAndComments(int postCount, int commentCount, User creator) {
        List<Post> posts = new ArrayList<>();
        List<Trip> trips = new ArrayList<>();
        try {
            File imageFile = new File("src/test/testimg.jpg");
            InputStream is = new FileInputStream(imageFile.getAbsoluteFile());

            byte[] data = IOUtils.toByteArray(is);
            is.close();

            for (int i = 0; i < postCount; ++i) {

                Image image = imageService.addImage("image/jpg", "image name", data);

                List<Trip> addTripList = new ArrayList<Trip>();
                if (i % 2 == 0) {
                    Trip trip = tripService.createTrip("Trip header", "Trip description", Trip.Visibility.PUBLIC, creator);
                    trips.add(trip);
                    addTripList.add(trip);
                }

                Post post = postService.createPost(image, "Image text", addTripList, creator);

                posts.add(post);
            }
        } catch (IOException ex) {
            assertTrue("Ioexception in test setup", false);
        }


        Random random = new Random();
        for (int i = 0; i < commentCount; ++i) {
            Comment comment = new Comment();
            comment.setCommentText("Comment text");
            comment.setUser(creator);

            if (trips.size() == 0 || random.nextBoolean()) {
                commentService.addCommentToPost(comment, posts.get(random.nextInt(posts.size())), creator);
            } else {
                commentService.addCommentToTrip(comment, trips.get(random.nextInt(trips.size())), creator);
            }
        }
    }
}

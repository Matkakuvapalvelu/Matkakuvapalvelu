package wadp.service;

import java.io.File;
import java.io.FileInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.repository.PostRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    private Image image;
    private User user;
    private Post post;

    @Before
    public void setUp() throws IOException {
        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        user = userService.createUser("adasdsdaads", "pisadjsods");

        image = imageService.addImage(new Image(), "image/", "foo", data);
        post = postService.createPost(image, "Hello!", new ArrayList<Trip>(), user);

        // tripService requires authenticated user for it to work
        User loggedInUser = userService.createUser("loginuser", "loginuser");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));
    }

    @Test
    public void creatingPostAddsPostToRepository() {
        assertNotNull(post);
        assertNotNull(postRepository.findOne(post.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void creationThrowsIfImageIsNull() {
        postService.createPost(null, "daaddas", new ArrayList<Trip>(), user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void creationThrowsIfTripListIsNull() {
        postService.createPost(new Image(), "daaddas", null, user);
    }

    @Test
    public void createdPostHasCorrectImageText() {
        assertEquals("Hello!", post.getImageText());
    }

    @Test
    public void createdPostStoresImageReference() {
        assertEquals(image, post.getImage());
    }

    @Test
    public void createdPostContainsUserReference() {
        assertEquals(user, post.getPoster());
    }

    @Test
    public void imagePostDateIsNowOrBefore() {
        assertNotNull(post.getPostDate());
        Date date = new Date();
        assertTrue(date.after(post.getPostDate()));
    }

    @Test
    public void getPostReturnsCorrectImage() {
        assertEquals(post, postService.getPost(post.getId()));
    }

    @Test
    public void getUserPostsReturnAllUserPosts() {
        User user2 = userService.createUser("dffdsfdfd", "pisadjsods");
        postService.createPost(image, "Hello2!", new ArrayList<Trip>(), user);
        postService.createPost(image, "Hello3!", new ArrayList<Trip>(), user);
        postService.createPost(image, "Hello4!", new ArrayList<Trip>(), user);
        postService.createPost(image, "Hi!", new ArrayList<Trip>(), user2);
        postService.createPost(image, "Hi2!", new ArrayList<Trip>(), user2);

        List<String> imageTexts = Arrays.asList("Hello!", "Hello2!", "Hello3!", "Hello4!");

        List<Post> posts = postService.getUserPosts(user);

        assertEquals(4, posts.size());
        // check that each posts contains correct poster and correct image texts
        for (Post p : posts) {
            assertEquals(user, p.getPoster());
            assertTrue(imageTexts.contains(p.getImageText()));
        }
    }

    @Test
    @Transactional
    public void postIsAddedToTripWhenCreatingNewPost() {

        Trip trip = tripService.createTrip("description", Trip.Visibility.PRIVATE, userService.getAuthenticatedUser());
        List<Trip> trips = Arrays.asList(trip);
        Post post = postService.createPost(new Image(), "Hello", trips, userService.getAuthenticatedUser());
        assertTrue(trip.getPosts().contains(post));
    }
}

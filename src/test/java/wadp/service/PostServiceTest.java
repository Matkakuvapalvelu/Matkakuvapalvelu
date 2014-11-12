package wadp.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.domain.User;
import wadp.repository.PostRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    private Image image;
    private User user;
    private Post post;

    @Before
    public void setUp() throws IOException {
        user = userService.createUser("adasdsdaads", "pisadjsods");
        image = imageService.addImage(new Image(), "image/", "foo", new byte[1]);
        post = postService.createPost(image, "Hello!", user);
    }

    @Test
    public void creatingPostAddsPostToRepository() {
        assertNotNull(post);
        assertNotNull(postRepository.findOne(post.getId()));
    }

    @Test(expected= IllegalArgumentException.class)
    public void creationThrowsIfImageIsNull() {
        postService.createPost(null, "daaddas", user);
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

    @Test public void getUserPostsReturnAllUserPosts() {
        User user2 = userService.createUser("dffdsfdfd", "pisadjsods");
        postService.createPost(image, "Hello2!", user);
        postService.createPost(image, "Hello3!", user);
        postService.createPost(image, "Hello4!", user);
        postService.createPost(image, "Hi!", user2);
        postService.createPost(image, "Hi2!", user2);

        List<String> imageTexts = Arrays.asList("Hello!", "Hello2!", "Hello3!", "Hello4!");

        List<Post> posts = postService.getUserPosts(user);

        assertEquals(4, posts.size());
        // check that each posts contains correct poster and correct image texts
        for (Post p : posts) {
            assertEquals(user, p.getPoster());
            assertTrue(imageTexts.contains(p.getImageText()));
        }
    }

}

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
import java.util.Date;

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

    @Test(expected= IllegalArgumentException.class)
    public void creationThrowsIfImageIsNull() {
        postService.createPost(null, "daaddas", user);
    }
}

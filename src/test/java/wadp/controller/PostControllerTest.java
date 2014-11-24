package wadp.controller;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import wadp.Application;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.service.ImageService;
import wadp.service.PostService;
import wadp.service.TripService;
import wadp.service.UserService;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static wadp.controller.utility.ControllerTestHelpers.makeGet;
import static wadp.controller.utility.ControllerTestHelpers.makePost;
import static wadp.controller.utility.ControllerTestHelpers.makePostWithFile;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PostControllerTest {

    private final String URI = "/posts";

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private TripService tripService;

    @Autowired
    private PostService postService;

    @Autowired
    ImageService imageService;

    private User loggedInUser;
    private User otherUser;
    private Post post;
    private Trip trip;
    private byte [] data;


    private MockMvc mockMvc;

    @Before
    public void setUp() throws IOException {


        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .addFilter(springSecurityFilter, "/*")
                .build();

        webAppContext.getServletContext().setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

        createTestPosts();
    }

    private void createTestPosts() throws IOException {
        loggedInUser = userService.createUser("loginuser", "loginuser");
        otherUser = userService.createUser("otheruser", "otheruser");

        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
       data = IOUtils.toByteArray(is);


        Image img = new Image();
        imageService.addImage(img, "image/", "img1", data);

        trip = tripService.createTrip("trip", Trip.Visibility.PUBLIC, loggedInUser);
        post = postService.createPost(img, "desc1", Arrays.asList(trip), loggedInUser);

        postService.createPost(img, "desc3", new ArrayList<>(), otherUser);
        postService.createPost(img, "desc4", new ArrayList<>(), otherUser);
    }

    @Test
    public void postsAreAddedToModelWhenRequestingPostsView() throws Exception {
        MvcResult result = makeGet(mockMvc, URI, "posts", "posts");

        List<Post> posts = (List<Post>)result.getModelAndView().getModel().get("posts");
        assertEquals(1, posts.size());
        assertEquals(post.getId(), posts.get(0).getId());
    }

    @Test
    public void newPostCreationAddsTripsToModel() throws Exception {
        MvcResult result = makeGet(mockMvc, URI + "/new", "newpost", "trips");
        List<Trip> trips = (List<Trip>)result.getModelAndView().getModel().get("trips");
        assertEquals(1, trips.size());
        assertEquals(trip.getId(), trips.get(0).getId());
    }

    @Test
    @Transactional
    public void canCreateNewPost() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text";

        parameters.put("image_text", imageText);
        parameters.put("trips", trip.getId().toString());
        makePostWithFile(mockMvc, URI, "/posts/[0-9]+", data, "image/jpg", parameters);

        List<Post> posts = postService.getUserPosts(loggedInUser);

        assertEquals(2, posts.size());

        posts =  posts.stream()
                .filter(p -> p.getImageText().equals(imageText))
                .collect(Collectors.toList());
        assertEquals(1, posts.size());

        assertEquals(imageText, posts.get(0).getImageText());
        assertEquals(trip.getId(), posts.get(0).getTrips().get(0).getId());
        assertNotNull(posts.get(0).getImage());
    }
}

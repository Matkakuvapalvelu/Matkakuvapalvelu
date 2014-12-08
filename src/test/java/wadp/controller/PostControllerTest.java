package wadp.controller;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.domain.*;
import wadp.service.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    private ImageService imageService;

    @Autowired
    private CommentService commentService;

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

        createTestPostsAndSetUpAuthenticatedUser();
    }

    private void createTestPostsAndSetUpAuthenticatedUser() throws IOException {
        loggedInUser = userService.createUser("loginuser", "loginuser");
        otherUser = userService.createUser("otheruser", "otheruser");

        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        data = IOUtils.toByteArray(is);


        Image img = imageService.addImage("image/", "img1", data);

        trip = tripService.createTrip("trip", Trip.Visibility.PUBLIC, loggedInUser);

        post = postService.createPost(img, "desc1", Arrays.asList(trip), loggedInUser);

        postService.createPost(img, "desc3", new ArrayList<>(), otherUser);
        postService.createPost(img, "desc4", new ArrayList<>(), otherUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));
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
        makePostWithFile(mockMvc, URI, "/posts/[0-9]+", status().is3xxRedirection(), data, "image/jpg", parameters);

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

    @Test
    @Transactional
    public void emptyDataFileAddsErrorToModelAndNoPostIsCreated() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text";

        parameters.put("image_text", imageText);
        parameters.put("trips", trip.getId().toString());
        MvcResult res = makePostWithFile(mockMvc, URI, "", status().is2xxSuccessful(), null, "image/jpg", parameters);
        String error = (String)res.getModelAndView().getModel().get("error");
        assertNotNull(error);

        assertEquals(1, postService.getUserPosts(loggedInUser).size());
    }

    @Test
    @Transactional
    public void invalidTypeAddsErrorToModelAndNoPostIsCreated() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text";

        parameters.put("image_text", imageText);
        parameters.put("trips", trip.getId().toString());
        MvcResult res = makePostWithFile(mockMvc, URI, "", status().is2xxSuccessful(), data, "video/avi", parameters);
        String error = (String)res.getModelAndView().getModel().get("error");
        assertNotNull(error);
        assertEquals(1, postService.getUserPosts(loggedInUser).size());
    }

    @Test
    @Transactional
    public void commentIsAddedCorrectlyToPost() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String commentText = "This is my comment.";

        parameters.put("commentText", commentText);

        Long id = postService.getUserPosts(loggedInUser).get(0).getId();
        makePost(mockMvc, URI + "/" + id + "/comment", "/posts/" + id, parameters);

        List<Comment> comments = postService.getPost(id).getComments();
        assertEquals(1, comments.size());
        assertEquals(commentText, comments.get(0).getCommentText());
    }

    @Test
    @Transactional
    public void showSinglePostAddsPostAndItsCommentsToModel() throws Exception {

        Post post = postService.getUserPosts(loggedInUser).get(0);

        String commentText = "commentText asdasdasdasdad";
        Comment comment = new Comment();
        comment.setCommentText(commentText);
        commentService.addCommentToPost(comment, post);

        MvcResult result = makeGet(mockMvc, URI + "/" + post.getId(), "post", "post", "comments");


        Post addedPost = (Post)result.getModelAndView().getModel().get("post");
        List<Comment> comments = (List<Comment>)result.getModelAndView().getModel().get("comments");

        assertEquals(post.getId(), addedPost.getId());
        assertEquals(post.getComments().size(), comments.size());
        assertEquals(post.getComments().get(0).getId(), comments.get(0).getId());
        assertEquals(post.getComments().get(0).getCommentText(), comments.get(0).getCommentText());

    }

}

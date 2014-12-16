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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.*;
import wadp.service.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private MockMvcTesting mockMvcTesting;

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
    private Trip loggedInUserTrip;

    private byte [] data;


    @Before
    public void setUp() throws IOException {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);
        createTestPostsAndSetUpAuthenticatedUser();
    }

    private void createTestPostsAndSetUpAuthenticatedUser() throws IOException {
        loggedInUser = userService.createUser("loginuser", "loginuser");
        otherUser = userService.createUser("otheruser", "otheruser");

        File imageFile = new File("src/test/testimg.jpg");
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        data = IOUtils.toByteArray(is);


        Image img = imageService.addImage("image/", "img1", data);

        loggedInUserTrip = tripService.createTrip("header", "loggedInUserTrip", Trip.Visibility.PUBLIC, loggedInUser);

        post = postService.createPost(img, "desc1", Arrays.asList(loggedInUserTrip), loggedInUser);

        postService.createPost(img, "desc3", new ArrayList<>(), otherUser);
        postService.createPost(img, "desc4", new ArrayList<>(), otherUser);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));
    }

    @Test
    public void postsAreAddedToModelWhenRequestingPostsView() throws Exception {
        MvcResult result = mockMvcTesting.makeGet(URI, "posts", "posts");

        List<Post> posts = (List<Post>)result.getModelAndView().getModel().get("posts");
        assertEquals(1, posts.size());
        assertEquals(post.getId(), posts.get(0).getId());
    }

    @Test
    @Transactional
    public void canCreateNewPost() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text with scändics änd öther stüff";

        parameters.put("image_text", imageText);
        parameters.put("trips", loggedInUserTrip.getId().toString());
        mockMvcTesting.makePostWithFile(URI, "/posts/[0-9]+", status().is3xxRedirection(), data, "image/jpg", parameters);

        List<Post> posts = postService.getUserPosts(loggedInUser);

        assertEquals(2, posts.size());

        posts =  posts.stream()
                .filter(p -> p.getImageText().equals(imageText))
                .collect(Collectors.toList());
        assertEquals(1, posts.size());

        assertEquals(imageText, posts.get(0).getImageText());
        assertEquals(loggedInUserTrip.getId(), posts.get(0).getTrips().get(0).getId());
        assertNotNull(posts.get(0).getImage());
    }

    @Test
    @Transactional
    public void emptyDataFileAddsErrorToModelAndNoPostIsCreated() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text";

        parameters.put("image_text", imageText);
        parameters.put("trips", loggedInUserTrip.getId().toString());
        MvcResult res = mockMvcTesting.makePostWithFile(URI, "", status().is2xxSuccessful(), null, "image/jpg", parameters);
        String error = (String)res.getModelAndView().getModel().get("error");
        assertNotNull(error);

        assertEquals(1, postService.getUserPosts(loggedInUser).size());
    }

    @Test
    @Transactional
    public void cannotAddPostToAnotherUsersTripAndNoPostIsCreated() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text";

        Trip otherTrip = tripService.createTrip("Other header", "Other trip", Trip.Visibility.PUBLIC, otherUser);
        parameters.put("image_text", imageText);
        parameters.put("trips", otherTrip.getId().toString());
        MvcResult res = mockMvcTesting.makePostWithFile(URI, "", status().is2xxSuccessful(), data, "image/jpg", parameters);
        String error = (String)res.getModelAndView().getModel().get("error");

        assertNotNull(error);
        assertEquals(1, postService.getUserPosts(loggedInUser).size());
        assertEquals(0, tripService.getTrip(otherTrip.getId()).getPosts().size());
    }

    @Test
    @Transactional
    public void invalidTypeAddsErrorToModelAndNoPostIsCreated() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        String imageText = "This is image text";

        parameters.put("image_text", imageText);
        parameters.put("trips", loggedInUserTrip.getId().toString());
        MvcResult res = mockMvcTesting.makePostWithFile(URI, "", status().is2xxSuccessful(), data, "video/avi", parameters);
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
        mockMvcTesting.makePost(URI + "/" + id + "/comment", "/posts/" + id, parameters);

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
        commentService.addCommentToPost(comment, post, loggedInUser);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + post.getId(), "post", "post", "comments");

        Post addedPost = (Post)result.getModelAndView().getModel().get("post");
        List<Comment> comments = (List<Comment>)result.getModelAndView().getModel().get("comments");

        assertEquals(post.getId(), addedPost.getId());
        assertEquals(post.getComments().size(), comments.size());
        assertEquals(post.getComments().get(0).getId(), comments.get(0).getId());
        assertEquals(post.getComments().get(0).getCommentText(), comments.get(0).getCommentText());
    }
}

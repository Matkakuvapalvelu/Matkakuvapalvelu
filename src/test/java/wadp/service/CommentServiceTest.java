package wadp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import wadp.Application;
import wadp.domain.Comment;
import wadp.domain.Image;
import wadp.domain.Post;
import wadp.domain.User;
import wadp.repository.CommentRepository;
import wadp.repository.PostRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ImageService imageService;

    @Autowired
    PostService postService;

    @Autowired
    UserService userService;

    @Autowired
    PostRepository postRepository;

    private Image img;
    private Post post;
    private Comment comment;
    private User user;
    private User loggedInUser;

    public CommentServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {

        comment = new Comment();
        comment.setCommentText("What's up?");

        post = new Post();
        post.setComments(new ArrayList<>());
        postRepository.save(post);

        loggedInUser = userService.createUser("loginuser", "loginuser");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

    }

    @After
    public void tearDown() {
    }

    @Test
    public void addingCommentToAPostSavesTheCommentToARepository() {
        commentService.addCommentToPost(comment, post, loggedInUser);
        Long id = comment.getId();
        assertNotNull(commentRepository.findOne(id));
    }

    @Test
    public void addedCommentHasCorrectCommentText() {
        commentService.addCommentToPost(comment, post, loggedInUser);
        assertEquals(comment.getCommentText(), commentService.getComment(comment.getId()).getCommentText());
    }

    @Test
    public void getCommentsReturnsAListOfComments() {
        Comment comment2 = new Comment();
        comment2.setUser(user);
        commentRepository.save(comment2);
        commentService.addCommentToPost(comment, post, loggedInUser);
        
        assertEquals(2, commentService.getComments().size());
    }
    

    @Test
    public void findCommentsByUserReturnsCorrectComments() {
        Comment comment3 = new Comment();
        commentService.addCommentToPost(comment, post, loggedInUser);
        commentService.addCommentToPost(comment3, post, loggedInUser);
        
        List<Comment> comments = commentService.getUserComments(loggedInUser);
        
        assertEquals(2, comments.size());
        
        for (Comment c : comments) {
            assertEquals(loggedInUser, c.getUser());
        }
    }

}

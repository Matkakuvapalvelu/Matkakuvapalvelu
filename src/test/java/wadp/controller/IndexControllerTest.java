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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.*;
import wadp.service.*;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class IndexControllerTest {


    private final String URI = "/index";

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private TripService tripService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    private MockMvcTesting mockMvcTesting;


    User mostActive;
    User secondMostActive;
    User thirdMostActive;
    User fourthMostActiveMostActive;
    User fifthMostActiveMostActive;

    Image image;

    @Before
    public void setUp() {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);
        addTestData();
    }


    @Test
    public void randomGetIsRedirectedToIndex() throws Exception {
        mockMvcTesting.makeGet("/completelyRandomUri", "index");
    }

    @Test
    @Transactional
    public void relevantValuesAreAddedToModelAndValueAreCorrect() throws Exception {
        MvcResult res = mockMvcTesting.makeGet(URI, "index",
                "activeUsers", "startPoint", "coordinates", "isTripMap", "mapHeight", "tripsInMap");

        assertActiveUsers(res);
        assertStartPoint(res);
        assertCoordinates(res);
        assertFalse((Boolean)res.getModelAndView().getModel().get("isTripMap"));

        assertTripMap(res);

    }

    private void assertTripMap(MvcResult res) {
        Map<Trip, List<Post>> tripMap = (Map<Trip, List<Post>>)res.getModelAndView().getModel().get("tripsInMap");
        assertTrue(tripMap.size() <= 3);
        for (Trip trip : tripMap.keySet()) {
            assertTrue(tripMap.get(trip).size() <= 3);
        }
    }


    private void assertActiveUsers(MvcResult res) {
        Map<User, Integer> userPostCountMap = (Map<User, Integer>)res.getModelAndView().getModel().get("activeUsers");
        assertEquals(5, userPostCountMap.size());
        assertEquals((Integer)5, userPostCountMap.get(mostActive));
        assertEquals((Integer)2, userPostCountMap.get(secondMostActive));
        assertEquals((Integer)6, userPostCountMap.get(thirdMostActive));
        assertEquals((Integer)1, userPostCountMap.get(fourthMostActiveMostActive));
        assertEquals((Integer)3, userPostCountMap.get(fifthMostActiveMostActive));
    }


    private void assertStartPoint(MvcResult res) {
        double[] startPoint = (double[])res.getModelAndView().getModel().get("startPoint");
        assertEquals(3, startPoint.length);
        assertEquals(image.getLatitude(), startPoint[0], 0.001);
        assertEquals(image.getLongitude(), startPoint[1], 0.001);
        assertNotNull(tripService.getTrip((long)startPoint[2]));
    }


    private void assertCoordinates(MvcResult res) {
        List<double[]> coordinates = (List<double[]>)res.getModelAndView().getModel().get("coordinates");
        assertEquals(tripService.getPublicTrips().size(), coordinates.size());
        for (double [] arr : coordinates) {
            assertEquals(3, arr.length);
            assertEquals(image.getLatitude(), arr[0], 0.001);
            assertEquals(image.getLongitude(), arr[1], 0.001);
            assertNotNull(tripService.getTrip((long)arr[2]));
        }
    }

    private void addTestData() {
        mostActive = userService.createUser("Most_Active", "password");
        addPostsAndComments(5, 5, mostActive);

        secondMostActive = userService.createUser("Second_Most_Active", "password");
        addPostsAndComments(2, 5, secondMostActive);
        thirdMostActive = userService.createUser("Third_Most_Active", "password");
        addPostsAndComments(6, 0, thirdMostActive);

        fourthMostActiveMostActive = userService.createUser("Fourth_Most_Active", "password");
        addPostsAndComments(1, 4, fourthMostActiveMostActive);
        fifthMostActiveMostActive = userService.createUser("Fifth_Most_Active", "password");
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

                List<Trip> addTripList = new ArrayList<Trip>();
                if (i % 2 == 0) {
                    Trip trip = tripService.createTrip("Trip header", "Trip description", Trip.Visibility.PUBLIC, creator);
                    trips.add(trip);
                    addTripList.add(trip);
                }

                image = imageService.addImage("image/jpg", "image name", data);
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

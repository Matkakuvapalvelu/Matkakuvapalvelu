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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import wadp.Application;
import wadp.controller.utility.MockMvcTesting;
import wadp.domain.*;
import wadp.service.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TripControllerTest {

    private final String URI = "/trips";

    @Autowired
    private TripService tripService;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    private MockMvcTesting mockMvcTesting;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PostService postService;

    private User loggedInUser;
    private User otherUser;

    @Before
    public void setUp() {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);

        loggedInUser = userService.createUser("loginuser", "loginuser");
        otherUser = userService.createUser("otheruser", "otheruser");

    }

    @Test
    public void tripListHasModelAttributesSetWithCorrectValues() throws Exception {
        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, loggedInUser);
        tripService.createTrip("header", "description", Trip.Visibility.PRIVATE, otherUser);

        MvcResult result = mockMvcTesting.makeGet(URI, "trips", "trips", "visibilities");

        List<Trip> trips = (List<Trip>)result.getModelAndView().getModel().get("trips");
        assertEquals(1, trips.size());
        assertEquals(t.getId(), trips.get(0).getId());

        checkVisibilities(t, result);
    }

    @Test
    public void editModelAttributesAreSetProperlyWhenRequestingAView() throws Exception {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId() + "/edit", "tripedit", "trip", "visibilities");

        Trip modelTrip = (Trip)result.getModelAndView().getModel().get("trip");
        assertEquals(t.getId(), modelTrip.getId());

        checkVisibilities(t, result);
    }


    private void checkVisibilities(Trip t, MvcResult result) {
        List<Trip.Visibility> visibilities = (List<Trip.Visibility>)result.getModelAndView().getModel().get("visibilities");
        assertEquals(3, visibilities.size());
        assertTrue(visibilities.contains(Trip.Visibility.PUBLIC));
        assertTrue(visibilities.contains(Trip.Visibility.FRIENDS));
        assertTrue(visibilities.contains(Trip.Visibility.PRIVATE));
    }

    @Test
    public void nothingIsAddedToModelIfUserHasNoRightToSeeTrip() throws Exception {
        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PRIVATE, otherUser);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId(), "trips");
        assertNull(result.getModelAndView().getModel().get("startPoint"));
        assertNull(result.getModelAndView().getModel().get("trip"));
        assertNull(result.getModelAndView().getModel().get("coordinates"));
    }

    @Test
    public void attributesAreAddedToModelWhenViewingSingleTripWhenFriends() throws Exception {
        Trip t = tripService.createTrip("header", "description", Trip.Visibility.FRIENDS, otherUser);

        Friendship f = friendshipService.createNewFriendshipRequest(otherUser, loggedInUser);
        friendshipService.acceptRequest(f.getId());
        mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip", "startPoint", "trip", "coordinates");
    }

    @Test
    public void attributesAreAddedToModelWhenViewingSingleTripWhenTripIsPublic() throws Exception {
        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, otherUser);

        mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip", "startPoint", "trip", "coordinates");
    }

    @Test
    public void tripAttributeHasCorrectValueWhenViewingSingleTrip() throws Exception {
        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, otherUser);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip");
        Trip modelTrip = (Trip)result.getModelAndView().getModel().get("trip");
        assertEquals(t.getId(), modelTrip.getId());
    }

    @Test
    public void startPointHasDefaultValueWhenTripHasNoPosts() throws Exception {

        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, otherUser);
        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip");

        double[] latitudeLongitudeId = (double[])result.getModelAndView().getModel().get("startPoint");

        assertEquals(2, latitudeLongitudeId.length);
        assertEquals(0.0, latitudeLongitudeId[0], 0.0001);
        assertEquals(0.0, latitudeLongitudeId[1], 0.0001);
    }

    @Test
    public void coordinatesListIsEmptyWhenTripHasNoPosts() throws Exception {

        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, otherUser);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip");

        List<double[]> latitudeLongitudeIds = (List<double[]>)result.getModelAndView().getModel().get("coordinates");
        assertEquals(0, latitudeLongitudeIds.size());
    }

    @Test
    public void startPointAttributeHasCorrectValueWhenViewingSingleTripWithPosts() throws Exception {

        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, otherUser);

        Image firstImage = loadTestImage("src/test/testimg.jpg", t);
        Image secondImage = loadTestImage("src/test/testimg3.jpg", t);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip");

        double[] latitudeLongitudeId = (double[])result.getModelAndView().getModel().get("startPoint");

        assertEquals(3, latitudeLongitudeId.length);
        assertEquals(secondImage.getLatitude(), latitudeLongitudeId[0], 0.0001);
        assertEquals(secondImage.getLongitude(), latitudeLongitudeId[1], 0.0001);
        assertEquals((double)secondImage.getId(), latitudeLongitudeId[2], 0.0001);
    }

    @Test
    @Transactional
    public void coordinatesHaveCorrectValueWhenViewingSingleTripWithPosts() throws Exception {

        Trip t = tripService.createTrip("header", "description", Trip.Visibility.PUBLIC, otherUser);

        Image firstImage = loadTestImage("src/test/testimg.jpg", t);
        Image secondImage = loadTestImage("src/test/testimg3.jpg", t);

        MvcResult result = mockMvcTesting.makeGet(URI + "/" + t.getId(), "trip");

        List<double[]> latitudeLongitudeIds = (List<double[]>)result.getModelAndView().getModel().get("coordinates");

        assertEquals(2, latitudeLongitudeIds.size());

        assertEquals(secondImage.getLatitude(), latitudeLongitudeIds.get(0)[0], 0.0001);
        assertEquals(secondImage.getLongitude(), latitudeLongitudeIds.get(0)[1], 0.0001);
        assertEquals((double)secondImage.getId(), latitudeLongitudeIds.get(0)[2], 0.0001);

        assertEquals(firstImage.getLatitude(), latitudeLongitudeIds.get(1)[0], 0.0001);
        assertEquals(firstImage.getLongitude(), latitudeLongitudeIds.get(1)[1], 0.0001);
        assertEquals((double)firstImage.getId(), latitudeLongitudeIds.get(1)[2], 0.0001);
    }

    private Image loadTestImage(String name, Trip trip) throws IOException {
        File imageFile = new File(name);
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        Image image = imageService.addImage("image/", "foo", data);
        Post post = postService.createPost(image, "Hello!", Arrays.asList(trip), otherUser);

        return image;
    }

    @Test
    public void canPostNewTrips() throws Exception {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("description", "my trip description");
        parameters.put("visibility", "FRIENDS");

        mockMvcTesting.makePost(URI, "/trips/", parameters);

        List<Trip> trips = tripService.getTrips(loggedInUser, loggedInUser);
        assertEquals(1, trips.size());

        assertEquals("my trip description", trips.get(0).getDescription());
        assertEquals(Trip.Visibility.FRIENDS, trips.get(0).getVisibility());
    }


    @Test
    public void canEditTrip() throws Exception {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);

        Map<String, String> parameters = new HashMap<>();
        String description = "new description";
        parameters.put("description", description);
        parameters.put("visibility", Trip.Visibility.PRIVATE.toString());

        mockMvcTesting.makePost(URI + "/" + t.getId() + "/edit", "/trips/", parameters);

        assertEquals(description, tripService.getTrip(t.getId()).getDescription());
        assertEquals(Trip.Visibility.PRIVATE, tripService.getTrip(t.getId()).getVisibility());

    }

    @Test
    @Transactional
    public void canCommentTrips() throws Exception {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, otherUser);

        Map<String, String> parameters = new HashMap<>();
        String commentText = "This is my comment. There are many like it, but this one is mine";
        parameters.put("commentText", commentText);

        mockMvcTesting.makePost(URI + "/" + t.getId() + "/comment", "/trips/" + t.getId(), parameters);

        List<Comment> comments = tripService.getTrip(t.getId()).getComments();
        assertEquals(1, comments.size());
        assertEquals(commentText, comments.get(0).getCommentText());
    }
}

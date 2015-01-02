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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SearchControllerTest {

    private final String URI = "/search";

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
    private TripService tripService;

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    private User loggedInUser;

    private Trip myPrivateTrip;
    private Trip friendFriendTrip;
    private Trip strangerPublicTrip;

    @Before
    public void setUp() throws IOException {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);

        setupTestData();
    }

    private void setupTestData() throws IOException {
        loggedInUser = userService.createUser("loginuser", "loginuser");
        User friend = userService.createUser("friend", "friend");
        Friendship f = friendshipService.createNewFriendshipRequest(friend, loggedInUser);
        friendshipService.acceptRequest(f.getId());

        User stranger = userService.createUser("stranger", "stranger");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

        myPrivateTrip = tripService.createTrip("My awesome trip to Spain", "It was awesome", Trip.Visibility.PRIVATE, loggedInUser);

        friendFriendTrip = tripService.createTrip("Traveling in Spain", "It was fun", Trip.Visibility.FRIENDS, friend);
        tripService.createTrip("Private Portugal trip", "it was nice", Trip.Visibility.PRIVATE, friend);


        strangerPublicTrip = tripService.createTrip("My travels", "are allways fun", Trip.Visibility.PUBLIC, stranger);
        createPost("src/test/testimg.jpg", "Daytrip to Spain!", strangerPublicTrip);
        tripService.createTrip("Images for friends", "I hope they like 'em", Trip.Visibility.FRIENDS, stranger);
        tripService.createTrip("Private stuff", "This stuff is only for me", Trip.Visibility.PRIVATE, stranger);

        tripService.createTrip("Spain", "spain spain spain", Trip.Visibility.PRIVATE, stranger);
    }

    @Test
    public void canRequestSearchView() throws Exception {
        mockMvcTesting.makeGet(URI, "search");
    }

    @Test
    public void emptyTermListRedirectsAndAddsEmptyListToModel() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("keywords", "");
        MvcResult result = mockMvcTesting.makePost(URI, "search", parameters);

        List<Trip> trips = (List<Trip>)result.getFlashMap().get("trips");
        assertNotNull(trips);
        assertEquals(0, trips.size());
    }

    @Test
    @Transactional
    public void correctTripsAreAddedToModelWhenSearching() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("keywords", "spain");
        MvcResult result = mockMvcTesting.makePost(URI, "search", parameters);

        List<Trip> trips = (List<Trip>)result.getFlashMap().get("trips");

        assertNotNull(trips);

        assertTrue(trips.stream().anyMatch(t -> t.getId() == myPrivateTrip.getId()));
        assertTrue(trips.stream().anyMatch(t -> t.getId() == friendFriendTrip.getId()));
        assertTrue(trips.stream().anyMatch(t -> t.getId() == strangerPublicTrip.getId()));

        assertEquals(3, trips.size());
    }

    private Image createPost(String imageName, String postDescription, Trip trip) throws IOException {
        File imageFile = new File(imageName);
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        Image image = imageService.addImage("image/", "foo", data);
        Post post = postService.createPost(image, postDescription, trip);
        return image;
    }

}

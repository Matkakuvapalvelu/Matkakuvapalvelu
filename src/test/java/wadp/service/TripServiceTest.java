package wadp.service;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import wadp.Application;
import wadp.domain.*;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TripServiceTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PostService postService;

    private User loggedInUser;
    private User friend;
    private User stranger;

    private Trip publicTrip;
    private Trip friendTrip;
    private Trip privateTrip;

    @Before
    public void setUp() {

        loggedInUser = userService.createUser("loginuser", "loginuser");
        friend = userService.createUser("friend", "friend");
        Friendship f = friendshipService.createNewFriendshipRequest(friend, loggedInUser);
        friendshipService.acceptRequest(f.getId());

        stranger = userService.createUser("stranger", "stranger");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loggedInUser.getUsername(), loggedInUser.getPassword()));

        publicTrip = tripService.createTrip("PublicTrip", Trip.Visibility.PUBLIC, loggedInUser);
        friendTrip = tripService.createTrip("FriendTrip", Trip.Visibility.FRIENDS, loggedInUser);
        privateTrip = tripService.createTrip("PrivateTrip", Trip.Visibility.PRIVATE, loggedInUser);

    }

    @Test
    public void canCreateTrip() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        assertNotNull(tripService.getTrip(t.getId()));
    }

    @Test
    public void canUpdateTrip() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        t.setDescription("A new description");
        tripService.updateTrip(t, loggedInUser);
        assertEquals("A new description", tripService.getTrip(t.getId()).getDescription());
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateTripThrowsIfDoneByNonOwner() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        t.setDescription("A new description");
        tripService.updateTrip(t, stranger);
    }

    @Test
    public void updateTripDoesNotChangeValuesIfDoneByNonOwner() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        t.setDescription("A new description");
        try {
            tripService.updateTrip(t, stranger);
        } catch (IllegalArgumentException ex) {
            // intentionally empty
        }
        assertEquals("Description", tripService.getTrip(t.getId()).getDescription());
    }

    @Test
    public void updateTripChangesWorks() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        tripService.updateTripChanges(t.getId(), "A new description", Trip.Visibility.PRIVATE, loggedInUser);

        assertEquals("A new description", tripService.getTrip(t.getId()).getDescription());
        assertEquals(Trip.Visibility.PRIVATE, tripService.getTrip(t.getId()).getVisibility());
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateTripChangesThrowsIfDoneByNonOwner() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        tripService.updateTripChanges(t.getId(), "A new description", Trip.Visibility.PRIVATE, stranger);
    }

    @Test
    public void updateTripChangesDoesNotChangeValuesIfDoneByNonOwner() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        try {
            tripService.updateTripChanges(t.getId(), "A new description", Trip.Visibility.PRIVATE, stranger);
        } catch (IllegalArgumentException ex) {
            // intentionally empty
        }
        assertEquals("Description", tripService.getTrip(t.getId()).getDescription());
        assertEquals(Trip.Visibility.PUBLIC, tripService.getTrip(t.getId()).getVisibility());
    }

    @Test
    public void createdTripHasCorrectValues() {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);
        assertEquals("Description", t.getDescription());
        assertEquals(Trip.Visibility.PUBLIC, t.getVisibility());
        assertEquals(loggedInUser, t.getCreator());
    }

    @Test
    public void getTripsReturnsAllTripsIfRequesterIsSelf() {

        List<Trip> trips = tripService.getTrips(loggedInUser, loggedInUser);
        assertEquals(3, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("FriendTrip"))
                .collect(Collectors.toList())
                .size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("PrivateTrip"))
                .collect(Collectors.toList())
                .size());
    }



    @Test
    public void getTripsReturnsAllPublicAndFriendTripsIfRequesterIsFriend() {


        List<Trip> trips = tripService.getTrips(loggedInUser, friend);
        assertEquals(2, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("FriendTrip"))
                .collect(Collectors.toList())
                .size());
    }

    @Test
    public void getTripsReturnsAllPublicTripsIfRequesterIsStranger() {

        List<Trip> trips = tripService.getTrips(loggedInUser, stranger);
        assertEquals(1, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());
    }

    @Test
    public void getTripsReturnsAllPublicTripsIfRequesterIsNotLoggedIn() {

        List<Trip> trips = tripService.getTrips(loggedInUser, null);
        assertEquals(1, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());
    }

    @Test
    public void canSeeTripReturnsCorrectValueForOwner() {
        assertTrue(tripService.hasRightToSeeTrip(publicTrip.getId(), loggedInUser));
        assertTrue(tripService.hasRightToSeeTrip(friendTrip.getId(), loggedInUser));
        assertTrue(tripService.hasRightToSeeTrip(privateTrip.getId(), loggedInUser));
    }

    @Test
    public void canSeeTripReturnsCorrectValueForFriend() {
        assertTrue(tripService.hasRightToSeeTrip(publicTrip.getId(), friend));
        assertTrue(tripService.hasRightToSeeTrip(friendTrip.getId(), friend));
        assertFalse(tripService.hasRightToSeeTrip(privateTrip.getId(), friend));
    }

    @Test
    public void canSeeTripReturnsCorrectValueForStranger() {
        assertTrue(tripService.hasRightToSeeTrip(publicTrip.getId(), stranger));
        assertFalse(tripService.hasRightToSeeTrip(friendTrip.getId(), stranger));
        assertFalse(tripService.hasRightToSeeTrip(privateTrip.getId(), stranger));
    }


    @Test
    public void canSeeTripReturnsCorrectValueForNonLoggedInUser() {
        assertTrue(tripService.hasRightToSeeTrip(publicTrip.getId(), null));
        assertFalse(tripService.hasRightToSeeTrip(friendTrip.getId(), null));
        assertFalse(tripService.hasRightToSeeTrip(privateTrip.getId(), null));
    }

    @Test
    @Transactional
    public void getTripImageCoordinatesReturnCorrectCoordinates() throws IOException {
        Trip t = tripService.createTrip("Description", Trip.Visibility.PUBLIC, loggedInUser);

        Image firstImage = createPost("src/test/testimg.jpg", t);
        Image secondImage = createPost("src/test/testimg3.jpg", t);

        List<double[]> coordinates = tripService.getTripImageCoordinates(t.getId());

        assertEquals(2, coordinates.size());

        assertEquals(secondImage.getLatitude(), coordinates.get(0)[0], 0.0001);
        assertEquals(secondImage.getLongitude(), coordinates.get(0)[1], 0.0001);
        assertEquals((double)secondImage.getId(), coordinates.get(0)[2], 0.0001);

        assertEquals(firstImage.getLatitude(), coordinates.get(1)[0], 0.0001);
        assertEquals(firstImage.getLongitude(), coordinates.get(1)[1], 0.0001);
        assertEquals((double)firstImage.getId(), coordinates.get(1)[2], 0.0001);
    }


    private Image createPost(String name, Trip trip) throws IOException {
        File imageFile = new File(name);
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        Image image = imageService.addImage(new Image(), "image/", "foo", data);
        Post post = postService.createPost(image, "Hello!", Arrays.asList(trip), loggedInUser);
        return image;
    }

}

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
import java.util.ArrayList;
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

        publicTrip = tripService.createTrip("PublicTrip", "PublicTrip", Trip.Visibility.PUBLIC, loggedInUser);
        friendTrip = tripService.createTrip("FriendTrip", "FriendTrip", Trip.Visibility.FRIENDS, loggedInUser);
        privateTrip = tripService.createTrip("PrivateTrip", "PrivateTrip", Trip.Visibility.PRIVATE, loggedInUser);

    }

    @Test
    public void canCreateTrip() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        assertNotNull(tripService.getTrip(t.getId()));
    }

    @Test
    public void canUpdateTrip() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        t.setDescription("A new description");
        tripService.updateTrip(t, loggedInUser);
        assertEquals("A new description", tripService.getTrip(t.getId()).getDescription());
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateTripThrowsIfDoneByNonOwner() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        t.setDescription("A new description");
        tripService.updateTrip(t, stranger);
    }

    @Test
    public void updateTripDoesNotChangeValuesIfDoneByNonOwner() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
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
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        tripService.updateTripChanges(t.getId(), "New header", "A new description", Trip.Visibility.PRIVATE, loggedInUser);

        assertEquals("A new description", tripService.getTrip(t.getId()).getDescription());
        assertEquals(Trip.Visibility.PRIVATE, tripService.getTrip(t.getId()).getVisibility());
    }

    @Test(expected=IllegalArgumentException.class)
    public void updateTripChangesThrowsIfDoneByNonOwner() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        tripService.updateTripChanges(t.getId(), "New header",  "A new description", Trip.Visibility.PRIVATE, stranger);
    }

    @Test
    public void updateTripChangesDoesNotChangeValuesIfDoneByNonOwner() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        try {
            tripService.updateTripChanges(t.getId(), "New header", "A new description", Trip.Visibility.PRIVATE, stranger);
        } catch (IllegalArgumentException ex) {
            // intentionally empty
        }
        assertEquals("Description", tripService.getTrip(t.getId()).getDescription());
        assertEquals(Trip.Visibility.PUBLIC, tripService.getTrip(t.getId()).getVisibility());
    }

    @Test
    public void createdTripHasCorrectValues() {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);
        assertEquals("Description", t.getDescription());
        assertEquals(Trip.Visibility.PUBLIC, t.getVisibility());
        assertEquals(loggedInUser, t.getCreator());
    }

    @Test
    public void getTripsReturnsAllTripsIfRequesterIsSelf() {

        List<Trip> trips = tripService.getTrips(loggedInUser, loggedInUser);
        assertEquals(3, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("FriendTrip"))
                .collect(Collectors.toList())
                .size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("PrivateTrip"))
                .collect(Collectors.toList())
                .size());
    }



    @Test
    public void getTripsReturnsAllPublicAndFriendTripsIfRequesterIsFriend() {


        List<Trip> trips = tripService.getTrips(loggedInUser, friend);
        assertEquals(2, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("FriendTrip"))
                .collect(Collectors.toList())
                .size());
    }

    @Test
    public void getTripsReturnsAllPublicTripsIfRequesterIsStranger() {

        List<Trip> trips = tripService.getTrips(loggedInUser, stranger);
        assertEquals(1, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("PublicTrip"))
                .collect(Collectors.toList())
                .size());
    }

    @Test
    public void getTripsReturnsAllPublicTripsIfRequesterIsNotLoggedIn() {

        List<Trip> trips = tripService.getTrips(loggedInUser, null);
        assertEquals(1, trips.size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getHeader().equals("PublicTrip"))
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
    public void hasRightToSeeTripReturnsFalseIfTripDoesNotExist() {
        assertFalse(tripService.hasRightToSeeTrip(32435l, loggedInUser));
    }

    @Test
    public void getUserTripsReturnsZeroTripsIfThereAreNoTrips() {
        assertEquals(0, tripService.getUserTrips(stranger).size());
    }

    @Test
    public void getUserTripsReturnsCorrectTrips() {
        tripService.createTrip("header", "sdasd", Trip.Visibility.PUBLIC, stranger);
        List<Trip> trips = tripService.getUserTrips(loggedInUser);
        assertEquals(3, trips.size());
        assertEquals(1, trips.stream()
                        .filter(t -> t.getDescription().equals(publicTrip.getDescription()))
                        .collect(Collectors.toList()).size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals(friendTrip.getDescription()))
                .collect(Collectors.toList()).size());

        assertEquals(1, trips.stream()
                .filter(t -> t.getDescription().equals(privateTrip.getDescription()))
                .collect(Collectors.toList()).size());
    }

    @Test
    @Transactional
    public void getTripImageCoordinatesReturnCorrectCoordinates() throws IOException {
        Trip t = tripService.createTrip("header", "Description", Trip.Visibility.PUBLIC, loggedInUser);

        Image firstImage = createPost("src/test/testimg.jpg", "Hello!",t);
        Image secondImage = createPost("src/test/testimg3.jpg", "Hello!", t);

        List<double[]> coordinates = tripService.getTripImageCoordinates(t.getId());

        assertEquals(2, coordinates.size());

        assertEquals(secondImage.getLatitude(), coordinates.get(0)[0], 0.0001);
        assertEquals(secondImage.getLongitude(), coordinates.get(0)[1], 0.0001);
        assertEquals((double)secondImage.getId(), coordinates.get(0)[2], 0.0001);

        assertEquals(firstImage.getLatitude(), coordinates.get(1)[0], 0.0001);
        assertEquals(firstImage.getLongitude(), coordinates.get(1)[1], 0.0001);
        assertEquals((double)firstImage.getId(), coordinates.get(1)[2], 0.0001);
    }

    @Test
    @Transactional
    public void searchWithEmptyKeywordListReturnsEmptyList() {



        List<Trip> trips = tripService.searchTripsWithKeywords(new ArrayList<>(), loggedInUser);
        assertEquals(0, trips.size());
    }

    @Test
    @Transactional
    public void searchReturnsTripsWithKeywords() throws IOException {

        final Trip myPrivateTrip = tripService.createTrip("My awesome trip to Spain", "Was fun",Trip.Visibility.PRIVATE, loggedInUser);

        final Trip friendFriendTrip = tripService.createTrip("Traveling in Spain", "IT was nice", Trip.Visibility.FRIENDS, friend);
        tripService.createTrip("Private Portugal trip", "Fun by myself", Trip.Visibility.PRIVATE, friend);


        final Trip strangerPublicTrip = tripService.createTrip("My travels", "My description", Trip.Visibility.PUBLIC, stranger);
        createPost("src/test/testimg.jpg", "Daytrip to Spain!", strangerPublicTrip);
        tripService.createTrip("Images for friends", "For friends only", Trip.Visibility.FRIENDS, stranger);
        tripService.createTrip("Private stuff", "for me only", Trip.Visibility.FRIENDS, stranger);

        List<Trip> trips = tripService.searchTripsWithKeywords(Arrays.asList("Spain"), loggedInUser);

        assertEquals(3, trips.size());

        assertEquals(1,
                trips
                .stream()
                .filter(t -> t.getId() == myPrivateTrip.getId())
                .collect(Collectors.toList())
                .size());

        assertEquals(1,
                trips
                        .stream()
                        .filter(t -> t.getId() == friendFriendTrip.getId())
                        .collect(Collectors.toList())
                        .size());
        assertEquals(1,
                trips
                        .stream()
                        .filter(t -> t.getId() == strangerPublicTrip.getId())
                        .collect(Collectors.toList())
                        .size());
    }

    @Test
    @Transactional
    public void searchForKeywordsInNonVisibleTripsReturnsEmptyList() throws IOException {

        tripService.createTrip("My awesome trip to Spain", "Spain", Trip.Visibility.PRIVATE, loggedInUser);

        tripService.createTrip("Traveling in Spain", "Spain", Trip.Visibility.FRIENDS, friend);
        tripService.createTrip("Private Portugal trip", "Private Portugal trip", Trip.Visibility.PRIVATE, friend);


        final Trip strangerPublicTrip = tripService.createTrip("My travels", "travels", Trip.Visibility.PUBLIC, stranger);
        createPost("src/test/testimg.jpg", "Daytrip to Spain!", strangerPublicTrip);
        tripService.createTrip("Images for friends", "Images for friends", Trip.Visibility.FRIENDS, stranger);
        tripService.createTrip("Private stuff", "Private stuff", Trip.Visibility.FRIENDS, stranger);

        List<Trip> trips = tripService.searchTripsWithKeywords(Arrays.asList("Portugal", "stuff", "friends"), loggedInUser);

        assertEquals(0, trips.size());
    }


    @Test
    @Transactional
    public void searchWithMultipleKeywordsAndDifferentCasingReturnsCorrectTrips() throws IOException {

        tripService.createTrip("My awesome trip to Spain", "Spain", Trip.Visibility.PRIVATE, loggedInUser);

        final Trip friendFriendTrip = tripService.createTrip("Traveling in Spain", "Spain", Trip.Visibility.FRIENDS, friend);
        tripService.createTrip("Private Portugal trip", "Portugal", Trip.Visibility.PRIVATE, friend);


        final Trip strangerPublicTrip = tripService.createTrip("My travels", "travels", Trip.Visibility.PUBLIC, stranger);
        createPost("src/test/testimg.jpg", "Daytrip to Spain!", strangerPublicTrip);
        tripService.createTrip("Images for friends", "for friends", Trip.Visibility.FRIENDS, stranger);
        tripService.createTrip("Private stuff", "Private", Trip.Visibility.FRIENDS, stranger);

        List<Trip> trips = tripService.searchTripsWithKeywords(Arrays.asList("TRAvels", "TrAvElInG" ), loggedInUser);

        assertEquals(2, trips.size());

        assertEquals(1,
                trips
                        .stream()
                        .filter(t -> t.getId() == friendFriendTrip.getId())
                        .collect(Collectors.toList())
                        .size());
        assertEquals(1,
                trips
                        .stream()
                        .filter(t -> t.getId() == strangerPublicTrip.getId())
                        .collect(Collectors.toList())
                        .size());
    }


    @Test
    @Transactional
    public void searchWithNullUserDefaultToPublicVisibility() throws IOException {

        tripService.createTrip("My awesome trip to Spain", "Spain", Trip.Visibility.PRIVATE, loggedInUser);

        tripService.createTrip("Traveling in Spain", "Spain", Trip.Visibility.FRIENDS, friend);
        tripService.createTrip("Private Portugal trip", "Portugal", Trip.Visibility.PRIVATE, friend);


        final Trip strangerPublicTrip = tripService.createTrip("My travels", "travels", Trip.Visibility.PUBLIC, stranger);
        createPost("src/test/testimg.jpg", "Daytrip to Spain!", strangerPublicTrip);
        tripService.createTrip("Images for friends", "for friends", Trip.Visibility.FRIENDS, stranger);
        tripService.createTrip("Private stuff", "Private", Trip.Visibility.FRIENDS, stranger);

        List<Trip> trips = tripService.searchTripsWithKeywords(Arrays.asList("spain"), null);

        assertEquals(1, trips.size());
        assertEquals(strangerPublicTrip.getId(), trips.get(0).getId());

    }

    // test for a found bug
    @Test
    @Transactional
    public void getStartpointCoordinatesOfTripsHandlesPostListWithNoLocationData() throws IOException {

        createPost("src/test/no_gps.jpg", "Test", publicTrip);

        List<double[]> coordinates = tripService.getStartpointCoordinatesOfTrips(null, null);
        assertEquals(1, coordinates.size());
        assertEquals(0.0, coordinates.get(0)[0], 0.0001);
        assertEquals(0.0, coordinates.get(0)[1], 0.0001);

        assertEquals((long)publicTrip.getId(), (long)coordinates.get(0)[2]);

    }



    private Image createPost(String imageName, String postDescription, Trip trip) throws IOException {
        File imageFile = new File(imageName);
        InputStream is = new FileInputStream(imageFile.getAbsoluteFile());
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        Image image = imageService.addImage("image/", "foo", data);
        Post post = postService.createPost(image, postDescription, Arrays.asList(trip), trip.getCreator());
        return image;
    }





}

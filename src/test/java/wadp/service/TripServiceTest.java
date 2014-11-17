package wadp.service;

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
import wadp.domain.Friendship;
import wadp.domain.Trip;
import wadp.domain.User;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class TripServiceTest {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

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

        publicTrip = tripService.createTrip("PublicTrip", Trip.Visibility.PUBLIC);
        friendTrip = tripService.createTrip("FriendTrip", Trip.Visibility.FRIENDS);
        privateTrip = tripService.createTrip("PrivateTrip", Trip.Visibility.PRIVATE);

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

    // Todo: rest of test methods
}

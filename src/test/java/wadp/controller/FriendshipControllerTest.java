package wadp.controller;


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
import wadp.domain.Friendship;
import wadp.domain.User;
import wadp.service.FriendshipService;
import wadp.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FriendshipControllerTest {
    private final String URI = "/friendship";

    @Autowired
    private UserService userService;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private FilterChainProxy springSecurityFilter;

    private MockMvcTesting mockMvcTesting;

    @Autowired
    private FriendshipService friendshipService;

    // spring security and mockmvc won't play nicely together. This means we have to build our own mock http session
    // and add necessary authentication fields to it as we mock requests
    @Before
    public void setUp() {
        mockMvcTesting = new MockMvcTesting(webAppContext, springSecurityFilter);
        addTestData();
    }




    User loggedInUser;
    private void addTestData() {
        loggedInUser = userService.createUser("loginuser", "loginuser");

        createFriends(loggedInUser);
        createFriendshipRequests(loggedInUser);
    }

    private void createFriends(User loggedInUser) {
        User friend = userService.createUser("friend1", "friend1");
        Friendship f = friendshipService.createNewFriendshipRequest(loggedInUser, friend);
        friendshipService.acceptRequest(f.getId());

        friend = userService.createUser("friend2", "friend2");
        f = friendshipService.createNewFriendshipRequest(friend, loggedInUser);
        friendshipService.acceptRequest(f.getId());
    }

    private void createFriendshipRequests(User loggedInUser) {
        User friend;
        friend = userService.createUser("friendRequest1", "friendRequest1");
        friendshipService.createNewFriendshipRequest(friend, loggedInUser);

        friend = userService.createUser("friendRequest2", "friendRequest2");
        friendshipService.createNewFriendshipRequest(friend, loggedInUser);
    }

    @Test
    public void statusIsOkWhenFetchingFriendsPage() throws Exception {
        mockMvcTesting.makeGet(URI, "friends");
    }

    @Test
    public void friendPageModelContainsFriendAttributeAndCorrectFriends() throws Exception {

        MvcResult result =mockMvcTesting.makeGet(URI, "friends");

        List<User> friends = (List)result.getModelAndView().getModel().get("friends");


        assertEquals("Friend1 not present", 1,
                friends.stream().filter(
                        f -> f.getUsername().equals("friend1")
                ).collect(Collectors.toList()).size()
        );

        assertEquals("Friend2 not present", 1,
                friends.stream().filter(
                        f -> f.getUsername().equals("friend2")
                ).collect(Collectors.toList()).size()
        );

        assertEquals(2, friends.size());
    }

    @Test
    public void friendPageModelContainsFriendRequestAttributeAndCorrectFriendRequests() throws Exception {

        MvcResult result = mockMvcTesting.makeGet(URI, "friends", "friendRequests");

        List<Friendship> friendshipRequests = (List)result.getModelAndView().getModel().get("friendRequests");


        assertEquals(2, friendshipService.getFriendshipRequests(loggedInUser).size());
        assertEquals(2, friendshipRequests.size());

        assertEquals("friendRequest1 not present", 1,
                friendshipRequests.stream().filter(
                        f -> f.getSourceUser().getUsername().equals("friendRequest1"))
                .collect(Collectors.toList()).size()
        );

        assertEquals("friendRequest2 not present", 1,
                friendshipRequests.stream().filter(
                        f -> f.getSourceUser().getUsername().equals("friendRequest2"))
                        .collect(Collectors.toList()).size()
        );
    }

    @Test
    public void creatingFriendRequestRedirectsUser() throws Exception {
        User newUser = userService.createUser("newUser", "newUser");
        final String redirectUrl = "/profile/" + newUser.getId();
        final String postUrl = URI + "/request/" + newUser.getId();

        mockMvcTesting.makePost(postUrl, redirectUrl);
    }

    @Test
    public void friendRequestIsCreatedWhenPosting() throws Exception {

        User newUser = userService.createUser("newUser", "newUser");

        final String postUrl = URI + "/request/" + newUser.getId();
        final String redirectUrl = "/profile/" + newUser.getId();

        mockMvcTesting.makePost(postUrl, redirectUrl);

        List<Friendship> requests = friendshipService.getFriendshipRequests(newUser);

        assertEquals(1, requests.size());
        assertEquals("loginuser", requests.get(0).getSourceUser().getUsername());
    }

    @Test
    public void noRequestIsCreatedIfTargetingSelf() throws Exception {

        User newUser = userService.createUser("newUser", "newUser");

        final String postUrl = URI + "/request/" + loggedInUser.getId();
        final String redirectUrl = "/profile/" + loggedInUser.getId();

        mockMvcTesting.makePost(postUrl, redirectUrl);

        List<Friendship> requests = friendshipService.getFriendshipRequests(newUser);
        assertEquals(0, requests.size());
    }

    @Test
    public void canAcceptFriendshipRequests() throws Exception {
        List<Friendship> requests = friendshipService.getFriendshipRequests(loggedInUser);

        Friendship f = requests.get(0);
        final String postUrl = URI + "/request/accept/" + f.getId();
        final String redirectUrl = URI;

        mockMvcTesting.makePost(postUrl, redirectUrl);

        List<User> friends = friendshipService.getFriends(f.getSourceUser());
        assertEquals(1, friends.size());
        assertEquals("loginuser", friends.get(0).getUsername());
    }

    @Test
    public void canRejectFriendships() throws Exception {
        List<Friendship> requests = friendshipService.getFriendshipRequests(loggedInUser);

        Friendship f = requests.get(0);
        final String postUrl = URI + "/request/reject/" + f.getId();
        final String redirectUrl = URI;

        mockMvcTesting.makePost(postUrl, redirectUrl);

        List<User> friends = friendshipService.getFriends(f.getSourceUser());
        assertEquals(0, friends.size());

        requests = friendshipService.getFriendshipRequests(loggedInUser);

        assertEquals(1, requests.size());
        assertNotEquals(f.getSourceUser(), requests.get(0).getSourceUser());
    }


    @Test
    public void canUnfriend() throws Exception {
        List<User> friends = friendshipService.getFriends(loggedInUser);
        User u = friends.get(0);

        final String postUrl = URI + "/unfriend/" + u.getId();
        final String redirectUrl = URI;

        mockMvcTesting.makeDelete(postUrl, redirectUrl);

        friends = friendshipService.getFriends(loggedInUser);
        assertEquals(1, friends.size());
        assertNotEquals(u.getUsername(), friends.get(0).getUsername());
    }
}

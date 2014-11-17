package wadp.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import wadp.Application;
import wadp.domain.Friendship;
import wadp.domain.Notification;
import wadp.domain.User;
import wadp.repository.FriendshipRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional // throws lazy initialization exceptions without this one
public class FriendshipServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private NotificationService notificationService;

    private List<User> testUsers;

    @Before
    public void setUp() {
        testUsers = new ArrayList<>();
        testUsers.add(userService.createUser("User1", "user1"));
        testUsers.add(userService.createUser("User2", "user2"));
        testUsers.add(userService.createUser("User3", "user3"));
        testUsers.add(userService.createUser("User4", "user4"));
    }

    @Test
    public void canCreateFriendshipRequests() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        assertNotNull(friendshipRepository.findOne(friendship.getId()));
    }

    @Test
    public void createdFriendshipHasCorrectValues() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        assertEquals(testUsers.get(0), friendship.getSourceUser());
        assertEquals(testUsers.get(1), friendship.getTargetUser());
        assertEquals(Friendship.Status.PENDING, friendship.getStatus());
    }

    @Test(expected=IllegalArgumentException.class)
         public void creationThrowsOnRepeatedCreation() {
        friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void creationThrowsOnRepeatedCreationIfParametersAreSwapped() {
        friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendshipService.createNewFriendshipRequest(testUsers.get(1), testUsers.get(0));
    }

    @Test
    public void acceptingFriendshipWorks() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendshipService.acceptRequest(friendship.getId());
        assertEquals(Friendship.Status.ACCEPTED, friendshipRepository.findOne(friendship.getId()).getStatus());
    }

    @Test
    public void rejectingFriendshipWorks() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendshipService.rejectRequest(friendship.getId());
        assertFalse(friendshipRepository.exists(friendship.getId()));
    }

    @Test
    public void removingfFriendshipWorks() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendshipService.unfriend(friendship.getId(), testUsers.get(0));
        assertFalse(friendshipRepository.exists(friendship.getId()));
    }

    @Test
    public void friendshipRequestGeneratesNotification() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        assertEquals(1, notificationService.getUnreadNotificationCountForUser(testUsers.get(1)));

        User user = userService.getUser(testUsers.get(1).getId());
        assertEquals(testUsers.get(0).getUsername(), user.getReceivedNotifications().get(0).getSender().getUsername());
    }

    @Test
    public void acceptingFriendshipRequestGeneratesNotification() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(3));
        friendshipService.acceptRequest(friendship.getId());
        assertEquals(1, notificationService.getUnreadNotificationCountForUser(testUsers.get(0)));

        User user = userService.getUser(testUsers.get(0).getId());
        assertEquals(testUsers.get(3).getUsername(), user.getReceivedNotifications().get(0).getSender().getUsername());
    }

    @Test
    public void rejectingFriendshipRequestGeneratesNotification() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(3));
        friendshipService.rejectRequest(friendship.getId());
        assertEquals(1, notificationService.getUnreadNotificationCountForUser(testUsers.get(0)));

        User user = userService.getUser(testUsers.get(0).getId());
        assertEquals(testUsers.get(3).getUsername(), user.getReceivedNotifications().get(0).getSender().getUsername());
    }

    @Test
    public void unfriendingGeneratesNotificationIfSenderUnfriends() {

        User sender = testUsers.get(0);
        User receiver = testUsers.get(3);

        Friendship friendship = friendshipService.createNewFriendshipRequest(sender, receiver);
        friendshipService.acceptRequest(friendship.getId());

        // erase any generated notifications
        notificationService.deleteNotification(sender.getReceivedNotifications().get(0));
        notificationService.deleteNotification(receiver.getReceivedNotifications().get(0));

        sender = userService.getUser(sender.getId());
        receiver = userService.getUser(receiver.getId());

        // sanity check
        assertEquals(0, sender.getReceivedNotifications().size());
        assertEquals(0, receiver.getReceivedNotifications().size());

        friendshipService.unfriend(friendship.getId(), sender);

        assertEquals(1, notificationService.getUnreadNotificationCountForUser(receiver));

        receiver= userService.getUser(receiver.getId());
        assertEquals(sender.getUsername(), receiver.getReceivedNotifications().get(0).getSender().getUsername());
    }

    @Test
    public void unfriendingGeneratesNotificationIfReceiverUnfriends() {

        User sender = testUsers.get(0);
        User receiver = testUsers.get(3);

        Friendship friendship = friendshipService.createNewFriendshipRequest(sender, receiver);
        friendshipService.acceptRequest(friendship.getId());

        // erase any generated notifications
        notificationService.deleteNotification(sender.getReceivedNotifications().get(0));
        notificationService.deleteNotification(receiver.getReceivedNotifications().get(0));

        sender = userService.getUser(sender.getId());
        receiver = userService.getUser(receiver.getId());

        // sanity check
        assertEquals(0, sender.getReceivedNotifications().size());
        assertEquals(0, receiver.getReceivedNotifications().size());

        friendshipService.unfriend(friendship.getId(), receiver);

        assertEquals(1, notificationService.getUnreadNotificationCountForUser(sender));

        sender = userService.getUser(sender.getId());
        assertEquals(receiver.getUsername(), sender.getReceivedNotifications().get(0).getSender().getUsername());
    }

    @Test(expected=NofriendshipExistsException.class)
    public void acceptingNonExistantRequestThrows() {
        friendshipService.acceptRequest(2545l);
    }


    @Test(expected=NofriendshipExistsException.class)
    public void rejectingNonExistantRequestThrows() {
        friendshipService.rejectRequest(2545l);
    }

    @Test(expected=NofriendshipExistsException.class)
    public void unfriendingNonExistantFriendshipThrows() {
        friendshipService.unfriend(2545l, testUsers.get(0));
    }

    @Test
    public void friendshipIsUpdatedCorrectly() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendship = friendshipService.update(friendship);
        assertEquals(Friendship.Status.ACCEPTED, friendshipRepository.findOne(friendship.getId()).getStatus());
    }

    @Test
    public void canCreateFriendshipIfOneUserIsDifferent() {
        friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        assertNotNull(friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(2)));
    }

    @Test
    public void friendshipExistsReturnsFalseIfNotFriends() {
        assertFalse(friendshipService.areFriends(testUsers.get(0), testUsers.get(1)));
    }

    @Test
    public void friendshipExistsReturnsFalseIfOnlyPendingRequest() {
        friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        assertFalse(friendshipService.areFriends(testUsers.get(0), testUsers.get(1)));
    }

    @Test
    public void friendshipExistsReturnsTrueIfFriendshipExists() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(1));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendship = friendshipService.update(friendship);
        assertTrue(friendshipService.areFriends(testUsers.get(0), testUsers.get(1)));
    }

    @Test
    public void friendshipExistsReturnsTrueIfFriendshipExistsAndParametersAreSwapped() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(1), testUsers.get(0));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendship = friendshipService.update(friendship);
        assertTrue(friendshipService.areFriends(testUsers.get(0), testUsers.get(1)));
    }

    @Test
    public void pendingRequestsAreReturned() {
        friendshipService.createNewFriendshipRequest(testUsers.get(1), testUsers.get(0));
        friendshipService.createNewFriendshipRequest(testUsers.get(2), testUsers.get(0));
        friendshipService.createNewFriendshipRequest(testUsers.get(3), testUsers.get(0));

        List<Friendship> friendshipRequests = friendshipService.getFriendshipRequests(testUsers.get(0));
        assertEquals(3, friendshipRequests.size());

        assertTrue(
                friendshipRequests.stream().anyMatch(
                        f -> !f.equals(testUsers.get(0)) && testUsers.contains(f.getTargetUser())
                )
        );

    }

    @Test
    public void friendsAreReturned() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(1), testUsers.get(0));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendshipService.update(friendship);

        friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(2));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendshipService.update(friendship);

        List<User> friends = friendshipService.getFriends(testUsers.get(0));



        assertEquals(2, friends.size());

        assertTrue("User 2 not present",
                friends.stream().anyMatch(
                        f -> f.getUsername().equals("User2")
                )
        );

        assertTrue("User 3 not present",
                friends.stream().anyMatch(
                        f -> f.getUsername().equals("User3")
                )
        );

    }

    @Test
    public void returnedFriendsDoesNotContainPendingRequests() {
        Friendship friendship = friendshipService.createNewFriendshipRequest(testUsers.get(1), testUsers.get(0));
        friendship.setStatus(Friendship.Status.ACCEPTED);
        friendshipService.update(friendship);

        friendship = friendshipService.createNewFriendshipRequest(testUsers.get(0), testUsers.get(2));
        friendshipService.update(friendship);

        List<User> friends = friendshipService.getFriends(testUsers.get(0));

        assertEquals(1, friends.size());

        assertTrue("User 2 not present",
                friends.stream().anyMatch(
                        f -> f.getUsername().equals("User2")
                )
        );

    }
}


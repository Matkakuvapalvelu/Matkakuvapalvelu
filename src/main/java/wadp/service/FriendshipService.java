package wadp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wadp.domain.Friendship;
import wadp.domain.User;
import wadp.repository.FriendshipRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private NotificationService notificationService;

    public Friendship createNewFriendshipRequest(User source, User target) {
        if (friendshipEntityExists(source, target)) {
            throw new IllegalArgumentException("Friendship entity between two users already exists");
        }
        Friendship friendship = new Friendship();

        friendship.setSourceUser(source);
        friendship.setTargetUser(target);
        friendship.setStatus(Friendship.Status.PENDING);

        notificationService.createNewNotification(
                "Friendship request",
                "User " + source.getUsername() + " wants to be your friend!",
                source,
                target);


        return friendshipRepository.save(friendship);
    }

    public boolean friendshipEntityExists(User first, User second) {
        return friendshipRepository.anyFriendshipCountBetween(first, second) != 0;
    }

    public boolean areFriends(User first, User second) {
        return friendshipRepository.acceptedFriendshipCountBetween(first, second) != 0;
    }

    public Friendship update(Friendship friendship) {
        return friendshipRepository.save(friendship);
    }

    public List<Friendship> getFriendshipRequests(User user) {
        return friendshipRepository.getFriendshipRequests(user);
    }

    public List<User> getFriends(User user) {
        List<Friendship> friendships = friendshipRepository.getFriendships(user);

        List<User> friends = friendships
                .stream()
                .map(
                    f -> {
                        if (f.getSourceUser().getUsername().equals(user.getUsername())) {
                            return f.getTargetUser();
                        } else {
                            return f.getSourceUser();
                        }})
                .collect(Collectors.toList());

        return friends;
    }


    public void acceptRequest(Long id) {
        Friendship friendship = friendshipRepository.getOne(id);
        if (friendship == null) {
            throw new NofriendshipExistsException("No friendship exists");
        }

        friendship.setStatus(Friendship.Status.ACCEPTED);

        notificationService.createNewNotification(
                "Friendship request accepted",
                "User " + friendship.getTargetUser().getUsername() +" has accepted your friendship request!",
                friendship.getTargetUser(),
                friendship.getSourceUser());

        update(friendship);
    }

    public void rejectRequest(Long id) {
        Friendship friendship = friendshipRepository.getOne(id);
        if (friendship == null) {
            throw new NofriendshipExistsException("No friendship exists");
        }

        notificationService.createNewNotification(
                "Friendship request rejected",
                "User " + friendship.getTargetUser().getUsername() +" has rejected your friendship request!",
                friendship.getTargetUser(),
                friendship.getSourceUser());

        friendshipRepository.delete(friendship);
    }

    public void unfriend(Long id, User unfriender) {
        Friendship friendship = friendshipRepository.getOne(id);
        if (friendship == null) {
            throw new NofriendshipExistsException("No friendship exists");
        }

        User target;
        if (unfriender.getUsername().equals(friendship.getSourceUser())) {
            target = friendship.getTargetUser();
        } else {
            target = friendship.getSourceUser();
        }

        notificationService.createNewNotification(
                "Friend unfriended you",
                "User " + unfriender + " has unfriended you!",
                unfriender,
                target);

        friendshipRepository.delete(friendship);
    }
}

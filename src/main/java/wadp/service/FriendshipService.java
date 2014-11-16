package wadp.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import wadp.domain.Friendship;
import wadp.domain.User;
import wadp.repository.FriendshipRepository;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;


    public Friendship createNewFriendshipRequest(User source, User target) {
        if (friendshipEntityExists(source, target)) {
            throw new IllegalArgumentException("Friendship entity between two users already exists");
        }
        Friendship friendship = new Friendship();

        friendship.setSourceUser(source);
        friendship.setTargetUser(target);
        friendship.setStatus(Friendship.Status.PENDING);

        return friendshipRepository.save(friendship);
    }

    private boolean friendshipEntityExists(User first, User second) {
        return friendshipRepository.anyFriendshipCountBetween(first, second) != 0;
    }

    public boolean areFriends(User first, User second) {
        return friendshipRepository.acceptedFriendshipCountBetween(first, second) != 0;
    }

    public Friendship update(Friendship friendship) {
        return friendshipRepository.save(friendship);
    }
}

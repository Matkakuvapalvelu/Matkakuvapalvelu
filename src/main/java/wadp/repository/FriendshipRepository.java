package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wadp.domain.Friendship;
import wadp.domain.User;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {


    @Query("SELECT f FROM Friendship f WHERE ((f.sourceUser = ?1 AND f.targetUser = ?2) OR (f.sourceUser = ?2 AND f.targetUser = ?1)) AND f.status='ACCEPTED'")
    Friendship acceptedFriendshipBetween(User source, User target);

    @Query("SELECT f FROM Friendship f WHERE (f.sourceUser = ?1 AND f.targetUser = ?2) OR (f.sourceUser = ?2 AND f.targetUser = ?1)")
    Friendship anyFriendshipBetween(User first, User second);

    @Query("SELECT f FROM Friendship f WHERE f.targetUser = ?1 AND f.status='PENDING'")
    List<Friendship> getFriendshipRequests(User user);

    @Query("SELECT f FROM Friendship f WHERE (f.sourceUser = ?1 OR f.targetUser=?1) AND f.status='ACCEPTED'")
    List<Friendship> getFriendships(User user);
}

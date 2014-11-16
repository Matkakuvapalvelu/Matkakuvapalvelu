package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wadp.domain.Friendship;
import wadp.domain.User;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT COUNT(f) FROM Friendship f WHERE ((f.sourceUser = ?1 AND f.targetUser = ?2) OR (f.sourceUser = ?2 AND f.targetUser = ?1)) AND f.status='ACCEPTED'")
    long acceptedFriendshipCountBetween(User first, User second);


    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.sourceUser = ?1 AND f.targetUser = ?2) OR (f.sourceUser = ?2 AND f.targetUser = ?1)")
    long anyFriendshipCountBetween(User first, User second);
}

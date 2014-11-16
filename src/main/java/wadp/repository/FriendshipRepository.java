package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
}

package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Post;
import wadp.domain.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import wadp.domain.Trip;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPoster(User poster);
    List<Post> findByTrip(Trip trip, Pageable pageable);
}

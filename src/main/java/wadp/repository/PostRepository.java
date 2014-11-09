package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Post;
import wadp.domain.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPoster(User poster);
}

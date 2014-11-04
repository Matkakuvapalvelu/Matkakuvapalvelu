package wadp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}

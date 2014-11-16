package wadp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import wadp.domain.Comment;
import wadp.domain.Post;
import wadp.domain.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPoster(User poster);
}

package wadp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Comment;
import wadp.domain.Post;
import wadp.domain.User;
import wadp.repository.CommentRepository;
import wadp.repository.PostRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository PostRepository;

    @Autowired
    private UserService userService;

    /**
     *
     *
     */
    @Transactional
    public void addCommentToPost(Comment comment, Post post) {
        User user = addPostingInfo(comment);

        post.getComments().add(comment);
        user.getComments().add(comment);
        commentRepository.save(comment);
    }

    @Transactional
    private User addPostingInfo(Comment comment) {
        User user = userService.getAuthenticatedUser();
        comment.setUser(user);
        comment.setCreationTime(Date.from(Instant.now()));
        return user;
    }

    public List<Comment> getComments() {
        return commentRepository.findAll();
    }

    public List<Comment> getUserComments(User user) {
        return commentRepository.findByPoster(user);
    }

    public Comment getComment(Long id) {
        return commentRepository.findOne(id);
    }

}

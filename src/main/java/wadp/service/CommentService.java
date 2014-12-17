package wadp.service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Comment;
import wadp.domain.Post;
import wadp.domain.Trip;
import wadp.domain.User;
import wadp.repository.CommentRepository;
import wadp.repository.PostRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Adds comment to given post with given commenter
     *
     * @param comment Comment to be added
     * @param post Post that is commented
     * @param commenter Commenter
     */
    @Transactional
    public void addCommentToPost(Comment comment, Post post, User commenter) {
        addPostingInfo(comment, commenter);
        post.getComments().add(comment);
        commentRepository.save(comment);
    }

    /**
     * Adds comment to given trip with given commenter
     *
     * @param comment Comment to be added
     * @param trip Trip that is commented
     * @param commenter Commenter
     */
    @Transactional
    public void addCommentToTrip(Comment comment, Trip trip, User commenter) {
        addPostingInfo(comment, commenter);
        trip.getComments().add(comment);
        commentRepository.save(comment);
    }

    @Transactional
    private void addPostingInfo(Comment comment, User commenter) {
        comment.setUser(commenter);
        comment.setCreationTime(Date.from(Instant.now()));

        commenter.getComments().add(comment);
    }

    @Transactional
    public void deleteCommentFromTrip(Trip trip, User user, Comment comment) {
        trip.getComments().remove(comment);
        user.getComments().remove(comment);
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteCommentFromPost(Post post, User user, Comment comment) {
        post.getComments().remove(comment);
        user.getComments().remove(comment);
        commentRepository.delete(comment);
    }

    @Transactional
    public List<Comment> getComments() {
        return commentRepository.findAll();
    }

    @Transactional
    public List<Comment> getUserComments(User user) {
        return commentRepository.findByPoster(user);
    }

    @Transactional
    public Comment getComment(Long id) {
        return commentRepository.findOne(id);
    }

}

package wadp.service;

import java.util.ArrayList;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.Comment;
import wadp.domain.Post;
import wadp.domain.User;
import wadp.repository.CommentRepository;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private UserService userService;
    
    /**
     * 
     * 
     */
    @Transactional
    public void addCommentToPost(String commentText, Post post) {
        ArrayList<Comment> comments;
        User user = userService.getAuthenticatedUser();
        Comment comment = new Comment();
        comment.setCommentText(commentText);
        
        
        if (user.getComments() == null) {
            comments = new ArrayList<>();
            user.setComments(comments);
        }
        
        user.getComments().add(comment);        
        commentRepository.save(comment);
    }
}

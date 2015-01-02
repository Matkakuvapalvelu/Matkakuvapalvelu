package wadp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wadp.domain.*;
import wadp.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

// postgresql barfs without the @Transactional annotation as images might be split into multiple values inside database
// and therefore database needs multiple queries to fetch all the parts -> requires transaction for safety

/**
 * Service that handles anything post related, such as creating and listing posts
 */
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CommentService commentService;

    /**
     * This method creates and saves new post into database and returns the resulting object.
     *
     * @param image Image associated with the post. This is mandatory and must not be null
     * @param imageText Image text. Can be empty
     * @param trip The trip this post is associated with.
     * @return Instance of post after it is saved to database
     */

    @Transactional
    public Post createPost(Image image, String imageText, Trip trip) {

        if (image == null || trip == null) {
            throw new IllegalArgumentException("Image, trip and poster must not be null when creating new post");
        }

        Post post = new Post();
        post.setImageText(imageText);
        post.setImage(image);
        post.setTrip(trip);
        post.setPoster(trip.getCreator());

        post = postRepository.save(post);
        trip.getPosts().add(post);

        return post;
    }



    /**
     * This method returns list of posts made by given user
     *
     * @param user User whose posts are wanted
     * @return List of posts by the user
     */
    @Transactional
    public List<Post> getUserPosts(User user) {
        return postRepository.findByPoster(user);
    }

    /**
     *  Returns single post with given id or null if post does not exist
     * @param id Post id
     * @return Post with id or null if post does not exist
     */
    @Transactional
    public Post getPost(Long id) {
        return postRepository.findOne(id);
    }
    
    /**
     *  Returns List of newest posts from specific trip
     * @param trip trip which posts are wanted
     * @param top how many values we want
     * @return List of top posts
     */
    @Transactional
    public List<Post> getNewestPosts(Trip trip, int top) {
        return postRepository.findByTrip(trip, new PageRequest(0, top, Sort.Direction.DESC, "postDate"));
    }

    public void updatePost(Post post) {
        postRepository.save(post);
    }

    public void deletePost(Post p) {
        imageService.deleteImage(p.getImage());

        List<Comment> comments = new ArrayList<>(p.getComments());

        comments.forEach(commentService::deleteComment);

        p.getTrip().getPosts().remove(p);
        postRepository.delete(p);
    }

}
